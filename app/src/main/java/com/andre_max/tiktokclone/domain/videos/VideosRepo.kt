/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.videos

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.video.RemoteVideo

/**
 * Read/like access to the global video collection.
 *
 * Narrower than the legacy `repo.network.videos.VideosRepo` on purpose: saving an uploaded
 * video belongs to the upload slice and will be added here when that slice migrates, with a
 * `Result` return instead of the legacy `onComplete` callback.
 */
interface VideosRepo {

    /**
     * Fetches a batch of up to 12 videos. The cap bounds network use per fetch; the "random"
     * in the name is aspirational — the backing query has no ranking, which is exactly the
     * gap the future recommendation model fills (via [usecase.GetFeedBatchUseCase], not here).
     */
    suspend fun fetchRandomVideos(): Result<List<RemoteVideo>, DataError.Network>

    suspend fun fetchVideo(videoId: String): Result<RemoteVideo, DataError.Network>

    /** Whether the current user has the video in their liked collection. */
    suspend fun isVideoLiked(videoId: String): Result<Boolean, DataError.Network>

    /**
     * Likes or unlikes in three writes: my liked-videos entry, the video's like count, the
     * author's total-likes count. Not atomic — a partial failure mid-call is a known,
     * accepted risk (docs/SPEC.md §5) until this becomes a server-side transaction.
     */
    suspend fun likeOrUnlikeVideo(
        videoId: String,
        authorId: String,
        shouldLike: Boolean,
    ): EmptyResult<DataError.Network>
}
