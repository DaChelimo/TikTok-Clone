/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.discover

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.tags.usecase.GetTagVideosUseCase
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.test_utils.FakeTagsRepo
import com.andre_max.tiktokclone.test_utils.FakeVideosRepo
import com.andre_max.tiktokclone.test_utils.makeVideo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
class DiscoverViewModelTest {

    private val tagsRepo = FakeTagsRepo()
    private val videosRepo = FakeVideosRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel() = DiscoverViewModel(
        tagsRepo = tagsRepo,
        getTagVideos = GetTagVideosUseCase(tagsRepo, videosRepo),
    )

    @Test
    fun `loads the popular tags and each row's videos`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        tagsRepo.popularTags = listOf(Tag("travel", 5), Tag("fun", 3))
        videosRepo.randomBatch = listOf(makeVideo("a"))
        tagsRepo.tagVideoIds["travel"] = listOf("a")

        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.tagRows.map { it.tag.name }).containsExactly("travel", "fun").inOrder()
        assertThat(state.tagRows.first().videos.map { it.videoId }).containsExactly("a")
        assertThat(state.tagRows.last().videos).isEmpty()
    }

    @Test
    fun `a failed tag fetch leaves an empty, non-loading screen`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        tagsRepo.fetchError = DataError.Network.NO_INTERNET

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.tagRows).isEmpty()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}
