/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.components.video

import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.video.RemoteVideo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Shared UI models and actions for a full-screen video page — used by the home feed's pager
 * and the single-video detail screen (large_video). Lives in `components` so neither feature
 * reaches into the other's package.
 */

/**
 * One full-screen video. Author fields and like/follow state are null/default until their
 * per-video enrichment fetches land — the page renders immediately and fills in.
 */
data class VideoItemUi(
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

/** Gestures on the video page itself (overlay + action rail). */
sealed interface VideoPageAction {
    data class LikeToggled(val videoId: String) : VideoPageAction
    data class FollowClicked(val authorUid: String) : VideoPageAction
    data class AuthorClicked(val authorUid: String) : VideoPageAction
    data class OpenComments(val videoId: String) : VideoPageAction
}

/** Gestures inside the comment sheet. */
sealed interface CommentSheetAction {
    data object Close : CommentSheetAction
    data class DraftChanged(val text: String) : CommentSheetAction
    data object Send : CommentSheetAction
    data class CommentLikeToggled(val commentId: String) : CommentSheetAction
}

/** Test/preview tags for the nodes a video page must expose. */
object VideoPageTags {
    const val LIKE = "video_page_like"
    const val OPEN_COMMENTS = "video_page_open_comments"
    const val SHARE = "video_page_share"
    const val FOLLOW = "video_page_follow"
    const val AUTHOR_ICON = "video_page_author_icon"
    const val COMMENT_SHEET = "video_page_comment_sheet"
    const val COMMENT_INPUT = "video_page_comment_input"
    const val SEND_COMMENT = "video_page_send_comment"
}
