/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.profile

import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileUiState(
    val user: User? = null,
    val isOwnProfile: Boolean = false,
    /** Which collections the viewer may see, in tab order — the visibility rule made visible. */
    val tabs: ImmutableList<ProfileTabUi> = persistentListOf(),
    val showSignOutDialog: Boolean = false,
)

data class ProfileTabUi(
    val videoType: VideoType,
    val videos: ImmutableList<RemoteVideo> = persistentListOf(),
    val isLoading: Boolean = true,
)

sealed interface ProfileAction {
    data class VideoClicked(val video: RemoteVideo) : ProfileAction
    data object SettingsClicked : ProfileAction
    data object SignOutClicked : ProfileAction
    data object SignOutConfirmed : ProfileAction
    data object SignOutDismissed : ProfileAction
}

sealed interface ProfileEvent {
    data class NavigateToVideo(val video: RemoteVideo) : ProfileEvent
    data object NavigateToSettings : ProfileEvent
    data object SignedOut : ProfileEvent
}

/** Test/preview tags for the nodes this screen must expose. */
object ProfileScreenTags {
    const val MENU = "profile_menu"
    const val MENU_SETTINGS = "profile_menu_settings"
    const val MENU_SIGN_OUT = "profile_menu_sign_out"
    const val EDIT_PROFILE = "profile_edit"
    const val TAB_ROW = "profile_tab_row"
    const val VIDEO_GRID = "profile_video_grid"
    const val SIGN_OUT_CONFIRM = "profile_sign_out_confirm"
}
