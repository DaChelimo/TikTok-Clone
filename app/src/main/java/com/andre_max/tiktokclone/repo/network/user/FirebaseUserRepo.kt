/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.user

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * [com.andre_max.tiktokclone.domain.user.UserRepo] against Firebase.
 *
 * Same paths and write sequence as the legacy `DefaultUserRepo` (which stays alive for its
 * remaining legacy callers), with the writes awaited so a failure is reportable instead of
 * fire-and-forget.
 */
class FirebaseUserRepo(
    private val fireAuth: FirebaseAuth = Firebase.auth,
    private val realFire: FirebaseDatabase = Firebase.database,
    private val firePath: FirePath = FirePath(),
) : com.andre_max.tiktokclone.domain.user.UserRepo {

    override fun isSignedIn(): Boolean = fireAuth.currentUser != null

    override fun currentUid(): String? = fireAuth.uid

    override suspend fun getUserProfile(uid: String): Result<User, DataError.Network> {
        val result = safeCall {
            realFire.getReference(firePath.getUserInfo(uid))
                .get()
                .await()
                .getValue<User>()
        }
        return when (result) {
            is Result.Success ->
                result.data?.let { Result.Success(it) }
                    ?: Result.Error(DataError.Network.NOT_FOUND)

            is Result.Error -> result
        }
    }

    override suspend fun isFollowing(otherUid: String): Result<Boolean, DataError.Network> =
        safeCall {
            realFire.getReference(firePath.getMyFollowingPath())
                .child(otherUid)
                .get()
                .await()
                .exists()
        }

    override suspend fun follow(otherUid: String): EmptyResult<DataError.Network> =
        changeFollowRelationship(otherUid, shouldFollow = true)

    override suspend fun unfollow(otherUid: String): EmptyResult<DataError.Network> =
        changeFollowRelationship(otherUid, shouldFollow = false)

    /** Four writes: my following list + count, their followers list + count. Not atomic. */
    private suspend fun changeFollowRelationship(
        otherUid: String,
        shouldFollow: Boolean,
    ): EmptyResult<DataError.Network> {
        val myUid = fireAuth.uid ?: return Result.Error(DataError.Network.UNAUTHORIZED)

        return safeCall {
            realFire.getReference(firePath.getMyFollowingPath())
                .child(otherUid)
                .setValue(if (shouldFollow) otherUid else null)
                .await()
            adjustCount(
                realFire.getReference("users/$myUid").child(FOLLOWING),
                increase = shouldFollow,
            )

            realFire.getReference(firePath.getUserFollowerPath(otherUid))
                .child(myUid)
                .setValue(if (shouldFollow) myUid else null)
                .await()
            adjustCount(
                realFire.getReference("users/$otherUid").child(FOLLOWERS),
                increase = shouldFollow,
            )
        }
    }

    /**
     * The collection stores video ids; each resolves against the global `videos/` path. A
     * dangling id (video deleted, entry not cleaned up) is skipped rather than failing the
     * whole grid — same net behavior as the legacy `filterNotNull()` in the tab ViewModel.
     */
    override suspend fun getUserVideos(
        uid: String,
        videoType: VideoType,
    ): Result<List<RemoteVideo>, DataError.Network> = safeCall {
        val videoIds = realFire
            .getReference(firePath.getUserVideos(uid, videoType))
            .get()
            .await()
            .getValue<Map<String, String>>()
            ?.values
            ?.toList() ?: listOf()

        val allVideos = realFire.getReference(firePath.getAllVideosPath())
        videoIds.mapNotNull { videoId ->
            allVideos.child(videoId).get().await().getValue<RemoteVideo>()
        }
    }

    private suspend fun adjustCount(ref: DatabaseReference, increase: Boolean) {
        val count = ref.get().await().getValue<Long>() ?: 0
        ref.setValue(if (increase) count + 1 else count - 1).await()
    }

    companion object {
        private const val FOLLOWERS = "followers"
        private const val FOLLOWING = "following"
    }
}
