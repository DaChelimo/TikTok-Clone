/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.user

import com.andre_max.tiktokclone.domain.auth.UserProfileRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.map
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of the domain [UserProfileRepo].
 *
 * Writes under the uid it is given rather than reading `Firebase.auth.uid` back out, unlike the
 * legacy `DefaultUserRepo.addUserToDatabase` — those two are the same value today only because
 * the write happens immediately after sign-in, which is a coincidence of timing rather than
 * something the function guarantees.
 */
class DefaultUserProfileRepo(
    private val firePath: FirePath = FirePath(),
    private val realFire: FirebaseDatabase = Firebase.database,
) : UserProfileRepo {

    override suspend fun createProfile(
        uid: String,
        username: String,
        profilePictureUrl: String?,
    ): EmptyResult<DataError.Network> = safeCall {
        val user = User(
            username = username,
            followers = 0,
            following = 0,
            totalLikes = 0,
            profilePictureUrl = profilePictureUrl,
            uid = uid,
        )

        realFire.getReference(firePath.getUserInfo(uid)).setValue(user).await()
    }.map { }
}
