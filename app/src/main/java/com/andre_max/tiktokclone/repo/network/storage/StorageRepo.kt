package com.andre_max.tiktokclone.repo.network.storage

import android.net.Uri
import com.andre_max.tiktokclone.models.TheResult
import com.andre_max.tiktokclone.models.TheResult.Companion.theError
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.*

class StorageRepo {
    private val fireStorage = Firebase.storage

    suspend fun uploadVideo(localVideoUri: Uri?) = safeAccess {
        val storageRef =
            fireStorage.getReference("videos/${Firebase.auth.uid}/${UUID.randomUUID()}")

        // If localVideoUri is null, the safe access will catch and log it
        val uploadTask = storageRef.putFile(localVideoUri!!).await()
        uploadTask.storage.downloadUrl.await()
    }

}