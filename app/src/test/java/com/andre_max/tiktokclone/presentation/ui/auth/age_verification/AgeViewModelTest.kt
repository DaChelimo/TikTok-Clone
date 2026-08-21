/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.auth.usecase.ValidateAgeUseCase
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class AgeViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private fun viewModel() = AgeViewModel(ValidateAgeUseCase())

    private val validDob = LocalDate.now().minusYears(20)
    private val tooYoungDob = LocalDate.now().minusYears(5)

    @Test
    fun `initial state has no date, no error and a disabled continue button`() = runTest {
        viewModel().uiState.test {
            assertThat(awaitItem()).isEqualTo(AgeUiState())
        }
    }

    @Test
    fun `DateChanged with a valid date clears the error and enables continue`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(AgeAction.DateChanged(validDob))

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.dateOfBirth).isEqualTo(validDob)
            assertThat(state.error).isNull()
            assertThat(state.isContinueEnabled).isTrue()
        }
    }

    @Test
    fun `DateChanged with an under-13 date surfaces a UiText error and disables continue`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(AgeAction.DateChanged(tooYoungDob))

            viewModel.uiState.test {
                val state = awaitItem()
                assertThat(state.error).isInstanceOf(UiText::class.java)
                assertThat(state.isContinueEnabled).isFalse()
            }
        }

    @Test
    fun `correcting an invalid date clears the error`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(AgeAction.DateChanged(tooYoungDob))
        viewModel.onAction(AgeAction.DateChanged(validDob))

        viewModel.uiState.test {
            assertThat(awaitItem().error).isNull()
        }
    }

    @Test
    fun `ContinueClicked with a valid date emits NavigateToSelectAuth`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(AgeAction.DateChanged(validDob))

        viewModel.events.test {
            viewModel.onAction(AgeAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToSelectAuth)
        }
    }

    @Test
    fun `ContinueClicked with an under-13 date does not navigate`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(AgeAction.DateChanged(tooYoungDob))

        viewModel.events.test {
            viewModel.onAction(AgeAction.ContinueClicked)

            expectNoEvents()
        }
    }

    @Test
    fun `ContinueClicked before any date is picked does not navigate`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(AgeAction.ContinueClicked)

            expectNoEvents()
        }
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(AgeAction.BackClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateBack)
        }
    }

    @Test
    fun `a navigation event is emitted exactly once and not replayed to a new collector`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(AgeAction.DateChanged(validDob))

            viewModel.events.test {
                viewModel.onAction(AgeAction.ContinueClicked)
                assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToSelectAuth)
            }

            // A recreated collector (e.g. after a rotation) must not see the event again.
            viewModel.events.test {
                expectNoEvents()
            }
        }
}
