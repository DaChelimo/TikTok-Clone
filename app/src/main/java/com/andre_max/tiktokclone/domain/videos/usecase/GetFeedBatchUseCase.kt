/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.videos.usecase

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.core.map
import com.andre_max.tiktokclone.domain.videos.VideosRepo
import com.andre_max.tiktokclone.models.video.RemoteVideo
import javax.inject.Inject

/**
 * Produces the next batch of feed videos, excluding ones the feed already shows.
 *
 * This is the seam where the recommendation model plugs in: replace the body (fetch-random +
 * filter) with a ranked query without touching the ViewModel or the repo interface. The
 * exclusion exists because the backing query is a fixed window, so consecutive fetches return
 * overlapping results — the legacy feed appended those duplicates verbatim, which a keyed
 * `LazyColumn`/`Pager` would crash on (duplicate keys), so they are filtered here instead.
 */
class GetFeedBatchUseCase @Inject constructor(
    private val videosRepo: VideosRepo,
) {

    suspend operator fun invoke(
        excludeIds: Set<String>,
    ): Result<List<RemoteVideo>, DataError.Network> =
        videosRepo.fetchRandomVideos().map { batch ->
            batch.filter { it.videoId !in excludeIds }
        }
}
