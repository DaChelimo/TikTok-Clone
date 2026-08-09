/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.sign_up

/**
 * Screen 7's state contract — the sign-up landing.
 *
 * [isGoogleEnabled] and [isFacebookEnabled] are state rather than constants because each
 * provider depends on configuration that is not in this repository (see `SocialAuthModule`);
 * a disabled button plus an explanation is the honest rendering of "not set up yet".
 */
data class SignUpUiState(
    val isGoogleEnabled: Boolean = false,
    val isFacebookEnabled: Boolean = false,
    val isSigningIn: Boolean = false,
)

sealed interface SignUpAction {
    data object UsePhoneOrEmailClicked : SignUpAction
    data object GoogleClicked : SignUpAction
    data object LogInClicked : SignUpAction
    data object CloseClicked : SignUpAction

    /** The ID token handed back by the Google sign-in Activity, or null if it was cancelled. */
    data class GoogleTokenReceived(val idToken: String?) : SignUpAction
}

/** Test/preview tags for the nodes this screen must expose. */
object SignUpScreenTags {
    const val CLOSE = "sign_up_close"
    const val USE_PHONE_OR_EMAIL = "sign_up_use_phone_or_email"
    const val GOOGLE = "sign_up_google"
    const val FACEBOOK = "sign_up_facebook"
    const val LOG_IN = "sign_up_log_in"
}
