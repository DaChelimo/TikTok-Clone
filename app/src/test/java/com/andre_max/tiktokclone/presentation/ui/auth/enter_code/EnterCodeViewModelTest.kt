/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_code

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.auth.usecase.SignUpCredentials
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.andre_max.tiktokclone.test_utils.FakePhoneAuthRepo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/** The SMS-code screen, in both of its modes. */
@ExperimentalCoroutinesApi
class EnterCodeViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val phoneAuthRepo = FakePhoneAuthRepo()
    private val signUpSession = SignUpSession()

    private fun viewModel(
        verificationId: String = "vid",
        isLogin: Boolean = false,
    ) = EnterCodeViewModel(
        phoneAuthRepo = phoneAuthRepo,
        signUpSession = signUpSession,
        savedStateHandle = SavedStateHandle(
            mapOf("verificationId" to verificationId, "isLogIn" to isLogin),
        ),
    )

    private fun EnterCodeViewModel.type(code: String) =
        onAction(EnterCodeAction.CodeChanged(code))

    @Test
    fun `continue stays disabled until all six digits are present`() = runTest {
        val viewModel = viewModel()

        viewModel.type("12345")
        assertThat(viewModel.uiState.value.isContinueEnabled).isFalse()

        viewModel.type("123456")
        assertThat(viewModel.uiState.value.isContinueEnabled).isTrue()
    }

    @Test
    fun `non-digits are dropped rather than rejected`() = runTest {
        // A code pasted out of an SMS often arrives with spaces or a dash around it.
        val viewModel = viewModel()

        viewModel.type("12 34-56")

        assertThat(viewModel.uiState.value.code).isEqualTo("123456")
    }

    @Test
    fun `typing past six digits is ignored`() = runTest {
        val viewModel = viewModel()

        viewModel.type("1234567890")

        assertThat(viewModel.uiState.value.code).isEqualTo("123456")
    }

    @Test
    fun `editing the code clears a previous rejection`() = runTest {
        val viewModel = viewModel(isLogin = true)
        phoneAuthRepo.signInError = DataError.Auth.INVALID_CREDENTIALS
        viewModel.type("000000")
        viewModel.onAction(EnterCodeAction.ContinueClicked)

        viewModel.type("000001")

        assertThat(viewModel.uiState.value.error).isNull()
    }

    @Test
    fun `sign-up parks the code instead of verifying it here`() = runTest {
        // The account is created on the create-username screen for every path, so verifying
        // early would create an account the user might abandon before choosing a name.
        val viewModel = viewModel()
        viewModel.type("123456")

        viewModel.onAction(EnterCodeAction.ContinueClicked)

        assertThat(signUpSession.credentials)
            .isEqualTo(SignUpCredentials.Phone("vid", "123456"))
        assertThat(phoneAuthRepo.submittedCodes).isEmpty()
    }

    @Test
    fun `sign-up navigates on to picking a username`() = runTest {
        val viewModel = viewModel()
        viewModel.type("123456")

        viewModel.events.test {
            viewModel.onAction(EnterCodeAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToUsername)
        }
    }

    @Test
    fun `log-in verifies the code here because there is no later screen to do it`() = runTest {
        val viewModel = viewModel(isLogin = true)
        viewModel.type("123456")

        viewModel.events.test {
            viewModel.onAction(EnterCodeAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToHome)
        }
        assertThat(phoneAuthRepo.submittedCodes).containsExactly("vid" to "123456")
    }

    @Test
    fun `a rejected code on log-in shows an error and does not navigate`() = runTest {
        phoneAuthRepo.signInError = DataError.Auth.INVALID_CREDENTIALS
        val viewModel = viewModel(isLogin = true)
        viewModel.type("000000")

        viewModel.events.test {
            viewModel.onAction(EnterCodeAction.ContinueClicked)

            expectNoEvents()
        }
        assertThat(viewModel.uiState.value.error).isNotNull()
    }

    @Test
    fun `a missing verification id fails closed rather than submitting`() = runTest {
        // Reachable via a deep link or a restored back stack.
        val viewModel = viewModel(verificationId = "", isLogin = true)
        viewModel.type("123456")

        viewModel.onAction(EnterCodeAction.ContinueClicked)

        assertThat(phoneAuthRepo.submittedCodes).isEmpty()
        assertThat(viewModel.uiState.value.error).isNotNull()
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(EnterCodeAction.BackClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateBack)
        }
    }
}
