/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.storage

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Drives the real two-request handshake against a [MockWebServer] standing in for both the
 * Worker and R2 — the presigned url the "Worker" returns simply points back at the same server.
 */
class R2VideoStorageRepoTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val server = MockWebServer()
    private val fireAuth = mockk<FirebaseAuth>()

    private lateinit var repo: R2VideoStorageRepo
    private lateinit var videoFile: File

    @Before
    fun setUp() {
        server.start()
        signInAs("uid-1")

        videoFile = tempFolder.newFile("clip.mp4").apply { writeText(FILE_CONTENT) }
        repo = R2VideoStorageRepo(
            workerBaseUrl = server.url("/").toString(),
            httpClient = OkHttpClient(),
            json = Json { ignoreUnknownKeys = true },
            fireAuth = fireAuth,
        )
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `returns the public url once the file reaches R2`() = runTest {
        server.enqueue(ticketResponse())
        server.enqueue(MockResponse().setResponseCode(200))

        val result = repo.uploadVideo(videoFile.path)

        assertThat(result).isEqualTo(Result.Success(PUBLIC_URL))
    }

    @Test
    fun `authenticates the ticket request with the firebase id token`() = runTest {
        server.enqueue(ticketResponse())
        server.enqueue(MockResponse().setResponseCode(200))

        repo.uploadVideo(videoFile.path)

        val ticketRequest = server.takeRequest()
        assertThat(ticketRequest.method).isEqualTo("POST")
        assertThat(ticketRequest.path).isEqualTo("/uploads")
        assertThat(ticketRequest.getHeader("Authorization")).isEqualTo("Bearer id-token")
    }

    /** The presign signs the content type, so a mismatched PUT header would be rejected by R2. */
    @Test
    fun `puts the file bytes to the presigned url with the authorised content type`() = runTest {
        server.enqueue(ticketResponse())
        server.enqueue(MockResponse().setResponseCode(200))

        repo.uploadVideo(videoFile.path)

        server.takeRequest()
        val uploadRequest = server.takeRequest()
        assertThat(uploadRequest.method).isEqualTo("PUT")
        assertThat(uploadRequest.path).isEqualTo("/presigned-object")
        assertThat(uploadRequest.getHeader("Content-Type")).isEqualTo("video/mp4")
        assertThat(uploadRequest.body.readUtf8()).isEqualTo(FILE_CONTENT)
    }

    @Test
    fun `maps a rejected ticket to unauthorized without uploading`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))

        val result = repo.uploadVideo(videoFile.path)

        assertThat(result).isEqualTo(Result.Error(DataError.Network.UNAUTHORIZED))
        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun `maps an R2 failure to a server error`() = runTest {
        server.enqueue(ticketResponse())
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repo.uploadVideo(videoFile.path)

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVER))
    }

    @Test
    fun `fails without calling the worker when signed out`() = runTest {
        every { fireAuth.currentUser } returns null

        val result = repo.uploadVideo(videoFile.path)

        assertThat(result).isEqualTo(Result.Error(DataError.Network.UNKNOWN))
        assertThat(server.requestCount).isEqualTo(0)
    }

    private fun signInAs(uid: String) {
        val tokenResult = mockk<GetTokenResult> { every { token } returns "id-token" }
        val user = mockk<FirebaseUser> {
            every { this@mockk.uid } returns uid
            every { getIdToken(false) } returns Tasks.forResult(tokenResult)
        }
        every { fireAuth.currentUser } returns user
    }

    private fun ticketResponse() = MockResponse()
        .setHeader("Content-Type", "application/json")
        .setBody(
            """
            {
              "uploadUrl": "${server.url("/presigned-object")}",
              "publicUrl": "$PUBLIC_URL",
              "objectKey": "videos/uid-1/abc.mp4",
              "contentType": "video/mp4",
              "expiresInSeconds": 900
            }
            """.trimIndent(),
        )

    private companion object {
        const val FILE_CONTENT = "fake-mp4-bytes"
        const val PUBLIC_URL = "https://videos.example.com/videos/uid-1/abc.mp4"
    }
}
