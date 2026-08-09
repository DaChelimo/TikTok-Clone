/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.tags.usecase

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.core.fold
import com.andre_max.tiktokclone.domain.core.map
import com.andre_max.tiktokclone.domain.tags.TagsRepo
import com.andre_max.tiktokclone.domain.videos.VideosRepo
import com.andre_max.tiktokclone.models.video.RemoteVideo
import javax.inject.Inject

/**
 * Resolves a tag to its videos: id list from the tag tree, each id resolved against the
 * global collection. A dangling id (deleted video, stale tag entry) is skipped rather than
 * failing the page.
 */
class GetTagVideosUseCase @Inject constructor(
    private val tagsRepo: TagsRepo,
    private val videosRepo: VideosRepo,
) {

    suspend operator fun invoke(tagName: String): Result<List<RemoteVideo>, DataError.Network> =
        tagsRepo.fetchTagVideoIds(tagName).map { videoIds ->
            videoIds.mapNotNull { videoId ->
                videosRepo.fetchVideo(videoId).fold({ it }, { null })
            }
        }
}
