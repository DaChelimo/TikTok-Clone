/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.sign_up

import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.auth.usecase.SignUpCredentials
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/** Screen 7 — the sign-up landing. */
@ExperimentalCoroutinesApi
class SignUpViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val signUpSession = SignUpSession()

    private fun viewModel(
        googleWebClientId: String? = "585896630733-abc.apps.googleusercontent.com",
        isFacebookEnabled: Boolean = false,
    ) = SignUpViewModel(signUpSession, googleWebClientId, isFacebookEnabled)

    @Test
    fun `google is offered only when a usable web client id is configured`() = runTest {
        assertThat(viewModel().uiState.value.isGoogleEnabled).isTrue()
        assertThat(viewModel(googleWebClientId = null).uiState.value.isGoogleEnabled).isFalse()
    }

    @Test
    fun `facebook is off`() = runTest {
        // Pinned rather than assumed: enabling it needs a Meta console client token that this
        // repository does not have, so a green build must not imply the button works.
        assertThat(viewModel().uiState.value.isFacebookEnabled).isFalse()
    }

    @Test
    fun `clicking google asks the host to launch the chooser`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(SignUpAction.GoogleClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.LaunchGoogleSignIn)
        }
    }

    @Test
    fun `clicking google does nothing when it is not configured`() = runTest {
        val viewModel = viewModel(googleWebClientId = null)

        viewModel.events.test {
            viewModel.onAction(SignUpAction.GoogleClicked)

            expectNoEvents()
        }
    }

    @Test
    fun `a google token is parked in the session rather than creating an account here`() =
        runTest {
            // Every path creates its account on the create-username screen; this one must not be
            // the exception, or an abandoned sign-up leaves a credentialed account behind.
            val viewModel = viewModel()

            viewModel.onAction(SignUpAction.GoogleTokenReceived("id-token"))

            assertThat(signUpSession.credentials)
                .isEqualTo(SignUpCredentials.Google("id-token"))
        }

    @Test
    fun `a google token navigates on to picking a username`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(SignUpAction.GoogleTokenReceived("id-token"))

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToUsername)
        }
    }

    @Test
    fun `a cancelled google chooser neither navigates nor errors`() = runTest {
        val viewModel = viewModel()
        viewModel.onAction(SignUpAction.GoogleClicked)

        viewModel.events.test {
            awaitItem() // LaunchGoogleSignIn
            viewModel.onAction(SignUpAction.GoogleTokenReceived(null))

            expectNoEvents()
        }
        assertThat(viewModel.uiState.value.isSigningIn).isFalse()
        assertThat(signUpSession.credentials).isNull()
    }

    @Test
    fun `UsePhoneOrEmail goes to the age gate`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(SignUpAction.UsePhoneOrEmailClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToAge)
        }
    }

    @Test
    fun `LogIn goes to the log-in screen`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(SignUpAction.LogInClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToLogIn)
        }
    }
}
