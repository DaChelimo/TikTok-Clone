/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.basic_phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.PhoneAuthRepo
import com.andre_max.tiktokclone.domain.auth.PhoneAuthStatus
import com.andre_max.tiktokclone.domain.auth.usecase.PhoneNumberError
import com.andre_max.tiktokclone.domain.auth.usecase.ValidatePhoneNumberUseCase
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Screen 5. */
@HiltViewModel
class BasicPhoneViewModel @Inject constructor(
    private val validatePhoneNumber: ValidatePhoneNumberUseCase,
    private val phoneAuthRepo: PhoneAuthRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BasicPhoneUiState())
    val uiState: StateFlow<BasicPhoneUiState> = _uiState.asStateFlow()

    /** One-shot navigation/error events, backed by a `Channel` — never replayed. */
    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onAction(action: BasicPhoneAction) {
        when (action) {
            is BasicPhoneAction.CountryCodeChanged -> onNumberChanged(countryCode = action.value)
            is BasicPhoneAction.PhoneNumberChanged -> onNumberChanged(phoneNumber = action.value)
            BasicPhoneAction.ContinueClicked -> onContinueClicked()
            BasicPhoneAction.BackClicked -> _events.trySend(AuthEvent.NavigateBack)
        }
    }

    private fun onNumberChanged(
        countryCode: String = _uiState.value.countryCode,
        phoneNumber: String = _uiState.value.phoneNumber,
    ) {
        val result = validatePhoneNumber(countryCode + phoneNumber)
        _uiState.update {
            it.copy(
                countryCode = countryCode,
                phoneNumber = phoneNumber,
                error = result.errorText(),
                isContinueEnabled = result is Result.Success,
            )
        }
    }

    private fun onContinueClicked() {
        val state = _uiState.value
        val e164Number = state.countryCode + state.phoneNumber

        // Re-validated here rather than trusting isContinueEnabled: a disabled button is a UI
        // affordance, not a gate, so the rule has to hold in the ViewModel too.
        when (val result = validatePhoneNumber(e164Number)) {
            is Result.Success -> requestVerificationCode(e164Number)
            is Result.Error -> _uiState.update {
                it.copy(error = result.errorText(), isContinueEnabled = false)
            }
        }
    }

    private fun requestVerificationCode(e164Number: String) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            phoneAuthRepo.requestVerificationCode(e164Number).collect { status ->
                _uiState.update { it.copy(isLoading = false) }

                when (status) {
                    is PhoneAuthStatus.CodeSent ->
                        _events.trySend(AuthEvent.NavigateToVerificationCode(status.verificationId))

                    PhoneAuthStatus.AutoVerified -> _events.trySend(AuthEvent.NavigateToUsername)

                    is PhoneAuthStatus.Failed -> _events.trySend(
                        AuthEvent.ShowError(UiText.Resource(R.string.error_occurred_during_sign_up)),
                    )
                }
            }
        }
    }

    private fun Result<Unit, PhoneNumberError>.errorText(): UiText? = when (this) {
        is Result.Success -> null

        is Result.Error -> when (error) {
            PhoneNumberError.Empty -> null
            PhoneNumberError.MissingCountryCode -> UiText.Resource(R.string.enter_valid_phone_number)
            PhoneNumberError.NonNumeric -> UiText.Resource(R.string.enter_valid_phone_number)
            PhoneNumberError.TooShort -> UiText.Resource(R.string.enter_valid_phone_number)
            PhoneNumberError.TooLong -> UiText.Resource(R.string.enter_valid_phone_number)
        }
    }
}
