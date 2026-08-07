/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import com.andre_max.tiktokclone.presentation.core.UiText
import java.time.LocalDate

data class AgeUiState(
    val dateOfBirth: LocalDate? = null,
    val error: UiText? = null,
    val isContinueEnabled: Boolean = false,
)

sealed interface AgeAction {
    data class DateChanged(val date: LocalDate) : AgeAction
    data object ContinueClicked : AgeAction
    data object BackClicked : AgeAction
}
