/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.user.UserRepo
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType

/** Hand-written [UserRepo] double — deterministic, no mocking framework. */
class FakeUserRepo : UserRepo {

    var signedIn = true
    var uid: String? = "me-uid"

    val profiles = mutableMapOf<String, User>()
    val followingIds = mutableSetOf<String>()
    val followCalls = mutableListOf<String>()
    val unfollowCalls = mutableListOf<String>()
    var followError: DataError.Network? = null

    override fun isSignedIn(): Boolean = signedIn

    override fun currentUid(): String? = if (signedIn) uid else null

    override suspend fun getUserProfile(uid: String): Result<User, DataError.Network> =
        profiles[uid]?.let { Result.Success(it) } ?: Result.Error(DataError.Network.NOT_FOUND)

    override suspend fun isFollowing(otherUid: String): Result<Boolean, DataError.Network> =
        Result.Success(otherUid in followingIds)

    override suspend fun follow(otherUid: String): EmptyResult<DataError.Network> {
        followCalls += otherUid
        followError?.let { return Result.Error(it) }
        followingIds += otherUid
        return Result.Success(Unit)
    }

    override suspend fun unfollow(otherUid: String): EmptyResult<DataError.Network> {
        unfollowCalls += otherUid
        followError?.let { return Result.Error(it) }
        followingIds -= otherUid
        return Result.Success(Unit)
    }

    val userVideos = mutableMapOf<Pair<String, VideoType>, List<RemoteVideo>>()
    var userVideosError: DataError.Network? = null

    override suspend fun getUserVideos(
        uid: String,
        videoType: VideoType,
    ): Result<List<RemoteVideo>, DataError.Network> {
        userVideosError?.let { return Result.Error(it) }
        return Result.Success(userVideos[uid to videoType] ?: emptyList())
    }
}
