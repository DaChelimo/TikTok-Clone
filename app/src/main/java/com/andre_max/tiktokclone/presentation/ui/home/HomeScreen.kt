/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
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
    onSearchClick: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        FeedPager(state = state, onAction = onAction)

        FeedTopBar(
            onSearchClick = onSearchClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding(),
        )

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

/**
 * "Following | For You" chrome from the reference feed screens. Only the For You feed exists,
 * so Following renders dimmed and inert rather than pretending to switch feeds.
 */
@Composable
private fun FeedTopBar(
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.following),
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.for_you),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(3.dp)
                        .background(Color.White),
                )
            }
        }

        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .testTag(HomeScreenTags.SEARCH),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search_white),
                contentDescription = stringResource(R.string.search),
                tint = Color.White,
            )
        }
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
