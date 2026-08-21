/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.username

import com.andre_max.tiktokclone.presentation.core.UiText

/**
 * Screen 4's state contract.
 *
 * Lives in a new package because the legacy `create_username` package still holds the
 * `CreateUsernameViewModel` that `fragment_create_username.xml` data-binds to; Pass 2 deletes
 * that pair and this becomes the only one (non-negotiable #2).
 */
data class CreateUsernameUiState(
    val username: String = "",
    val error: UiText? = null,
    /** True while the debounced availability lookup is in flight. */
    val isCheckingAvailability: Boolean = false,
    /** Null until a check has completed for the current input. */
    val isAvailable: Boolean? = null,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
)

sealed interface CreateUsernameAction {
    data class UsernameChanged(val value: String) : CreateUsernameAction
    data object ContinueClicked : CreateUsernameAction
    data object BackClicked : CreateUsernameAction
}

/** Test/preview tags for the nodes this screen must expose. */
object CreateUsernameScreenTags {
    const val USERNAME_FIELD = "create_username_field"
    const val ERROR = "create_username_error"
    const val AVAILABLE = "create_username_available"
    const val CHECKING = "create_username_checking"
    const val CONTINUE = "create_username_continue"
    const val BACK = "create_username_back"
}
