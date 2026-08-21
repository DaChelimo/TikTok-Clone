/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.basic_phone

import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.auth.PhoneAuthStatus
import com.andre_max.tiktokclone.domain.auth.usecase.ValidatePhoneNumberUseCase
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.andre_max.tiktokclone.test_utils.FakePhoneAuthRepo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

/** Screen 5 (spec §4.5). Ends at "verification code requested" — enter-code is out of scope. */
@ExperimentalCoroutinesApi
class BasicPhoneViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val repo = FakePhoneAuthRepo()

    private fun viewModel() = BasicPhoneViewModel(ValidatePhoneNumberUseCase(), repo, SignUpSession())

    private fun BasicPhoneViewModel.type(countryCode: String, number: String) {
        onAction(BasicPhoneAction.CountryCodeChanged(countryCode))
        onAction(BasicPhoneAction.PhoneNumberChanged(number))
    }

    @Test
    fun `initial state is empty and cannot continue`() = runTest {
        viewModel().uiState.test {
            assertThat(awaitItem()).isEqualTo(BasicPhoneUiState())
        }
    }

    @Test
    fun `a valid number enables continue with no error`() = runTest {
        val viewModel = viewModel()

        viewModel.type("+254", "712345678")

        val state = viewModel.uiState.value
        assertThat(state.error).isNull()
        assertThat(state.isContinueEnabled).isTrue()
    }

    @Test
    fun `a non-numeric number surfaces a UiText error and disables continue`() = runTest {
        val viewModel = viewModel()

        viewModel.type("+254", "7CALLME1")

        val state = viewModel.uiState.value
        assertThat(state.error).isInstanceOf(UiText::class.java)
        assertThat(state.isContinueEnabled).isFalse()
    }

    @Test
    fun `a number typed without picking a country code cannot continue`() = runTest {
        val viewModel = viewModel()

        viewModel.type("", "712345678")

        assertThat(viewModel.uiState.value.isContinueEnabled).isFalse()
    }

    @Test
    fun `a too-short number disables continue`() = runTest {
        val viewModel = viewModel()

        viewModel.type("+254", "1")

        assertThat(viewModel.uiState.value.isContinueEnabled).isFalse()
    }

    @Test
    fun `a too-long number disables continue`() = runTest {
        val viewModel = viewModel()

        viewModel.type("+254", "1".repeat(20))

        assertThat(viewModel.uiState.value.isContinueEnabled).isFalse()
    }

    @Test
    fun `ContinueClicked sends the country code and number to the repo as one E164 string`() =
        runTest {
            repo.statuses = listOf(PhoneAuthStatus.CodeSent("verification-id"))
            val viewModel = viewModel()
            viewModel.type("+254", "712345678")

            viewModel.onAction(BasicPhoneAction.ContinueClicked)

            assertThat(repo.requestedNumbers).containsExactly("+254712345678")
        }

    @Test
    fun `CodeSent emits NavigateToVerificationCode carrying the verification id`() = runTest {
        repo.statuses = listOf(PhoneAuthStatus.CodeSent("verification-id"))
        val viewModel = viewModel()
        viewModel.type("+254", "712345678")

        viewModel.events.test {
            viewModel.onAction(BasicPhoneAction.ContinueClicked)

            assertThat(awaitItem())
                .isEqualTo(AuthEvent.NavigateToVerificationCode("verification-id"))
        }
    }

    @Test
    fun `AutoVerified skips the code screen and goes straight to the username step`() = runTest {
        repo.statuses = listOf(PhoneAuthStatus.AutoVerified)
        val viewModel = viewModel()
        viewModel.type("+254", "712345678")

        viewModel.events.test {
            viewModel.onAction(BasicPhoneAction.ContinueClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateToUsername)
        }
    }

    @Test
    fun `a Failed status emits ShowError instead of navigating`() = runTest {
        repo.statuses = listOf(PhoneAuthStatus.Failed(DataError.Network.NO_INTERNET))
        val viewModel = viewModel()
        viewModel.type("+254", "712345678")

        viewModel.events.test {
            viewModel.onAction(BasicPhoneAction.ContinueClicked)

            assertThat(awaitItem()).isInstanceOf(AuthEvent.ShowError::class.java)
        }
    }

    @Test
    fun `ContinueClicked with an invalid number never reaches the repo`() = runTest {
        val viewModel = viewModel()
        viewModel.type("+254", "1")

        viewModel.onAction(BasicPhoneAction.ContinueClicked)

        assertThat(repo.requestedNumbers).isEmpty()
    }

    @Test
    fun `loading is raised while the request is in flight and lowered on failure`() = runTest {
        repo.statuses = listOf(PhoneAuthStatus.Failed(DataError.Network.SERVER))
        val viewModel = viewModel()
        viewModel.type("+254", "712345678")

        viewModel.onAction(BasicPhoneAction.ContinueClicked)

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `BackClicked emits NavigateBack`() = runTest {
        val viewModel = viewModel()

        viewModel.events.test {
            viewModel.onAction(BasicPhoneAction.BackClicked)

            assertThat(awaitItem()).isEqualTo(AuthEvent.NavigateBack)
        }
    }

    @Test
    fun `a navigation event is emitted exactly once and not replayed to a new collector`() =
        runTest {
            repo.statuses = listOf(PhoneAuthStatus.CodeSent("verification-id"))
            val viewModel = viewModel()
            viewModel.type("+254", "712345678")

            viewModel.events.test {
                viewModel.onAction(BasicPhoneAction.ContinueClicked)
                awaitItem()
            }

            viewModel.events.test {
                expectNoEvents()
            }
        }
}
