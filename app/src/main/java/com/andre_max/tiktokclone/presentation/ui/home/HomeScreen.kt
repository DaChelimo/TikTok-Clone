/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheet
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheetAction
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoPage
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoPageAction
import kotlinx.coroutines.launch

/**
 * Stateless feed — a vertical pager of full-screen [VideoPage]s plus the shared comment
 * sheet, a pure function of [state] and [onAction].
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        FeedPager(state = state, onAction = onAction)

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White,
            )
        }

        state.commentSheet?.let { sheet ->
            CommentSheet(
                sheet = sheet,
                isSignedIn = state.isSignedIn,
                onAction = { onAction(it.toHomeAction()) },
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun FeedPager(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { state.feed.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { onAction(HomeAction.PageSettled(it)) }
    }

    val scope = rememberCoroutineScope()

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .testTag(HomeScreenTags.FEED),
        key = { state.feed[it].video.videoId },
    ) { index ->
        VideoPage(
            item = state.feed[index],
            isActive = pagerState.settledPage == index,
            onAction = { onAction(it.toHomeAction()) },
            onVideoEnded = {
                // Legacy parity: a finished video advances the feed to the next one.
                if (index + 1 < state.feed.size) {
                    scope.launch { pagerState.animateScrollToPage(index + 1) }
                }
            },
        )
    }
}

private fun VideoPageAction.toHomeAction(): HomeAction = when (this) {
    is VideoPageAction.LikeToggled -> HomeAction.LikeToggled(videoId)
    is VideoPageAction.FollowClicked -> HomeAction.FollowClicked(authorUid)
    is VideoPageAction.AuthorClicked -> HomeAction.AuthorClicked(authorUid)
    is VideoPageAction.OpenComments -> HomeAction.OpenComments(videoId)
}

private fun CommentSheetAction.toHomeAction(): HomeAction = when (this) {
    CommentSheetAction.Close -> HomeAction.CloseComments
    is CommentSheetAction.DraftChanged -> HomeAction.CommentDraftChanged(text)
    CommentSheetAction.Send -> HomeAction.SendCommentClicked
    is CommentSheetAction.CommentLikeToggled -> HomeAction.CommentLikeToggled(commentId)
}
