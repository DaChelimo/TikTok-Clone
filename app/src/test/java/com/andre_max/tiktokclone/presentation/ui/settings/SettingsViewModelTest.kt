/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.settings

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.test_utils.FakeUserRepo
import com.andre_max.tiktokclone.test_utils.makeUser
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

/** The showLikedVideos toggle — the setting the User model always carried but no UI wrote. */
@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    private val userRepo = FakeUserRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel() = SettingsViewModel(userRepo)

    @Test
    fun `loads the current flag from my profile`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["me-uid"] = makeUser("me-uid").copy(showLikedVideos = true)

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showLikedVideos).isTrue()
    }

    @Test
    fun `toggling writes the new value optimistically`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["me-uid"] = makeUser("me-uid")

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(SettingsAction.ShowLikedVideosToggled)
        assertThat(viewModel.uiState.value.showLikedVideos).isTrue()
        advanceUntilIdle()

        assertThat(userRepo.showLikedVideosUpdates).containsExactly(true)
    }

    @Test
    fun `a failed write reverts the toggle`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["me-uid"] = makeUser("me-uid")
        userRepo.updateShowLikedVideosError = DataError.Network.NO_INTERNET

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(SettingsAction.ShowLikedVideosToggled)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showLikedVideos).isFalse()
    }
}
