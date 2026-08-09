/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.large_video

import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheetUiState
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoItemUi

/**
 * The screen's actions are the shared
 * [com.andre_max.tiktokclone.presentation.ui.components.video.VideoPageAction] and
 * [com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheetAction] directly —
 * this screen is one video page plus its sheet, so a third wrapper type would only re-state
 * them.
 */
data class LargeVideoUiState(
    /** Null only when the nav argument is missing — the screen renders empty rather than crash. */
    val item: VideoItemUi? = null,
    val isSignedIn: Boolean = false,
    val commentSheet: CommentSheetUiState? = null,
)

sealed interface LargeVideoEvent {
    data class NavigateToProfile(val uid: String) : LargeVideoEvent
    data class ShowMessage(val message: UiText) : LargeVideoEvent
}
