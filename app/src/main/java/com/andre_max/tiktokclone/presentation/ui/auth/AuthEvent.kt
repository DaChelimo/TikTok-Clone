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

    /**
     * Verification SMS requested. The enter-code screen itself is out of scope this phase (D2),
     * so this is where screen 5 ends.
     */
    data class NavigateToVerificationCode(val verificationId: String) : AuthEvent

    /** Account fully created. */
    data object NavigateToHome : AuthEvent
}
