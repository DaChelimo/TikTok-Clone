/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.tag

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.tags.TagsRepo
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * [TagsRepo] against the `tags/$name/{info,tag-videos}` tree that `saveVideo` writes.
 *
 * Deliberate fix over the legacy `DefaultTagRepo.fetchPopularTags`, which ordered by a
 * `count` child that doesn't exist at that level (`info/count` does) and took the FIRST 12 of
 * an ascending order — i.e. the least popular tags, when it returned anything at all.
 */
class FirebaseTagsRepo(
    private val realFire: FirebaseDatabase = Firebase.database,
    private val firePath: FirePath = FirePath(),
) : TagsRepo {

    override suspend fun fetchPopularTags(): Result<List<Tag>, DataError.Network> = safeCall {
        realFire.getReference(firePath.getTagsPath())
            .orderByChild("info/count")
            .limitToLast(POPULAR_TAG_COUNT)
            .get()
            .await()
            .children
            .mapNotNull { it.child("info").getValue<Tag>() }
            .sortedByDescending { it.count }
    }

    override suspend fun fetchTagVideoIds(
        tagName: String,
    ): Result<List<String>, DataError.Network> = safeCall {
        realFire.getReference(firePath.getTagVideos(tagName))
            .limitToFirst(TAG_VIDEO_BATCH)
            .get()
            .await()
            .children
            .mapNotNull { it.getValue<String>() }
    }

    companion object {
        private const val POPULAR_TAG_COUNT = 12

        /** Same 12-per-fetch network cap the legacy repo used. */
        private const val TAG_VIDEO_BATCH = 12
    }
}
