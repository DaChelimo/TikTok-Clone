/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.selected_tag

import androidx.lifecycle.SavedStateHandle
import com.andre_max.tiktokclone.domain.tags.usecase.GetTagVideosUseCase
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
class SelectedTagViewModelTest {

    private val tagsRepo = FakeTagsRepo()
    private val videosRepo = FakeVideosRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel(handle: SavedStateHandle) = SelectedTagViewModel(
        getTagVideos = GetTagVideosUseCase(tagsRepo, videosRepo),
        savedStateHandle = handle,
    )

    @Test
    fun `loads the tagged videos for the nav argument`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a"), makeVideo("b"))
        tagsRepo.tagVideoIds["travel"] = listOf("a", "b")

        val viewModel = buildViewModel(SavedStateHandle(mapOf("tagName" to "travel")))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.tagName).isEqualTo("travel")
        assertThat(state.videos.map { it.videoId }).containsExactly("a", "b").inOrder()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `a missing nav argument leaves the state empty instead of crashing`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = buildViewModel(SavedStateHandle())
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.videos).isEmpty()
    }
}
