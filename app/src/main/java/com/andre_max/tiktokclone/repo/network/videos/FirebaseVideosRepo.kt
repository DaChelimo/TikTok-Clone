/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.videos

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * [com.andre_max.tiktokclone.domain.videos.VideosRepo] against Firebase Realtime Database.
 *
 * Replaces the query/like half of the legacy `DefaultVideosRepo` (which stays alive for its
 * remaining legacy callers until the upload/profile slices migrate off it — strangler-fig).
 */
class FirebaseVideosRepo(
    private val realFire: FirebaseDatabase = Firebase.database,
    private val firePath: FirePath = FirePath(),
) : com.andre_max.tiktokclone.domain.videos.VideosRepo {

    override suspend fun fetchRandomVideos(): Result<List<RemoteVideo>, DataError.Network> =
        safeCall {
            realFire.getReference(firePath.getAllVideosPath()).also { it.keepSynced(true) }
                .limitToFirst(BATCH_SIZE)
                .get()
                .await()
                .getValue<Map<String, RemoteVideo>>()
                ?.values
                ?.toList() ?: listOf()
        }

    override suspend fun fetchVideo(videoId: String): Result<RemoteVideo, DataError.Network> {
        val result = safeCall {
            realFire.getReference(firePath.getAllVideosPath())
                .child(videoId)
                .get()
                .await()
                .getValue<RemoteVideo>()
        }
        return when (result) {
            is Result.Success ->
                result.data?.let { Result.Success(it) }
                    ?: Result.Error(DataError.Network.NOT_FOUND)

            is Result.Error -> result
        }
    }

    override suspend fun isVideoLiked(videoId: String): Result<Boolean, DataError.Network> =
        safeCall {
            realFire.getReference(firePath.getMyLikedVideos())
                .child(videoId)
                .get()
                .await()
                .exists()
        }

    override suspend fun likeOrUnlikeVideo(
        videoId: String,
        authorId: String,
        shouldLike: Boolean,
    ): EmptyResult<DataError.Network> = safeCall {
        realFire.getReference(firePath.getMyLikedVideos())
            .child(videoId)
            .setValue(if (shouldLike) videoId else null)
            .await()

        adjustCount(
            realFire.getReference(firePath.getAllVideosPath()).child(videoId).child("likes"),
            increase = shouldLike,
        )
        adjustCount(
            realFire.getReference(firePath.getUserInfo(authorId)).child("totalLikes"),
            increase = shouldLike,
        )
    }

    /**
     * Read-increment-write, as the legacy repo did. Racy under concurrent likes; moving to
     * `runTransaction` is a deliberate follow-up, not a drive-by (docs/SPEC.md §5).
     */
    private suspend fun adjustCount(
        ref: com.google.firebase.database.DatabaseReference,
        increase: Boolean,
    ) {
        val count = ref.get().await().getValue<Long>() ?: 0
        ref.setValue(if (increase) count + 1 else count - 1).await()
    }

    companion object {
        /** Caps network use per fetch; the value the legacy feed always used. */
        private const val BATCH_SIZE = 12
    }
}
