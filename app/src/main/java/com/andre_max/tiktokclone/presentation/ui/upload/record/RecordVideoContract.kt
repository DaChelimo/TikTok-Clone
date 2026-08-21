/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.record

import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.presentation.core.UiText

data class RecordVideoUiState(
    val hasRecordingStarted: Boolean = false,
    val isRecording: Boolean = false,
    val showLittleSpace: Boolean = false,
)

sealed interface RecordVideoEvent {
    data class ShowMessage(val message: UiText) : RecordVideoEvent
    data class NavigateToPreview(val localVideo: LocalVideo) : RecordVideoEvent
}

/** Test/preview tags for the nodes this screen must expose. */
object RecordVideoTags {
    const val RECORD = "record_video_record"
    const val PAUSE = "record_video_pause"
    const val FINISH = "record_video_finish"
    const val CLOSE = "record_video_close"
    const val UPLOAD = "record_video_upload"
    const val GRANT_PERMISSIONS = "record_video_grant_permissions"
}
