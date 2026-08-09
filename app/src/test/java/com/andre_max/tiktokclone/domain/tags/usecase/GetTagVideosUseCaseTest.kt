/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.tags.usecase

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.test_utils.FakeTagsRepo
import com.andre_max.tiktokclone.test_utils.FakeVideosRepo
import com.andre_max.tiktokclone.test_utils.makeVideo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetTagVideosUseCaseTest {

    private val tagsRepo = FakeTagsRepo()
    private val videosRepo = FakeVideosRepo()
    private val getTagVideos = GetTagVideosUseCase(tagsRepo, videosRepo)

    @Test
    fun `resolves the tag's ids against the global collection`() = runTest {
        videosRepo.randomBatch = listOf(makeVideo("a"), makeVideo("b"))
        tagsRepo.tagVideoIds["travel"] = listOf("a", "b")

        val result = getTagVideos("travel")

        assertThat(result).isEqualTo(Result.Success(videosRepo.randomBatch))
    }

    @Test
    fun `a dangling id is skipped, not fatal`() = runTest {
        videosRepo.randomBatch = listOf(makeVideo("a"))
        tagsRepo.tagVideoIds["travel"] = listOf("a", "deleted-video")

        val result = getTagVideos("travel")

        assertThat(result).isEqualTo(Result.Success(listOf(videosRepo.randomBatch.first())))
    }

    @Test
    fun `a failed id fetch surfaces as the error`() = runTest {
        tagsRepo.fetchError = DataError.Network.NO_INTERNET

        assertThat(getTagVideos("travel"))
            .isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
    }
}
