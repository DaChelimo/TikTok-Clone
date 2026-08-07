/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

import com.andre_max.tiktokclone.domain.auth.usecase.PasswordError
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Screen 3's state contract (spec §4.3).
 *
 * There is no single `error` string: the UI renders a **live rule checklist** — every rule
 * always visible, each one ticked or unticked — which is what returning every failed rule (D6)
 * buys.
 *
 * Lives in a new package alongside the legacy `email_auth/enter_password` one, which stays
 * wired to `fragment_create_password.xml` until Pass 2 deletes it (non-negotiable #2).
 */
data class EnterPasswordUiState(
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val failedRules: ImmutableList<PasswordError> = persistentListOf(),
    /** Don't show red rules before the first keystroke. */
    val hasBeenEdited: Boolean = false,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
)

sealed interface EnterPasswordAction {
    data class PasswordChanged(val value: String) : EnterPasswordAction
    data object TogglePasswordVisibility : EnterPasswordAction
    data object ContinueClicked : EnterPasswordAction
    data object BackClicked : EnterPasswordAction
}

/** Test/preview tags for the nodes this screen must expose. */
object EnterPasswordScreenTags {
    const val PASSWORD_FIELD = "enter_password_field"
    const val VISIBILITY_TOGGLE = "enter_password_visibility_toggle"
    const val CONTINUE = "enter_password_continue"
    const val BACK = "enter_password_back"
    const val LOADING = "enter_password_loading"

    /** One node per rule, tagged `rule_<PasswordError simple name>`. */
    fun ruleTag(error: PasswordError): String = "rule_${error::class.simpleName}"
}
