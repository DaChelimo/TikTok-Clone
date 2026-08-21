/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.auth.usecase.SignUpCredentials
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.domain.auth.usecase.ValidatePasswordUseCase
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Screen 3, in both of its modes.
 *
 * **Sign-up** runs the full D6 rule set and hands the credentials to [SignUpSession] for the
 * create-username screen. **Log-in** deliberately does not: an existing account's password was
 * accepted under whatever rules applied when it was chosen, and re-imposing today's stricter
 * ones would lock out exactly the users who signed up earliest. Log-in only requires that
 * something was typed, and lets the identity provider be the judge.
 */
@HiltViewModel
class EnterPasswordViewModel @Inject constructor(
    private val validatePassword: ValidatePasswordUseCase,
    private val signUpSession: SignUpSession,
    private val authRepo: AuthRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val isLogin: Boolean = savedStateHandle["isLogIn"] ?: false

    /**
     * The email local-part collected on screen 2. Read from [SignUpSession] rather than taken as
     * a nav argument so the identity travels the same route the password does.
     */
    private val identity: String?
        get() = signUpSession.email?.substringBefore('@')

    private val _uiState = MutableStateFlow(EnterPasswordUiState())
    val uiState: StateFlow<EnterPasswordUiState> = _uiState.asStateFlow()

    /** One-shot navigation/error events, backed by a `Channel` — never replayed. */
    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onAction(action: EnterPasswordAction) {
        when (action) {
            is EnterPasswordAction.PasswordChanged -> onPasswordChanged(action.value)
            EnterPasswordAction.TogglePasswordVisibility -> _uiState.update {
                it.copy(isPasswordVisible = !it.isPasswordVisible)
            }

            EnterPasswordAction.ContinueClicked -> onContinueClicked()
            EnterPasswordAction.BackClicked -> _events.trySend(AuthEvent.NavigateBack)
        }
    }

    private fun onPasswordChanged(password: String) {
        if (isLogin) {
            _uiState.update {
                it.copy(
                    password = password,
                    failedRules = persistentListOf(),
                    hasBeenEdited = true,
                    isContinueEnabled = password.isNotEmpty(),
                )
            }
            return
        }

        val failedRules = validatePassword(password, identity)
        _uiState.update {
            it.copy(
                password = password,
                failedRules = failedRules.toImmutableList(),
                hasBeenEdited = true,
                isContinueEnabled = failedRules.isEmpty(),
            )
        }
    }

    /**
     * Re-runs the rules against the stored password rather than trusting
     * [EnterPasswordUiState.isContinueEnabled]: a gate enforced only by a disabled button is a
     * client-side gate, and this one is reachable again after process death restores the field.
     */
    private fun onContinueClicked() {
        if (_uiState.value.isLoading) return
        if (isLogin) {
            onLogInClicked()
            return
        }

        val failedRules = validatePassword(_uiState.value.password, identity)

        if (failedRules.isEmpty()) {
            val email = signUpSession.email
            if (email == null) {
                // Reachable only if this screen is entered without screen 2 having run — a
                // deep link or a restored back stack. Bounce rather than carry a half-built
                // credential forward to a use case that cannot act on it.
                _events.trySend(AuthEvent.NavigateBack)
                return
            }

            signUpSession.setCredentials(
                SignUpCredentials.Email(email = email, password = _uiState.value.password),
            )
            _events.trySend(AuthEvent.NavigateToUsername)
        } else {
            _uiState.update {
                it.copy(
                    failedRules = failedRules.toImmutableList(),
                    hasBeenEdited = true,
                    isContinueEnabled = false,
                )
            }
        }
    }

    /**
     * Wrong password and unknown account are indistinguishable here by design — the data layer
     * collapses both into [DataError.Auth.INVALID_CREDENTIALS], so this screen cannot tell a
     * user (or an attacker enumerating addresses) which of the two it was.
     */
    private fun onLogInClicked() {
        val email = signUpSession.email
        val password = _uiState.value.password
        if (email == null || password.isEmpty()) {
            _events.trySend(AuthEvent.NavigateBack)
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = authRepo.signInWithEmail(email, password)
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> {
                    signUpSession.clear()
                    _events.trySend(AuthEvent.NavigateToHome)
                }

                is Result.Error -> _events.trySend(AuthEvent.ShowError(logInErrorText(result.error)))
            }
        }
    }

    private fun logInErrorText(error: DataError.Auth): UiText = when (error) {
        DataError.Auth.NO_INTERNET -> UiText.Resource(R.string.no_network_connection)
        DataError.Auth.TOO_MANY_REQUESTS -> UiText.Resource(R.string.too_many_requests)
        else -> UiText.Resource(R.string.error_occurred_during_log_in)
    }
}
