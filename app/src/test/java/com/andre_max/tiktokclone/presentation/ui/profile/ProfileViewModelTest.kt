/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.profile

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.user.usecase.GetVisibleVideoTypesUseCase
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.test_utils.FakeAuthRepo
import com.andre_max.tiktokclone.test_utils.FakeUserRepo
import com.andre_max.tiktokclone.test_utils.makeUser
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

/**
 * Pins the profile behaviour: uid resolution (nav arg vs own), the visibility rule driving
 * which tabs exist, per-tab video loading, and sign-out.
 */
@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private val userRepo = FakeUserRepo()
    private val authRepo = FakeAuthRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel(handle: SavedStateHandle = SavedStateHandle()) = ProfileViewModel(
        userRepo = userRepo,
        authRepo = authRepo,
        getVisibleVideoTypes = GetVisibleVideoTypesUseCase(),
        savedStateHandle = handle,
    )

    @Test
    fun `no nav argument means my own profile — all three tabs, private last`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["me-uid"] = makeUser("me-uid", username = "andrew")
        userRepo.userVideos["me-uid" to VideoType.PUBLIC] = listOf(makeVideo("pub"))
        userRepo.userVideos["me-uid" to VideoType.PRIVATE] = listOf(makeVideo("priv"))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isOwnProfile).isTrue()
        assertThat(state.user?.username).isEqualTo("andrew")
        assertThat(state.tabs.map { it.videoType })
            .containsExactly(VideoType.PUBLIC, VideoType.LIKED, VideoType.PRIVATE)
            .inOrder()
        assertThat(state.tabs.first().videos.map { it.videoId }).containsExactly("pub")
        assertThat(state.tabs.last().videos.map { it.videoId }).containsExactly("priv")
    }

    @Test
    fun `another user's profile hides private, and liked follows their flag`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["other-uid"] =
            makeUser("other-uid", username = "maria").copy(showLikedVideos = true)

        val viewModel = buildViewModel(SavedStateHandle(mapOf("uid" to "other-uid")))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isOwnProfile).isFalse()
        assertThat(state.tabs.map { it.videoType })
            .containsExactly(VideoType.PUBLIC, VideoType.LIKED)
            .inOrder()
    }

    @Test
    fun `another user's profile defaults to public only`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["other-uid"] = makeUser("other-uid")

        val viewModel = buildViewModel(SavedStateHandle(mapOf("uid" to "other-uid")))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.tabs.map { it.videoType })
            .containsExactly(VideoType.PUBLIC)
    }

    @Test
    fun `confirmed sign-out reaches the repo and emits the event`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["me-uid"] = makeUser("me-uid")

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ProfileAction.SignOutClicked)
            assertThat(viewModel.uiState.value.showSignOutDialog).isTrue()

            viewModel.onAction(ProfileAction.SignOutConfirmed)
            advanceUntilIdle()

            assertThat(authRepo.calls).contains(FakeAuthRepo.Call.SignOut)
            assertThat(awaitItem()).isEqualTo(ProfileEvent.SignedOut)
        }
    }

    @Test
    fun `dismissing the sign-out dialog signs nothing out`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["me-uid"] = makeUser("me-uid")

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(ProfileAction.SignOutClicked)
        viewModel.onAction(ProfileAction.SignOutDismissed)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.showSignOutDialog).isFalse()
        assertThat(authRepo.calls).isEmpty()
    }

    @Test
    fun `tapping a video emits navigation with the video`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        userRepo.uid = "me-uid"
        userRepo.profiles["me-uid"] = makeUser("me-uid")
        val video = makeVideo("pub")
        userRepo.userVideos["me-uid" to VideoType.PUBLIC] = listOf(video)

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ProfileAction.VideoClicked(video))
            assertThat(awaitItem()).isEqualTo(ProfileEvent.NavigateToVideo(video))
        }
    }
}
