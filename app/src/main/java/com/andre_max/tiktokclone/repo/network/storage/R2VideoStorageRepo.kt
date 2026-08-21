/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.storage

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.upload.VideoStorageRepo
import com.andre_max.tiktokclone.repo.network.utils.HttpException
import com.andre_max.tiktokclone.repo.network.utils.await
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.io.File
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * [VideoStorageRepo] against Cloudflare R2, reached through the `workers/r2-upload` Worker.
 *
 * Two round trips rather than one because R2 has no notion of a Firebase user: the Worker
 * verifies the caller's Firebase ID token and hands back a short-lived presigned PUT url, then
 * the file goes straight from the device to R2. Uploading *through* the Worker instead would
 * double the bandwidth and hit the Workers request-body size limit, so the handshake is the
 * cost of keeping storage off Firebase (and off the Blaze billing plan).
 *
 * Object keys are chosen by the Worker from the verified uid, never by this class — the client
 * cannot name the object it writes.
 */
class R2VideoStorageRepo(
    private val workerBaseUrl: String,
    private val httpClient: OkHttpClient,
    private val json: Json,
    private val fireAuth: FirebaseAuth = Firebase.auth,
) : VideoStorageRepo {

    override suspend fun uploadVideo(localFilePath: String): Result<String, DataError.Network> =
        safeCall {
            val ticket = requestUploadTicket(currentIdToken())
            uploadTo(ticket, File(localFilePath))
            ticket.publicUrl
        }

    /** Firebase mints a fresh token when the cached one is close to expiry, so `forceRefresh` stays false. */
    private suspend fun currentIdToken(): String {
        val user = fireAuth.currentUser ?: throw IllegalStateException("Not signed in")
        return user.getIdToken(false).await().token
            ?: throw IllegalStateException("Firebase returned no ID token")
    }

    private suspend fun requestUploadTicket(idToken: String): UploadTicket {
        val request = Request.Builder()
            .url("${workerBaseUrl.trimEnd('/')}/uploads")
            .header("Authorization", "Bearer $idToken")
            .post(
                json.encodeToString(UploadTicketRequest(VIDEO_CONTENT_TYPE))
                    .toRequestBody(JSON_MEDIA_TYPE),
            )
            .build()

        return httpClient.newCall(request).await().use { response ->
            if (!response.isSuccessful) {
                throw HttpException(response.code, "Upload ticket refused")
            }
            json.decodeFromString<UploadTicket>(response.body?.string().orEmpty())
        }
    }

    /**
     * The presigned url signs the content type, so the PUT has to send back exactly the type the
     * Worker authorised — [UploadTicket.contentType], not a type guessed from the file.
     */
    private suspend fun uploadTo(ticket: UploadTicket, file: File) {
        val request = Request.Builder()
            .url(ticket.uploadUrl)
            .put(file.asRequestBody(ticket.contentType.toMediaType()))
            .build()

        httpClient.newCall(request).await().use { response ->
            if (!response.isSuccessful) {
                throw HttpException(response.code, "R2 rejected the upload")
            }
        }
    }

    @Serializable
    private data class UploadTicketRequest(val contentType: String)

    /** Worker response. Extra fields it returns (objectKey, expiry) are ignored by [Json]. */
    @Serializable
    private data class UploadTicket(
        val uploadUrl: String,
        val publicUrl: String,
        val contentType: String,
    )

    companion object {
        /** The compressor always emits mp4, so the app never negotiates a type. */
        private const val VIDEO_CONTENT_TYPE = "video/mp4"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}
