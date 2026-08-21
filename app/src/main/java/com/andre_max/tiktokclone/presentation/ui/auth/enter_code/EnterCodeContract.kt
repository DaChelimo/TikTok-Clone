/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_code

import com.andre_max.tiktokclone.presentation.core.UiText

data class EnterCodeUiState(
    val code: String = "",
    val error: UiText? = null,
    val isSubmitting: Boolean = false,
) {
    val isContinueEnabled: Boolean get() = code.length == CODE_LENGTH && !isSubmitting

    companion object {
        /** Firebase SMS verification codes are 6 digits. */
        const val CODE_LENGTH = 6
    }
}

sealed interface EnterCodeAction {
    data class CodeChanged(val value: String) : EnterCodeAction
    data object ContinueClicked : EnterCodeAction
    data object BackClicked : EnterCodeAction
}

/** Test/preview tags for the nodes this screen must expose. */
object EnterCodeScreenTags {
    const val CODE_FIELD = "enter_code_field"
    const val ERROR = "enter_code_error"
    const val CONTINUE = "enter_code_continue"
    const val BACK = "enter_code_back"

    /** One per box; suffixed with the digit index. */
    const val BOX_PREFIX = "enter_code_box_"
}
