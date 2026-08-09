/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email

import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.auth.usecase.ValidateEmailUseCase
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/**
 * Screen 2 (spec §4.2).
 *
 * `ValidateEmailUseCase` is already implemented and has its own suite; the email edge cases in
 * §4.2 are exercised here, through the ViewModel, because what this screen adds is the mapping
 * from `EmailError` to `UiText` and the one-shot navigation event.
 */
@ExperimentalCoroutinesApi
class BasicEmailViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val signUpSession = SignUpSession()

    private fun viewModel() = BasicEmailViewModel(ValidateEmailUseCase(), signUpSession)

    private suspend fun stateAfterTyping(email: String): BasicEmailUiState {
        val viewModel = viewModel()
        viewModel.onAction(BasicEmailAction.EmailChanged(email))
        return viewModel.uiState.value
    }

    @Test
    fun `initial state is empty, error-free, not loading and cannot continue`() = runTest {
        viewModel().uiState.test {
            assertThat(awaitItem()).isEqualTo(BasicEmailUiState())
        }
    }

    @Test
    fun `a well-formed email enables continue with no error`() = runTest {
        val state = stateAfterTyping("andrew@example.com")

        assertThat(state.emailError).isNull()
        assertThat(state.isContinueEnabled).isTrue()
    }

    @Test
    fun `a blank email is rejected with a UiText error`() = runTest {
        val state = stateAfterTyping("")

        assertThat(state.emailError).isInstanceOf(UiText::class.java)
        assertThat(state.isContinueEnabled).isFalse()
    }

    @Test
    fun `a whitespace-only email is rejected`() = runTest {
        val state = stateAfterTyping("   ")

        assertThat(state.emailError).isInstanceOf(UiText::class.java)
        assertThat(state.isContinueEnabled).isFalse()
    }

    @Test
    fun `leading and trailing whitespace is trimmed rather than rejected`() = runTest {
        val state = stateAfterTyping("  andrew@example.com  ")

        assertThat(state.emailError).isNull()
        assertThat(state.isContinueEnabled).isTrue()
    }

    @Test
    fun `an email without an at sign is rejected`() = runTest {
        val state = stateAfterTyping("andrew.example.com")

        assertThat(state.emailError).isInstanceOf(UiText::class.java)
    }

    @Test
    fun `an email without a TLD is rejected`() = runTest {
        val state = stateAfterTyping("andrew@example")

        assertThat(state.emailError).isInstanceOf(UiText::class.java)
    }

    @Test
    fun `a single-character TLD is rejected by the existing regex`() = runTest {
        val state = stateAfterTyping("andrew@example.c")

        assertThat(state.emailError).isInstanceOf(UiText::class.java)
    }

    @Test
    fun `a unicode local part is accepted`() = runTest {
        val state = stateAfterTyping("andré@example.com")

        assertThat(state.emailError).isNull()
    }

    @Test
    fun `the error clears as soon as the user retypes a valid address`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(BasicEmailAction.EmailChanged("nope"))
        viewModel.onAction(BasicEmailAction.EmailChanged("andrew@example.com"))

        assertThat(viewModel.uiState.value.emailError).isNull()
    }

    @Test
    fun `the typed email is echoed back into state so the field stays controlled`() = runTest {
        val state = stateAfterTyping("part")

        assertThat(state.email).isEqualTo("part")
    }

    @Test
    fun `ContinueClicked with a valid email emits NavigateToPassword carrying that email`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(BasicEmailAction.EmailChanged("andrew@example.com"))

            viewModel.events.test {
                viewModel.onAction(BasicEmailAction.ContinueClicked)

                assertThat(awaitItem())
                    .isEqualTo(AuthEvent.NavigateToPassword("andrew@example.com"))
            }
        }

    @Test
    fun `ContinueClicked with an invalid email does not navigate`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(BasicEmailAction.EmailChanged("nope"))

        viewModel.events.test {
            viewModel.onAction(BasicEmailAction.ContinueClicked)

            expectNoEvents()
        }
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(BasicEmailAction.BackClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateBack)
        }
    }

    @Test
    fun `a navigation event is emitted exactly once and not replayed to a new collector`() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(BasicEmailAction.EmailChanged("andrew@example.com"))

            viewModel.events.test {
                viewModel.onAction(BasicEmailAction.ContinueClicked)
                awaitItem()
            }

            viewModel.events.test {
                expectNoEvents()
            }
        }

    @Test
    fun `a burst of Continue taps emits NavigateToPassword exactly once`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(BasicEmailAction.EmailChanged("andrew@example.com"))

        viewModel.events.test {
            viewModel.onAction(BasicEmailAction.ContinueClicked)
            viewModel.onAction(BasicEmailAction.ContinueClicked)
            viewModel.onAction(BasicEmailAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToPassword("andrew@example.com"))
            expectNoEvents()
        }
    }

    @Test
    fun `coming back to the screen re-arms Continue`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(BasicEmailAction.EmailChanged("andrew@example.com"))

        viewModel.events.test {
            viewModel.onAction(BasicEmailAction.ContinueClicked)
            awaitItem()
        }

        // A fresh collector is the screen being re-entered after a back press.
        viewModel.events.test {
            viewModel.onAction(BasicEmailAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToPassword("andrew@example.com"))
        }
    }

    @Test
    fun `loading is never left switched on once the action completes`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(BasicEmailAction.EmailChanged("andrew@example.com"))

        viewModel.onAction(BasicEmailAction.ContinueClicked)

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}
