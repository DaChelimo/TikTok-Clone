/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.auth.usecase.PasswordError
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.auth.usecase.SignUpCredentials
import com.andre_max.tiktokclone.domain.auth.usecase.ValidatePasswordUseCase
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.andre_max.tiktokclone.test_utils.FakeAuthRepo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/** Screen 3 (spec §4.3). */
@ExperimentalCoroutinesApi
class EnterPasswordViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    /**
     * [identity] is the email local-part, which the ViewModel now reads out of
     * [SignUpSession] rather than taking as a property — so the test seeds the session with
     * an address whose local-part is the identity under test.
     */
    private val authRepo = FakeAuthRepo()

    private fun viewModel(
        identity: String? = null,
        isLogin: Boolean = false,
        session: SignUpSession = SignUpSession(),
    ): EnterPasswordViewModel {
        identity?.let { session.setEmail("$it@example.com") }
        return EnterPasswordViewModel(
            validatePassword = ValidatePasswordUseCase(),
            signUpSession = session,
            authRepo = authRepo,
            savedStateHandle = SavedStateHandle(mapOf("isLogIn" to isLogin)),
        )
    }

    @Test
    fun `initial state is empty, unedited, hidden and cannot continue`() = runTest {
        viewModel().uiState.test {
            assertThat(awaitItem()).isEqualTo(EnterPasswordUiState())
        }
    }

    @Test
    fun `no rules are shown as failed before the first keystroke`() = runTest {
        val viewModel = viewModel()

        val state = viewModel.uiState.value

        assertThat(state.hasBeenEdited).isFalse()
        assertThat(state.failedRules).isEmpty()
    }

    @Test
    fun `the first keystroke marks the field as edited so the checklist can turn red`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(EnterPasswordAction.PasswordChanged("a"))

            assertThat(viewModel.uiState.value.hasBeenEdited).isTrue()
        }

    @Test
    fun `every violated rule is published, not just the first`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(EnterPasswordAction.PasswordChanged("pw"))

        assertThat(viewModel.uiState.value.failedRules).containsAtLeast(
            PasswordError.TooShort,
            PasswordError.NoDigit,
        )
    }

    @Test
    fun `a valid password empties the rule list and enables continue`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(EnterPasswordAction.PasswordChanged("Passw0rd!"))

        val state = viewModel.uiState.value
        assertThat(state.failedRules).isEmpty()
        assertThat(state.isContinueEnabled).isTrue()
    }

    @Test
    fun `a password containing the identity from the previous screen is rejected`() = runTest {
        val viewModel = viewModel(identity = "andrew")

        viewModel.onAction(EnterPasswordAction.PasswordChanged("xAndrewx1!"))

        val state = viewModel.uiState.value
        assertThat(state.failedRules).contains(PasswordError.ContainsIdentity)
        assertThat(state.isContinueEnabled).isFalse()
    }

    @Test
    fun `TogglePasswordVisibility flips visibility without touching the password`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(EnterPasswordAction.PasswordChanged("Passw0rd!"))

        viewModel.onAction(EnterPasswordAction.TogglePasswordVisibility)

        val state = viewModel.uiState.value
        assertThat(state.isPasswordVisible).isTrue()
        assertThat(state.password).isEqualTo("Passw0rd!")
    }

    @Test
    fun `TogglePasswordVisibility twice returns to hidden`() = runTest {
        val viewModel = viewModel()

        viewModel.onAction(EnterPasswordAction.TogglePasswordVisibility)
        viewModel.onAction(EnterPasswordAction.TogglePasswordVisibility)

        assertThat(viewModel.uiState.value.isPasswordVisible).isFalse()
    }

    @Test
    fun `ContinueClicked with a valid password emits NavigateToUsername`() = runTest {
        val viewModel = viewModel(identity = "andrew")
        viewModel.onAction(EnterPasswordAction.PasswordChanged("Passw0rd!"))

        viewModel.events.test {
            viewModel.onAction(EnterPasswordAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToUsername)
        }
    }

    @Test
    fun `ContinueClicked hands the credentials to the session for the username screen`() = runTest {
        val session = SignUpSession().apply { setEmail("andrew@example.com") }
        val viewModel = viewModel(session = session)
        viewModel.onAction(EnterPasswordAction.PasswordChanged("Passw0rd!"))

        viewModel.onAction(EnterPasswordAction.ContinueClicked)

        assertThat(session.credentials)
            .isEqualTo(SignUpCredentials.Email("andrew@example.com", "Passw0rd!"))
    }

    @Test
    fun `ContinueClicked without an email from the previous screen goes back instead`() = runTest {
        // Reachable via a deep link or a restored back stack. Carrying a password forward with
        // no email would hand CompleteSignUpUseCase a credential it cannot act on.
        val viewModel = viewModel()
        viewModel.onAction(EnterPasswordAction.PasswordChanged("Passw0rd!"))

        viewModel.events.test {
            viewModel.onAction(EnterPasswordAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateBack)
        }
    }

    @Test
    fun `ContinueClicked with a failing password does not navigate`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(EnterPasswordAction.PasswordChanged("pw"))

        viewModel.events.test {
            viewModel.onAction(EnterPasswordAction.ContinueClicked)

            expectNoEvents()
        }
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(EnterPasswordAction.BackClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateBack)
        }
    }

    @Test
    fun `loading is left switched off once the action completes`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(EnterPasswordAction.PasswordChanged("Passw0rd!"))

        viewModel.onAction(EnterPasswordAction.ContinueClicked)

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `a navigation event is emitted exactly once and not replayed to a new collector`() =
        runTest {
            val viewModel = viewModel(identity = "andrew")
            viewModel.onAction(EnterPasswordAction.PasswordChanged("Passw0rd!"))

            viewModel.events.test {
                viewModel.onAction(EnterPasswordAction.ContinueClicked)
                awaitItem()
            }

            viewModel.events.test {
                expectNoEvents()
            }
        }

    // region log-in mode

    @Test
    fun `log-in does not impose the sign-up password rules`() = runTest {
        // An account created before these rules existed must still be able to log in; enforcing
        // today's rules on the log-in field would lock out the earliest users.
        val viewModel = viewModel(identity = "andrew", isLogin = true)

        viewModel.onAction(EnterPasswordAction.PasswordChanged("old"))

        val state = viewModel.uiState.value
        assertThat(state.failedRules).isEmpty()
        assertThat(state.isContinueEnabled).isTrue()
    }

    @Test
    fun `log-in still requires something to be typed`() = runTest {
        val viewModel = viewModel(identity = "andrew", isLogin = true)

        viewModel.onAction(EnterPasswordAction.PasswordChanged(""))

        assertThat(viewModel.uiState.value.isContinueEnabled).isFalse()
    }

    @Test
    fun `a successful log-in signs in and goes to the profile`() = runTest {
        val viewModel = viewModel(identity = "andrew", isLogin = true)
        viewModel.onAction(EnterPasswordAction.PasswordChanged("old"))

        viewModel.events.test {
            viewModel.onAction(EnterPasswordAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToHome)
        }
        assertThat(authRepo.calls)
            .containsExactly(FakeAuthRepo.Call.SignInWithEmail("andrew@example.com", "old"))
    }

    @Test
    fun `a rejected credential shows an error and does not navigate`() = runTest {
        authRepo.signInError = DataError.Auth.INVALID_CREDENTIALS
        val viewModel = viewModel(identity = "andrew", isLogin = true)
        viewModel.onAction(EnterPasswordAction.PasswordChanged("wrong"))

        viewModel.events.test {
            viewModel.onAction(EnterPasswordAction.ContinueClicked)

            assertThat(awaitItem()).isInstanceOf(AuthEvent.ShowError::class.java)
        }
    }

    @Test
    fun `log-in never signs up`() = runTest {
        val viewModel = viewModel(identity = "andrew", isLogin = true)
        viewModel.onAction(EnterPasswordAction.PasswordChanged("old"))

        viewModel.onAction(EnterPasswordAction.ContinueClicked)

        assertThat(authRepo.calls).doesNotContain(
            FakeAuthRepo.Call.SignUpWithEmail("andrew@example.com", "old"),
        )
    }

    // endregion
}
