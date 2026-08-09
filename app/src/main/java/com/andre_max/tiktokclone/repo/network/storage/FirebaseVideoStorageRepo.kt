/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.storage

import androidx.core.net.toUri
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.upload.VideoStorageRepo
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.UUID
import kotlinx.coroutines.tasks.await

/** [VideoStorageRepo] against Firebase Storage — same `videos/$uid/$uuid` path as legacy. */
class FirebaseVideoStorageRepo(
    private val fireStorage: FirebaseStorage = Firebase.storage,
    private val fireAuth: FirebaseAuth = Firebase.auth,
) : VideoStorageRepo {

    override suspend fun uploadVideo(localFilePath: String): Result<String, DataError.Network> =
        safeCall {
            val storageRef =
                fireStorage.getReference("videos/${fireAuth.uid}/${UUID.randomUUID()}")

            val uploadTask = storageRef.putFile(File(localFilePath).toUri()).await()
            uploadTask.storage.downloadUrl.await().toString()
        }
}
