package com.andre_max.tiktokclone.repo.network.user

import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class NameRepo : INameRepo {
    private val realFire = Firebase.database
    private val firePath = FirePath()

    override suspend fun doesNameExist(username: String) =
        realFire
            .getReference(firePath.getTakenUsernames(username))
            .get()
            .await()
            ?.exists() == true

    override suspend fun registerUserName(username: String) = safeAccess {
        realFire
            .getReference(firePath.getTakenUsernames(username))
            .setValue(username)
            .await()

        true
    }
}