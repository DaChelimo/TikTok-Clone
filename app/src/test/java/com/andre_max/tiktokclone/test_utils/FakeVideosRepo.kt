/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.videos.VideosRepo
import com.andre_max.tiktokclone.models.video.RemoteVideo

/** Hand-written [VideosRepo] double — deterministic, no mocking framework. */
class FakeVideosRepo : VideosRepo {

    /** What the next [fetchRandomVideos] returns — reassign between fetches to vary batches. */
    var randomBatch: List<RemoteVideo> = emptyList()
    var fetchError: DataError.Network? = null
    var fetchCount = 0

    val likedVideoIds = mutableSetOf<String>()
    var likeError: DataError.Network? = null

    data class LikeCall(val videoId: String, val authorId: String, val shouldLike: Boolean)

    val likeCalls = mutableListOf<LikeCall>()

    override suspend fun fetchRandomVideos(): Result<List<RemoteVideo>, DataError.Network> {
        fetchCount++
        return fetchError?.let { Result.Error(it) } ?: Result.Success(randomBatch)
    }

    override suspend fun fetchVideo(videoId: String): Result<RemoteVideo, DataError.Network> =
        randomBatch.find { it.videoId == videoId }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Network.NOT_FOUND)

    override suspend fun isVideoLiked(videoId: String): Result<Boolean, DataError.Network> =
        Result.Success(videoId in likedVideoIds)

    override suspend fun likeOrUnlikeVideo(
        videoId: String,
        authorId: String,
        shouldLike: Boolean,
    ): EmptyResult<DataError.Network> {
        likeCalls += LikeCall(videoId, authorId, shouldLike)
        likeError?.let { return Result.Error(it) }
        if (shouldLike) likedVideoIds += videoId else likedVideoIds -= videoId
        return Result.Success(Unit)
    }
}
