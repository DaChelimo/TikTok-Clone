/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheetUiState
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoItemUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeUiState(
    val feed: ImmutableList<VideoItemUi> = persistentListOf(),
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    /** Non-null while the comment sheet is open. */
    val commentSheet: CommentSheetUiState? = null,
)

sealed interface HomeAction {
    /** The pager settled on [index] — the pagination trigger. */
    data class PageSettled(val index: Int) : HomeAction
    data class LikeToggled(val videoId: String) : HomeAction
    data class FollowClicked(val authorUid: String) : HomeAction
    data class AuthorClicked(val authorUid: String) : HomeAction
    data class OpenComments(val videoId: String) : HomeAction
    data object CloseComments : HomeAction
    data class CommentDraftChanged(val text: String) : HomeAction
    data object SendCommentClicked : HomeAction
    data class CommentLikeToggled(val commentId: String) : HomeAction
}

sealed interface HomeEvent {
    data class NavigateToProfile(val uid: String) : HomeEvent
    data class ShowMessage(val message: UiText) : HomeEvent
}

/**
 * Test/preview tags for the nodes this screen must expose. Per-video-page tags live in
 * [com.andre_max.tiktokclone.presentation.ui.components.video.VideoPageTags].
 */
object HomeScreenTags {
    const val FEED = "home_feed"
    const val SEARCH = "home_search"
}
