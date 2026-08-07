/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.username

import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.auth.usecase.CheckUsernameAvailabilityUseCase
import com.andre_max.tiktokclone.domain.auth.usecase.ValidateUsernameUseCase
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.test_utils.FakeUsernameRepo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

/**
 * Screen 4 (spec §4.4) — the only screen this phase with real I/O.
 *
 * This suite deliberately does **not** use `CoroutineTestRule`: its `TestCoroutineDispatcher`
 * has its own clock, and the debounce/staleness tests below need `Dispatchers.Main` driven by
 * `runTest`'s scheduler so that `advanceTimeBy` actually moves the debounce forward. Main is
 * therefore bound to `testScheduler` inside each test.
 */
@ExperimentalCoroutinesApi
class CreateUsernameViewModelTest {

    private val repo = FakeUsernameRepo()

    private fun viewModel(): CreateUsernameViewModel {
        val validate = ValidateUsernameUseCase()
        return CreateUsernameViewModel(
            validateUsername = validate,
            checkAvailability = CheckUsernameAvailabilityUseCase(validate, repo),
            usernameRepo = repo,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty with nothing checked yet`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        viewModel().uiState.test {
            assertThat(awaitItem()).isEqualTo(CreateUsernameUiState())
        }
    }

    @Test
    fun `the typed username is echoed back into state`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))

        assertThat(viewModel.uiState.value.username).isEqualTo("andrew")
    }

    @Test
    fun `an invalid username surfaces a UiText error without hitting the repo`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        viewModel.onAction(CreateUsernameAction.UsernameChanged("ab"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.error).isInstanceOf(UiText::class.java)
        assertThat(repo.availabilityCallCount).isEqualTo(0)
    }

    @Test
    fun `three rapid keystrokes trigger exactly one availability check`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        viewModel.onAction(CreateUsernameAction.UsernameChanged("andr"))
        advanceTimeBy(100)
        viewModel.onAction(CreateUsernameAction.UsernameChanged("andre"))
        advanceTimeBy(100)
        viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
        advanceUntilIdle()

        assertThat(repo.availabilityQueries).containsExactly("andrew")
    }

    @Test
    fun `no availability check runs before the debounce window elapses`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
        advanceTimeBy(CreateUsernameViewModel.DEBOUNCE_MILLIS - 1)

        assertThat(repo.availabilityCallCount).isEqualTo(0)
    }

    @Test
    fun `a free username is reported as available`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isAvailable).isTrue()
        assertThat(state.error).isNull()
        assertThat(state.isContinueEnabled).isTrue()
    }

    @Test
    fun `a taken username is reported with an error and cannot continue`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        repo.takenUsernames += "andrew"
        val viewModel = viewModel()

        viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isAvailable).isFalse()
        assertThat(state.error).isInstanceOf(UiText::class.java)
        assertThat(state.isContinueEnabled).isFalse()
    }

    @Test
    fun `a network failure mid-check surfaces an error rather than claiming availability`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            repo.availabilityError = DataError.Network.NO_INTERNET
            val viewModel = viewModel()

            viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertThat(state.error).isInstanceOf(UiText::class.java)
            assertThat(state.isAvailable).isNull()
        }

    @Test
    fun `a stale in-flight check never overwrites the result of a newer query`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        // "taken_one" is claimed, "free_two" is not. The first lookup is held in flight long
        // enough that the second query starts and finishes first; the stale "taken" answer must
        // not land on top of it.
        repo.takenUsernames += "taken_one"
        repo.availabilityDelayMillis = 5_000
        val viewModel = viewModel()

        viewModel.onAction(CreateUsernameAction.UsernameChanged("taken_one"))
        advanceTimeBy(CreateUsernameViewModel.DEBOUNCE_MILLIS + 100)
        repo.availabilityDelayMillis = 0
        viewModel.onAction(CreateUsernameAction.UsernameChanged("free_two"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.username).isEqualTo("free_two")
        assertThat(state.isAvailable).isTrue()
    }

    @Test
    fun `the checking flag is raised during the lookup and lowered afterwards`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        repo.availabilityDelayMillis = 1_000
        val viewModel = viewModel()

        viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
        advanceTimeBy(CreateUsernameViewModel.DEBOUNCE_MILLIS + 100)
        assertThat(viewModel.uiState.value.isCheckingAvailability).isTrue()

        advanceUntilIdle()
        assertThat(viewModel.uiState.value.isCheckingAvailability).isFalse()
    }

    @Test
    fun `ContinueClicked claims the username and emits NavigateToHome`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()
        viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(CreateUsernameAction.ContinueClicked)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToHome)
        }
        assertThat(repo.claimedUsernames).containsExactly("andrew")
    }

    @Test
    fun `a failed claim emits ShowError and does not navigate`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        repo.claimError = DataError.Network.SERVER
        val viewModel = viewModel()
        viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(CreateUsernameAction.ContinueClicked)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(AuthEvent.ShowError::class.java)
        }
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(CreateUsernameAction.BackClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateBack)
        }
    }

    @Test
    fun `a navigation event is emitted exactly once and not replayed to a new collector`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val viewModel = viewModel()
            viewModel.onAction(CreateUsernameAction.UsernameChanged("andrew"))
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.onAction(CreateUsernameAction.ContinueClicked)
                advanceUntilIdle()
                awaitItem()
            }

            viewModel.events.test {
                expectNoEvents()
            }
        }
}
