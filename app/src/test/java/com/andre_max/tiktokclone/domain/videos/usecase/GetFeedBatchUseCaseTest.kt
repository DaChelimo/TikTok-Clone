/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.videos.usecase

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.test_utils.FakeVideosRepo
import com.andre_max.tiktokclone.test_utils.makeVideo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The feed's fetch seam. The exclusion behaviour matters because the backing query is a fixed
 * window — without it, every "load more" would re-append the same videos (and crash a keyed
 * lazy list on duplicate keys).
 */
class GetFeedBatchUseCaseTest {

    private val videosRepo = FakeVideosRepo()
    private val getFeedBatch = GetFeedBatchUseCase(videosRepo)

    @Test
    fun `returns the whole batch when nothing is excluded`() = runTest {
        videosRepo.randomBatch = listOf(makeVideo("a"), makeVideo("b"))

        val result = getFeedBatch(excludeIds = emptySet())

        assertThat(result).isEqualTo(Result.Success(videosRepo.randomBatch))
    }

    @Test
    fun `filters out videos the feed already shows`() = runTest {
        val fresh = makeVideo("c")
        videosRepo.randomBatch = listOf(makeVideo("a"), makeVideo("b"), fresh)

        val result = getFeedBatch(excludeIds = setOf("a", "b"))

        assertThat(result).isEqualTo(Result.Success(listOf(fresh)))
    }

    @Test
    fun `a fully-seen batch becomes an empty success, not an error`() = runTest {
        videosRepo.randomBatch = listOf(makeVideo("a"))

        val result = getFeedBatch(excludeIds = setOf("a"))

        assertThat(result).isEqualTo(Result.Success(emptyList<Nothing>()))
    }

    @Test
    fun `passes the repo error through untouched`() = runTest {
        videosRepo.fetchError = DataError.Network.NO_INTERNET

        val result = getFeedBatch(excludeIds = emptySet())

        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
    }
}
