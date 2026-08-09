/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import com.andre_max.tiktokclone.models.upload.Progress
import com.andre_max.tiktokclone.presentation.core.UiText

data class PostVideoUiState(
    /** The local file being posted — for the thumbnail. Null only if the nav arg is missing. */
    val videoFilePath: String? = null,
    val description: String = "",
    val uploadStatus: Progress = Progress.IDLE,
)

sealed interface PostVideoAction {
    data class DescriptionChanged(val text: String) : PostVideoAction
    data object PostClicked : PostVideoAction
}

sealed interface PostVideoEvent {
    data object NavigateHome : PostVideoEvent
    data class ShowMessage(val message: UiText) : PostVideoEvent
}

/** Test/preview tags for the nodes this screen must expose. */
object PostVideoScreenTags {
    const val DESCRIPTION = "post_video_description"
    const val POST = "post_video_post"
}
