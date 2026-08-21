/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.sign_up

import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.usecase.SignUpCredentials
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
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

/**
 * Screen 7 — the sign-up landing.
 *
 * Google sign-in stops at the ID token: the account is not created here, it is created on the
 * create-username screen like every other path, so this ViewModel's job ends at putting
 * [SignUpCredentials.Google] into [SignUpSession]. That is what removes the old fragment's
 * `viewModel.googleAuthRepo.doGoogleAuth(...)` reach-through — the Fragment no longer holds a
 * repo at all.
 */
@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpSession: SignUpSession,
    @Named(SocialAuthModule.GOOGLE_WEB_CLIENT_ID) val googleWebClientId: String?,
    @Named(SocialAuthModule.IS_FACEBOOK_ENABLED) isFacebookEnabled: Boolean,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SignUpUiState(
            isGoogleEnabled = googleWebClientId != null,
            isFacebookEnabled = isFacebookEnabled,
        ),
    )
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onAction(action: SignUpAction) {
        when (action) {
            SignUpAction.UsePhoneOrEmailClicked -> _events.trySend(AuthEvent.NavigateToAge)
            SignUpAction.LogInClicked -> _events.trySend(AuthEvent.NavigateToLogIn)
            SignUpAction.CloseClicked -> _events.trySend(AuthEvent.NavigateBack)
            SignUpAction.GoogleClicked -> onGoogleClicked()
            is SignUpAction.GoogleTokenReceived -> onGoogleTokenReceived(action.idToken)
        }
    }

    private fun onGoogleClicked() {
        if (!_uiState.value.isGoogleEnabled) return
        _uiState.update { it.copy(isSigningIn = true) }
        _events.trySend(AuthEvent.LaunchGoogleSignIn)
    }

    private fun onGoogleTokenReceived(idToken: String?) {
        _uiState.update { it.copy(isSigningIn = false) }

        // Null covers both a cancelled chooser and a completed flow that returned no token.
        // Neither is worth an error message: the user either backed out deliberately, or is
        // about to see the same screen and can retry.
        if (idToken == null) return

        signUpSession.setCredentials(SignUpCredentials.Google(idToken))
        _events.trySend(AuthEvent.NavigateToUsername)
    }

    fun onGoogleSignInFailed() {
        _uiState.update { it.copy(isSigningIn = false) }
        _events.trySend(AuthEvent.ShowError(UiText.Resource(R.string.error_occurred_during_sign_up)))
    }
}
