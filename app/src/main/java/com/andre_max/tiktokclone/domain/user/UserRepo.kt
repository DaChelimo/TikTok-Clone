/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.user

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType

/**
 * Reads other users' profiles and manages the follow relationship.
 *
 * Distinct from [com.andre_max.tiktokclone.domain.auth.UserProfileRepo] (which only *creates*
 * the profile during sign-up) so that auth keeps its deliberately narrow surface.
 */
interface UserRepo {

    /** Whether a signed-in account exists on this device. */
    fun isSignedIn(): Boolean

    /** The signed-in user's uid, or null when signed out. */
    fun currentUid(): String?

    suspend fun getUserProfile(uid: String): Result<User, DataError.Network>

    /** Whether the current user follows [otherUid]. */
    suspend fun isFollowing(otherUid: String): Result<Boolean, DataError.Network>

    /**
     * Adds [otherUid] to my following (and me to their followers), adjusting both counts.
     * Like video-liking, the writes are not atomic — partial failure is a known risk.
     */
    suspend fun follow(otherUid: String): EmptyResult<DataError.Network>

    suspend fun unfollow(otherUid: String): EmptyResult<DataError.Network>

    /**
     * One of a user's three video collections. Visibility of non-public collections is the
     * caller's concern (see usecase.GetVisibleVideoTypesUseCase) — the repo just reads.
     */
    suspend fun getUserVideos(
        uid: String,
        videoType: VideoType,
    ): Result<List<RemoteVideo>, DataError.Network>
}
