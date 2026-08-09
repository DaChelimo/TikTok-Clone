/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.core.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeUiState(
    val feed: ImmutableList<FeedItemUi> = persistentListOf(),
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    /** Non-null while the comment sheet is open. */
    val commentSheet: CommentSheetUiState? = null,
)

/**
 * One feed page. Author fields and like/follow state are null/default until their per-video
 * enrichment fetches land — the page renders immediately with the video and fills in.
 */
data class FeedItemUi(
    val video: RemoteVideo,
    val authorUsername: String? = null,
    val authorProfilePictureUrl: String? = null,
    val isLiked: Boolean = false,
    val likeCount: Long = 0,
    /**
     * Hidden (false) until the follow check confirms this author is followable — same
     * default as the legacy `isFollowingAuthor = true` binding, which also hid the button
     * until proven otherwise.
     */
    val showFollowButton: Boolean = false,
)

data class CommentSheetUiState(
    val videoId: String,
    val comments: ImmutableList<CommentUi> = persistentListOf(),
    val commentCount: Int = 0,
    val draft: String = "",
)

data class CommentUi(
    val comment: Comment,
    val authorUsername: String? = null,
    val authorProfilePictureUrl: String? = null,
    val isLiked: Boolean = false,
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

/** Test/preview tags for the nodes this screen must expose. */
object HomeScreenTags {
    const val FEED = "home_feed"
    const val LIKE = "home_like"
    const val OPEN_COMMENTS = "home_open_comments"
    const val SHARE = "home_share"
    const val FOLLOW = "home_follow"
    const val AUTHOR_ICON = "home_author_icon"
    const val COMMENT_SHEET = "home_comment_sheet"
    const val COMMENT_INPUT = "home_comment_input"
    const val SEND_COMMENT = "home_send_comment"
}
