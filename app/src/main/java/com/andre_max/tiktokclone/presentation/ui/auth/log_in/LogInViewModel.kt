/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.log_in

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.repo.di.SocialAuthModule
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The log-in landing.
 *
 * Mirrors `SignUpViewModel` but is not shared with it: Google here *completes* the sign-in
 * (the account and profile already exist, so there is nothing to collect), whereas on sign-up
 * the same token is only parked for the create-username screen. Folding the two together would
 * take a boolean that changes what the class does rather than how it looks.
 */
@HiltViewModel
class LogInViewModel @Inject constructor(
    private val authRepo: AuthRepo,
    @Named(SocialAuthModule.GOOGLE_WEB_CLIENT_ID) val googleWebClientId: String?,
    @Named(SocialAuthModule.IS_FACEBOOK_ENABLED) isFacebookEnabled: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LogInUiState(
            isGoogleEnabled = googleWebClientId != null,
            isFacebookEnabled = isFacebookEnabled,
        ),
    )
    val uiState: StateFlow<LogInUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onAction(action: LogInAction) {
        when (action) {
            LogInAction.UsePhoneOrEmailClicked -> _events.trySend(AuthEvent.NavigateToSelectAuth)
            LogInAction.SignUpClicked, LogInAction.CloseClicked ->
                _events.trySend(AuthEvent.NavigateBack)

            LogInAction.GoogleClicked -> onGoogleClicked()
            is LogInAction.GoogleTokenReceived -> onGoogleTokenReceived(action.idToken)
        }
    }

    private fun onGoogleClicked() {
        if (!_uiState.value.isGoogleEnabled || _uiState.value.isSigningIn) return
        _uiState.update { it.copy(isSigningIn = true) }
        _events.trySend(AuthEvent.LaunchGoogleSignIn)
    }

    private fun onGoogleTokenReceived(idToken: String?) {
        if (idToken == null) {
            _uiState.update { it.copy(isSigningIn = false) }
            return
        }

        viewModelScope.launch {
            val result = authRepo.signInWithGoogle(idToken)
            _uiState.update { it.copy(isSigningIn = false) }

            when (result) {
                is Result.Success -> _events.trySend(AuthEvent.NavigateToHome)
                is Result.Error -> _events.trySend(AuthEvent.ShowError(errorText(result.error)))
            }
        }
    }

    fun onGoogleSignInUnavailable() {
        _uiState.update { it.copy(isSigningIn = false) }
        _events.trySend(AuthEvent.ShowError(UiText.Resource(R.string.error_occurred_during_log_in)))
    }

    private fun errorText(error: DataError.Auth): UiText = when (error) {
        DataError.Auth.NO_INTERNET -> UiText.Resource(R.string.no_network_connection)
        DataError.Auth.TOO_MANY_REQUESTS -> UiText.Resource(R.string.too_many_requests)
        else -> UiText.Resource(R.string.error_occurred_during_log_in)
    }
}
