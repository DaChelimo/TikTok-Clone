/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.upload.usecase.PostVideoUseCase
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.models.upload.Progress
import com.andre_max.tiktokclone.test_utils.FakeVideoCompressor
import com.andre_max.tiktokclone.test_utils.FakeVideoStorageRepo
import com.andre_max.tiktokclone.test_utils.FakeVideosRepo
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
class PostVideoViewModelTest {

    private val compressor = FakeVideoCompressor()
    private val storageRepo = FakeVideoStorageRepo()
    private val videosRepo = FakeVideosRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel(
        localVideo: LocalVideo? = LocalVideo("/videos/raw.mp4", 9_000, "123"),
    ) = PostVideoViewModel(
        postVideo = PostVideoUseCase(compressor, storageRepo, videosRepo),
        savedStateHandle = localVideo
            ?.let { SavedStateHandle(mapOf("localVideo" to it)) } ?: SavedStateHandle(),
    )

    @Test
    fun `a successful post reports DONE and navigates home`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel()

        viewModel.events.test {
            viewModel.onAction(PostVideoAction.DescriptionChanged("hello #world"))
            viewModel.onAction(PostVideoAction.PostClicked)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(PostVideoEvent.NavigateHome)
        }
        assertThat(viewModel.uiState.value.uploadStatus).isEqualTo(Progress.DONE)
        assertThat(videosRepo.saveCalls.single().description).isEqualTo("hello #world")
    }

    @Test
    fun `a failed post reports FAILED with a message`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        storageRepo.uploadError = DataError.Network.UPLOAD_FAILED
        val viewModel = buildViewModel()

        viewModel.events.test {
            viewModel.onAction(PostVideoAction.PostClicked)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(PostVideoEvent.ShowMessage::class.java)
        }
        assertThat(viewModel.uiState.value.uploadStatus).isEqualTo(Progress.FAILED)
    }

    @Test
    fun `posting twice while active only posts once`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel()

        viewModel.onAction(PostVideoAction.PostClicked)
        viewModel.onAction(PostVideoAction.PostClicked)
        advanceUntilIdle()

        assertThat(videosRepo.saveCalls).hasSize(1)
    }

    @Test
    fun `a missing nav argument makes posting a no-op`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel(localVideo = null)

        viewModel.onAction(PostVideoAction.PostClicked)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.uploadStatus).isEqualTo(Progress.IDLE)
        assertThat(videosRepo.saveCalls).isEmpty()
    }
}
