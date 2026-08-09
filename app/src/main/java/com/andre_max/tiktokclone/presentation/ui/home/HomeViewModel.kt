/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.comments.CommentsRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.core.fold
import com.andre_max.tiktokclone.domain.core.onError
import com.andre_max.tiktokclone.domain.user.UserRepo
import com.andre_max.tiktokclone.domain.videos.VideosRepo
import com.andre_max.tiktokclone.domain.videos.usecase.GetFeedBatchUseCase
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.core.UiText
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFeedBatch: GetFeedBatchUseCase,
    private val videosRepo: VideosRepo,
    private val userRepo: UserRepo,
    private val commentsRepo: CommentsRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isSignedIn = userRepo.isSignedIn()))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    private var isFetching = false
    private var commentsJob: Job? = null

    /**
     * Authors repeat across videos and comments; one fetch per uid per screen instance. A null
     * value means the profile fetch failed — kept so a dead uid isn't re-fetched per comment.
     */
    private val profileCache = mutableMapOf<String, User?>()

    init {
        fetchNextBatch()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.PageSettled -> onPageSettled(action.index)
            is HomeAction.LikeToggled -> onLikeToggled(action.videoId)
            is HomeAction.FollowClicked -> onFollowClicked(action.authorUid)
            is HomeAction.AuthorClicked ->
                _events.trySend(HomeEvent.NavigateToProfile(action.authorUid))

            is HomeAction.OpenComments -> onOpenComments(action.videoId)
            HomeAction.CloseComments -> onCloseComments()
            is HomeAction.CommentDraftChanged ->
                _uiState.update { it.copy(commentSheet = it.commentSheet?.copy(draft = action.text)) }

            HomeAction.SendCommentClicked -> onSendComment()
            is HomeAction.CommentLikeToggled -> onCommentLikeToggled(action.commentId)
        }
    }

    // region Feed

    /** Same threshold as the legacy scroll listener: fetch once within 5 pages of the end. */
    private fun onPageSettled(index: Int) {
        if (_uiState.value.feed.size - index <= NEAR_END_THRESHOLD) fetchNextBatch()
    }

    private fun fetchNextBatch() {
        if (isFetching) return
        isFetching = true
        _uiState.update { it.copy(isLoading = it.feed.isEmpty()) }

        viewModelScope.launch {
            val seenIds = _uiState.value.feed.map { it.video.videoId }.toSet()

            when (val result = getFeedBatch(excludeIds = seenIds)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        val newItems = result.data.map { video ->
                            FeedItemUi(video = video, likeCount = video.likes)
                        }
                        state.copy(feed = (state.feed + newItems).toImmutableList(), isLoading = false)
                    }
                    result.data.forEach { video ->
                        viewModelScope.launch { enrich(video) }
                    }
                }

                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.trySend(HomeEvent.ShowMessage(errorText(result.error)))
                }
            }
            isFetching = false
        }
    }

    private suspend fun enrich(video: RemoteVideo) {
        val author = cachedProfile(video.authorUid)
        updateFeedItem(video.videoId) {
            it.copy(
                authorUsername = author?.username,
                authorProfilePictureUrl = author?.profilePictureUrl,
            )
        }

        if (!userRepo.isSignedIn()) return

        val isLiked = videosRepo.isVideoLiked(video.videoId).fold({ it }, { false })
        updateFeedItem(video.videoId) { it.copy(isLiked = isLiked) }

        val isOwnVideo = video.authorUid == userRepo.currentUid()
        val isFollowing = isOwnVideo || userRepo.isFollowing(video.authorUid).fold({ it }, { true })
        updateFeedItem(video.videoId) { it.copy(showFollowButton = !isFollowing) }
    }

    private fun onLikeToggled(videoId: String) {
        if (!userRepo.isSignedIn()) return // Legacy parity: signed-out taps do nothing.
        val item = _uiState.value.feed.find { it.video.videoId == videoId } ?: return
        val shouldLike = !item.isLiked

        updateFeedItem(videoId) {
            it.copy(isLiked = shouldLike, likeCount = it.likeCount + if (shouldLike) 1 else -1)
        }

        viewModelScope.launch {
            videosRepo.likeOrUnlikeVideo(videoId, item.video.authorUid, shouldLike).onError {
                updateFeedItem(videoId) {
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

        setFollowButtonVisible(authorUid, visible = false)
        viewModelScope.launch {
            userRepo.follow(authorUid).onError {
                setFollowButtonVisible(authorUid, visible = true)
            }
        }
    }

    private fun setFollowButtonVisible(authorUid: String, visible: Boolean) {
        _uiState.update { state ->
            state.copy(
                feed = state.feed.map {
                    if (it.video.authorUid == authorUid) it.copy(showFollowButton = visible) else it
                }.toImmutableList(),
            )
        }
    }

    private fun updateFeedItem(videoId: String, transform: (FeedItemUi) -> FeedItemUi) {
        _uiState.update { state ->
            state.copy(
                feed = state.feed.map {
                    if (it.video.videoId == videoId) transform(it) else it
                }.toImmutableList(),
            )
        }
    }

    // endregion

    // region Comments

    private fun onOpenComments(videoId: String) {
        val video = _uiState.value.feed.find { it.video.videoId == videoId }?.video ?: return
        commentsJob?.cancel()

        _uiState.update {
            it.copy(
                commentSheet = CommentSheetUiState(
                    videoId = videoId,
                    commentCount = video.totalCommentsSize.coerceAtLeast(0).toInt(),
                ),
            )
        }

        commentsJob = viewModelScope.launch {
            launch {
                commentsRepo.observeComments(videoId).collect { comments ->
                    val ui = comments.map { toCommentUi(videoId, it) }.toImmutableList()
                    updateSheet(videoId) { it.copy(comments = ui) }
                }
            }
            launch {
                commentsRepo.observeCommentCount(videoId).collect { count ->
                    updateSheet(videoId) { it.copy(commentCount = count) }
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
        if (!userRepo.isSignedIn()) return // Legacy parity: prompt-to-sign-up is still a TODO.

        if (sheet.draft.isBlank()) {
            _events.trySend(
                HomeEvent.ShowMessage(UiText.Resource(R.string.empty_comment_error)),
            )
            return
        }

        _uiState.update { it.copy(commentSheet = it.commentSheet?.copy(draft = "")) }
        viewModelScope.launch {
            commentsRepo.sendComment(sheet.videoId, sheet.draft).onError { error ->
                // Restore the draft so a transient failure doesn't eat what the user typed.
                _uiState.update { it.copy(commentSheet = it.commentSheet?.copy(draft = sheet.draft)) }
                _events.trySend(HomeEvent.ShowMessage(errorText(error)))
            }
        }
    }

    private fun onCommentLikeToggled(commentId: String) {
        if (!userRepo.isSignedIn()) return
        val sheet = _uiState.value.commentSheet ?: return

        updateSheet(sheet.videoId) { current ->
            current.copy(
                comments = current.comments.map {
                    if (it.comment.commentId == commentId) it.copy(isLiked = !it.isLiked) else it
                }.toImmutableList(),
            )
        }
        viewModelScope.launch {
            commentsRepo.likeOrUnlikeComment(sheet.videoId, commentId)
        }
    }

    private fun updateSheet(videoId: String, transform: (CommentSheetUiState) -> CommentSheetUiState) {
        _uiState.update { state ->
            val sheet = state.commentSheet
            if (sheet?.videoId == videoId) state.copy(commentSheet = transform(sheet)) else state
        }
    }

    // endregion

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
        private const val NEAR_END_THRESHOLD = 5
    }
}
