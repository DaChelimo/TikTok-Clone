/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.log_in

data class LogInUiState(
    val isGoogleEnabled: Boolean = false,
    val isFacebookEnabled: Boolean = false,
    val isSigningIn: Boolean = false,
)

sealed interface LogInAction {
    data object UsePhoneOrEmailClicked : LogInAction
    data object GoogleClicked : LogInAction
    data object SignUpClicked : LogInAction
    data object CloseClicked : LogInAction
    data class GoogleTokenReceived(val idToken: String?) : LogInAction
}

/** Test/preview tags for the nodes this screen must expose. */
object LogInScreenTags {
    const val CLOSE = "log_in_close"
    const val USE_PHONE_OR_EMAIL = "log_in_use_phone_or_email"
    const val GOOGLE = "log_in_google"
    const val FACEBOOK = "log_in_facebook"
    const val SIGN_UP = "log_in_sign_up"
}
