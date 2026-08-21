/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.auth.UserProfileRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result

/** Hand-written [UserProfileRepo] double — deterministic, no mocking framework (spec §4.4). */
class FakeUserProfileRepo : UserProfileRepo {

    data class CreatedProfile(
        val uid: String,
        val username: String,
        val profilePictureUrl: String?,
    )

    val createdProfiles = mutableListOf<CreatedProfile>()

    var createError: DataError.Network? = null

    override suspend fun createProfile(
        uid: String,
        username: String,
        profilePictureUrl: String?,
    ): EmptyResult<DataError.Network> {
        createError?.let { return Result.Error(it) }
        createdProfiles += CreatedProfile(uid, username, profilePictureUrl)
        return Result.Success(Unit)
    }
}
