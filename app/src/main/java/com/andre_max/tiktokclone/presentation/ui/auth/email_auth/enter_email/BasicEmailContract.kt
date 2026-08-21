/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email

import com.andre_max.tiktokclone.presentation.core.UiText

/**
 * Screen 2's state contract (spec §4.2).
 *
 * Navigation is deliberately absent: it is a one-shot `AuthEvent`, not a `shouldNavigate`
 * boolean the screen has to remember to reset.
 */
data class BasicEmailUiState(
    val email: String = "",
    val emailError: UiText? = null,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
)

sealed interface BasicEmailAction {
    data class EmailChanged(val value: String) : BasicEmailAction
    data object ContinueClicked : BasicEmailAction

    /**
     * Pinned by spec §4.2 and handled by the ViewModel, but *not* emitted while this screen is a
     * `ViewPager2` page: the back arrow belongs to the host (`SelectBasicAuthFragment`), which
     * already renders one above the tabs. It gets its emitter back with screen 6 (spec §4.6).
     */
    data object BackClicked : BasicEmailAction
}

/** Test/preview tags for the nodes this screen must expose. */
object BasicEmailScreenTags {
    const val EMAIL_FIELD = "basic_email_field"
    const val ERROR = "basic_email_error"
    const val CONTINUE = "basic_email_continue"
    const val LOADING = "basic_email_loading"
}
