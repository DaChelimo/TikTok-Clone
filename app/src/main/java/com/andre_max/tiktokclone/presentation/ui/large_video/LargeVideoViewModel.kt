/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.large_video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.comments.CommentsRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.fold
import com.andre_max.tiktokclone.domain.core.onError
import com.andre_max.tiktokclone.domain.user.UserRepo
import com.andre_max.tiktokclone.domain.videos.VideosRepo
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheetAction
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheetUiState
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentUi
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoItemUi
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoPageAction
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single-video detail (opened from profile/tag/search grids), sourced from the `remoteVideo`
 * nav argument.
 *
 * The like/follow/comment handling deliberately mirrors `HomeViewModel` rather than sharing a
 * delegate — this is the second occurrence, and per AGENTS.md §5 the third is the signal to
 * abstract.
 */
@HiltViewModel
class LargeVideoViewModel @Inject constructor(
    private val videosRepo: VideosRepo,
    private val userRepo: UserRepo,
    private val commentsRepo: CommentsRepo,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LargeVideoUiState(isSignedIn = userRepo.isSignedIn()))
    val uiState: StateFlow<LargeVideoUiState> = _uiState.asStateFlow()

    private val _events = Channel<LargeVideoEvent>(Channel.BUFFERED)
    val events: Flow<LargeVideoEvent> = _events.receiveAsFlow()

    private var commentsJob: Job? = null
    private val profileCache = mutableMapOf<String, User?>()

    init {
        savedStateHandle.get<RemoteVideo>(NAV_ARG_REMOTE_VIDEO)?.let { video ->
            _uiState.update {
                it.copy(item = VideoItemUi(video = video, likeCount = video.likes))
            }
            viewModelScope.launch { enrich(video) }
        }
    }

    fun onPageAction(action: VideoPageAction) {
        when (action) {
            is VideoPageAction.LikeToggled -> onLikeToggled()
            is VideoPageAction.FollowClicked -> onFollowClicked(action.authorUid)
            is VideoPageAction.AuthorClicked ->
                _events.trySend(LargeVideoEvent.NavigateToProfile(action.authorUid))

            is VideoPageAction.OpenComments -> onOpenComments()
        }
    }

    fun onSheetAction(action: CommentSheetAction) {
        when (action) {
            CommentSheetAction.Close -> onCloseComments()
            is CommentSheetAction.DraftChanged ->
                _uiState.update { it.copy(commentSheet = it.commentSheet?.copy(draft = action.text)) }

            CommentSheetAction.Send -> onSendComment()
            is CommentSheetAction.CommentLikeToggled -> onCommentLikeToggled(action.commentId)
        }
    }

    private suspend fun enrich(video: RemoteVideo) {
        val author = cachedProfile(video.authorUid)
        updateItem {
            it.copy(
                authorUsername = author?.username,
                authorProfilePictureUrl = author?.profilePictureUrl,
            )
        }

        if (!userRepo.isSignedIn()) return

        val isLiked = videosRepo.isVideoLiked(video.videoId).fold({ it }, { false })
        updateItem { it.copy(isLiked = isLiked) }

        val isOwnVideo = video.authorUid == userRepo.currentUid()
        val isFollowing = isOwnVideo || userRepo.isFollowing(video.authorUid).fold({ it }, { true })
        updateItem { it.copy(showFollowButton = !isFollowing) }
    }

    private fun onLikeToggled() {
        if (!userRepo.isSignedIn()) return
        val item = _uiState.value.item ?: return
        val shouldLike = !item.isLiked

        updateItem {
            it.copy(isLiked = shouldLike, likeCount = it.likeCount + if (shouldLike) 1 else -1)
        }

        viewModelScope.launch {
            videosRepo.likeOrUnlikeVideo(
                videoId = item.video.videoId,
                authorId = item.video.authorUid,
                shouldLike = shouldLike,
            ).onError {
                updateItem {
                    it.copy(
                        isLiked = !shouldLike,
                        likeCount = it.likeCount + if (shouldLike) -1 else 1,
                    )
                }
            }
        }
    }

    private fun onFollowClicked(authorUid: String) {
        if (!userRepo.isSignedIn() || authorUid == userRepo.currentUid()) return

        updateItem { it.copy(showFollowButton = false) }
        viewModelScope.launch {
            userRepo.follow(authorUid).onError {
                updateItem { it.copy(showFollowButton = true) }
            }
        }
    }

    private fun onOpenComments() {
        val video = _uiState.value.item?.video ?: return
        commentsJob?.cancel()

        _uiState.update {
            it.copy(
                commentSheet = CommentSheetUiState(
                    videoId = video.videoId,
                    commentCount = video.totalCommentsSize.coerceAtLeast(0).toInt(),
                ),
            )
        }

        commentsJob = viewModelScope.launch {
            launch {
                commentsRepo.observeComments(video.videoId).collect { comments ->
                    val ui = comments.map { toCommentUi(video.videoId, it) }.toImmutableList()
                    _uiState.update { state ->
                        state.copy(commentSheet = state.commentSheet?.copy(comments = ui))
                    }
                }
            }
            launch {
                commentsRepo.observeCommentCount(video.videoId).collect { count ->
                    _uiState.update { state ->
                        state.copy(commentSheet = state.commentSheet?.copy(commentCount = count))
                    }
                }
            }
        }
    }

    private fun onCloseComments() {
        commentsJob?.cancel()
        commentsJob = null
        _uiState.update { it.copy(commentSheet = null) }
    }

    private suspend fun toCommentUi(videoId: String, comment: Comment): CommentUi {
        val author = cachedProfile(comment.authorUid)
        val isLiked = userRepo.isSignedIn() &&
            commentsRepo.isCommentLiked(videoId, comment.commentId).fold({ it }, { false })
        return CommentUi(
            comment = comment,
            authorUsername = author?.username,
            authorProfilePictureUrl = author?.profilePictureUrl,
            isLiked = isLiked,
        )
    }

    private fun onSendComment() {
        val sheet = _uiState.value.commentSheet ?: return
        if (!userRepo.isSignedIn()) return

        if (sheet.draft.isBlank()) {
            _events.trySend(
                LargeVideoEvent.ShowMessage(UiText.Resource(R.string.empty_comment_error)),
            )
            return
        }

        _uiState.update { it.copy(commentSheet = it.commentSheet?.copy(draft = "")) }
        viewModelScope.launch {
            commentsRepo.sendComment(sheet.videoId, sheet.draft).onError { error ->
                _uiState.update { it.copy(commentSheet = it.commentSheet?.copy(draft = sheet.draft)) }
                _events.trySend(LargeVideoEvent.ShowMessage(errorText(error)))
            }
        }
    }

    private fun onCommentLikeToggled(commentId: String) {
        if (!userRepo.isSignedIn()) return
        val sheet = _uiState.value.commentSheet ?: return

        _uiState.update { state ->
            val current = state.commentSheet ?: return@update state
            state.copy(
                commentSheet = current.copy(
                    comments = current.comments.map {
                        if (it.comment.commentId == commentId) it.copy(isLiked = !it.isLiked) else it
                    }.toImmutableList(),
                ),
            )
        }
        viewModelScope.launch {
            commentsRepo.likeOrUnlikeComment(sheet.videoId, commentId)
        }
    }

    private fun updateItem(transform: (VideoItemUi) -> VideoItemUi) {
        _uiState.update { state ->
            state.copy(item = state.item?.let(transform))
        }
    }

    private suspend fun cachedProfile(uid: String): User? =
        if (uid in profileCache) {
            profileCache[uid]
        } else {
            userRepo.getUserProfile(uid).fold({ it }, { null }).also { profileCache[uid] = it }
        }

    private fun errorText(error: DataError.Network): UiText = when (error) {
        DataError.Network.NO_INTERNET -> UiText.Resource(R.string.no_network_connection)
        else -> UiText.Resource(R.string.error_something_went_wrong)
    }

    companion object {
        /** Must match the argument name in mobile_navigation.xml. */
        const val NAV_ARG_REMOTE_VIDEO = "remoteVideo"
    }
}
