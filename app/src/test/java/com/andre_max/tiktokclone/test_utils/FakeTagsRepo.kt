/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.tags.TagsRepo
import com.andre_max.tiktokclone.models.tag.Tag

/** Hand-written [TagsRepo] double. */
class FakeTagsRepo : TagsRepo {

    var popularTags: List<Tag> = emptyList()
    val tagVideoIds = mutableMapOf<String, List<String>>()
    var fetchError: DataError.Network? = null

    override suspend fun fetchPopularTags(): Result<List<Tag>, DataError.Network> =
        fetchError?.let { Result.Error(it) } ?: Result.Success(popularTags)

    override suspend fun fetchTagVideoIds(
        tagName: String,
    ): Result<List<String>, DataError.Network> =
        fetchError?.let { Result.Error(it) }
            ?: Result.Success(tagVideoIds[tagName] ?: emptyList())
}
