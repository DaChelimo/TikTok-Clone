/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.record

import app.cash.turbine.test
import com.andre_max.tiktokclone.test_utils.FakeLocalMediaRepo
import com.andre_max.tiktokclone.test_utils.FakeLocalSpaceRepo
import com.andre_max.tiktokclone.test_utils.FakeRecordVideoRepo
import com.andre_max.tiktokclone.test_utils.FakeUserRepo
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
class RecordVideoViewModelTest {

    private val localSpaceRepo = FakeLocalSpaceRepo()
    private val recordVideoRepo = FakeRecordVideoRepo()
    private val localMediaRepo = FakeLocalMediaRepo()
    private val userRepo = FakeUserRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel() = RecordVideoViewModel(
        localSpaceRepo = localSpaceRepo,
        recordVideoRepo = recordVideoRepo,
        localMediaRepo = localMediaRepo,
        userRepo = userRepo,
    )

    @Test
    fun `starting signed out shows a message and opens nothing`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.signedIn = false
        val viewModel = buildViewModel()

        viewModel.events.test {
            assertThat(viewModel.startVideo()).isNull()
            assertThat(awaitItem()).isInstanceOf(RecordVideoEvent.ShowMessage::class.java)
        }
        assertThat(recordVideoRepo.initCalls).isEmpty()
        assertThat(viewModel.uiState.value.hasRecordingStarted).isFalse()
    }

    @Test
    fun `starting with low storage shows the little-space overlay`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        localSpaceRepo.enoughSpace = false
        val viewModel = buildViewModel()

        assertThat(viewModel.startVideo()).isNull()

        assertThat(viewModel.uiState.value.showLittleSpace).isTrue()
        assertThat(recordVideoRepo.initCalls).isEmpty()
    }

    @Test
    fun `starting opens a MediaStore entry and flips the recording flags`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel()

        // A null descriptor from the fake still exercises the session flags; the descriptor
        // itself is an OS handle no JVM test can construct meaningfully.
        viewModel.startVideo()

        assertThat(recordVideoRepo.initCalls).hasSize(1)
        assertThat(viewModel.uiState.value.hasRecordingStarted).isTrue()
        assertThat(viewModel.uiState.value.isRecording).isTrue()
    }

    @Test
    fun `stopping publishes the pending entry`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel()
        viewModel.startVideo()

        viewModel.stopVideo()
        advanceUntilIdle()

        assertThat(recordVideoRepo.stopCalls).isEqualTo(1)
        assertThat(viewModel.uiState.value.hasRecordingStarted).isFalse()
        assertThat(viewModel.uiState.value.isRecording).isFalse()
    }

    @Test
    fun `stopping before starting does nothing`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel()

        viewModel.stopVideo()
        advanceUntilIdle()

        assertThat(recordVideoRepo.stopCalls).isEqualTo(0)
    }
}
