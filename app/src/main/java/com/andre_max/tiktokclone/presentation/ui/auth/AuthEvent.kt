/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth

import com.andre_max.tiktokclone.presentation.core.UiText

/**
 * A one-shot navigation/side-effect event emitted by an auth ViewModel over a `Channel`, as
 * opposed to a boolean flag living in `UiState` that a screen has to remember to reset.
 */
sealed interface AuthEvent {
    data class NavigateToPassword(val email: String) : AuthEvent
    data object NavigateBack : AuthEvent
    data class ShowError(val message: UiText) : AuthEvent

    /** Age gate cleared — on to the phone/email tab screen. */
    data object NavigateToSelectAuth : AuthEvent

    /** Password accepted — on to picking a username. */
    data object NavigateToUsername : AuthEvent

    /** Verification SMS requested — on to entering the code. */
    data class NavigateToVerificationCode(val verificationId: String) : AuthEvent

    /** Account fully created. */
    data object NavigateToHome : AuthEvent

    /** Sign-up landing — the user chose phone/email over a social provider. */
    data object NavigateToAge : AuthEvent

    /** Sign-up landing — the user already has an account. */
    data object NavigateToLogIn : AuthEvent

    /**
     * Ask the host to start the Google sign-in Activity. An event rather than something the
     * ViewModel does itself: launching it needs an `ActivityResultLauncher`, which only the
     * composition owns.
     */
    data object LaunchGoogleSignIn : AuthEvent
}
