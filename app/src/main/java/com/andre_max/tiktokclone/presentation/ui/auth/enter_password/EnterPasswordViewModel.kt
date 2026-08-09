/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.domain.auth.usecase.SignUpCredentials
import com.andre_max.tiktokclone.domain.auth.usecase.ValidatePasswordUseCase
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Screen 3.
 *
 * [identity] is the email local-part (or username) carried in from the previous screen; it is
 * what makes [ValidatePasswordUseCase]'s `ContainsIdentity` rule (D6) possible. Pass 2 wires it
 * from `SavedStateHandle`; it is a constructor parameter here so ViewModel tests stay free of
 * Android nav plumbing.
 */
@HiltViewModel
class EnterPasswordViewModel @Inject constructor(
    private val validatePassword: ValidatePasswordUseCase,
    private val signUpSession: SignUpSession,
) : ViewModel() {

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
}
