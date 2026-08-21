/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_code

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.PhoneAuthRepo
import com.andre_max.tiktokclone.domain.auth.usecase.SignUpCredentials
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * SMS code entry — the screen that makes phone auth reach an account rather than dead-end.
 *
 * **Sign-up does not verify the code here.** It parks [SignUpCredentials.Phone] in
 * [SignUpSession] and moves on, so the account is still created on the create-username screen
 * like every other path; verifying early would create an account the user might abandon before
 * choosing a name. **Log-in does verify here**, because there is no later screen to do it on.
 */
@HiltViewModel
class EnterCodeViewModel @Inject constructor(
    private val phoneAuthRepo: PhoneAuthRepo,
    private val signUpSession: SignUpSession,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val verificationId: String = savedStateHandle["verificationId"] ?: ""
    private val isLogin: Boolean = savedStateHandle["isLogIn"] ?: false

    private val _uiState = MutableStateFlow(EnterCodeUiState())
    val uiState: StateFlow<EnterCodeUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onAction(action: EnterCodeAction) {
        when (action) {
            is EnterCodeAction.CodeChanged -> onCodeChanged(action.value)
            EnterCodeAction.ContinueClicked -> onContinueClicked()
            EnterCodeAction.BackClicked -> _events.trySend(AuthEvent.NavigateBack)
        }
    }

    /**
     * Non-digits are dropped rather than rejected: an SMS pasted with spaces or a stray dash is
     * a normal thing for a user to do, and the digits are unambiguous.
     */
    private fun onCodeChanged(value: String) {
        val digits = value.filter(Char::isDigit).take(EnterCodeUiState.CODE_LENGTH)
        _uiState.update { it.copy(code = digits, error = null) }
    }

    private fun onContinueClicked() {
        val state = _uiState.value
        if (!state.isContinueEnabled) return

        if (verificationId.isEmpty()) {
            _uiState.update { it.copy(error = UiText.Resource(R.string.code_invalid)) }
            return
        }

        if (!isLogin) {
            signUpSession.setCredentials(SignUpCredentials.Phone(verificationId, state.code))
            _events.trySend(AuthEvent.NavigateToUsername)
            return
        }

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val result = phoneAuthRepo.signInWithCode(verificationId, state.code)
            _uiState.update { it.copy(isSubmitting = false) }

            when (result) {
                is Result.Success -> {
                    signUpSession.clear()
                    _events.trySend(AuthEvent.NavigateToHome)
                }

                is Result.Error -> _uiState.update { it.copy(error = errorText(result.error)) }
            }
        }
    }

    private fun errorText(error: DataError.Auth): UiText = when (error) {
        DataError.Auth.NO_INTERNET -> UiText.Resource(R.string.no_network_connection)
        DataError.Auth.TOO_MANY_REQUESTS -> UiText.Resource(R.string.too_many_requests)
        else -> UiText.Resource(R.string.code_invalid)
    }
}
