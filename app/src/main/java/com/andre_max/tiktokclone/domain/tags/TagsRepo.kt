/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.tags

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.tag.Tag

/**
 * The discover surface's tag tree. Writing tags happens as part of saving a video
 * ([com.andre_max.tiktokclone.domain.videos.VideosRepo.saveVideo]), not here.
 */
interface TagsRepo {

    /** Up to 12 tags by descending use count. */
    suspend fun fetchPopularTags(): Result<List<Tag>, DataError.Network>

    /** Ids of videos carrying [tagName]; resolve them via `VideosRepo.fetchVideo`. */
    suspend fun fetchTagVideoIds(tagName: String): Result<List<String>, DataError.Network>
}
