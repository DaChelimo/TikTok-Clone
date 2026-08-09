/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.andre_max.tiktokclone.R
import kotlinx.coroutines.launch
import com.andre_max.tiktokclone.utils.NumbersUtils
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

/**
 * Stateless feed — a vertical pager of full-screen videos plus the comment sheet, a pure
 * function of [state] and [onAction]. The share intent fires directly from its click lambda
 * (fire-and-forget gesture effect, per presentation conventions).
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
            CommentSheet(sheet = sheet, isSignedIn = state.isSignedIn, onAction = onAction)
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
        FeedVideoPage(
            item = state.feed[index],
            isActive = pagerState.settledPage == index,
            onAction = onAction,
            onVideoEnded = {
                // Legacy parity: a finished video advances the feed to the next one.
                if (index + 1 < state.feed.size) {
                    scope.launch { pagerState.animateScrollToPage(index + 1) }
                }
            },
        )
    }
}

@Composable
private fun FeedVideoPage(
    item: FeedItemUi,
    isActive: Boolean,
    onAction: (HomeAction) -> Unit,
    onVideoEnded: () -> Unit,
) {
    var isPausedByUser by remember(item.video.videoId) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        FeedPlayer(
            url = item.video.url,
            playWhenReady = isActive && !isPausedByUser,
            onEnded = onVideoEnded,
            modifier = Modifier.fillMaxSize(),
        )

        // Gesture overlay — single tap play/pause, double tap like, same as the legacy
        // GestureDetector on the player view.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(item.video.videoId) {
                    detectTapGestures(
                        onTap = { isPausedByUser = !isPausedByUser },
                        onDoubleTap = { onAction(HomeAction.LikeToggled(item.video.videoId)) },
                    )
                },
        )

        if (isPausedByUser) {
            Icon(
                painter = painterResource(R.drawable.ic_round_play),
                contentDescription = stringResource(R.string.play_video),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(72.dp),
                tint = Color.White.copy(alpha = 0.8f),
            )
        }

        VideoInfoOverlay(
            item = item,
            onAction = onAction,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .navigationBarsPadding(),
        )
    }
}

@Composable
private fun VideoInfoOverlay(
    item: FeedItemUi,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 8.dp, bottom = 16.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.authorUsername?.let { "@$it" } ?: "@...",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.video.description ?: stringResource(R.string.no_description),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        ActionRail(item = item, onAction = onAction)
    }
}

@Composable
private fun ActionRail(
    item: FeedItemUi,
    onAction: (HomeAction) -> Unit,
) {
    val videoId = item.video.videoId

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box {
            AsyncImage(
                model = item.authorProfilePictureUrl,
                contentDescription = stringResource(R.string.creator_profile),
                placeholder = painterResource(R.drawable.ic_perm_identity),
                error = painterResource(R.drawable.ic_perm_identity),
                fallback = painterResource(R.drawable.ic_perm_identity),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable { onAction(HomeAction.AuthorClicked(item.video.authorUid)) }
                    .testTag(HomeScreenTags.AUTHOR_ICON),
            )
            if (item.showFollowButton) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_circle),
                    contentDescription = stringResource(R.string.follow_user),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(20.dp)
                        .clickable { onAction(HomeAction.FollowClicked(item.video.authorUid)) }
                        .testTag(HomeScreenTags.FOLLOW),
                    tint = Color.Unspecified,
                )
            }
        }

        RailButton(
            icon = R.drawable.heart,
            label = NumbersUtils.formatCount(item.likeCount.toInt()),
            contentDescription = stringResource(R.string.like_video),
            tint = if (item.isLiked) Color.Red else Color.White,
            testTag = HomeScreenTags.LIKE,
            onClick = { onAction(HomeAction.LikeToggled(videoId)) },
        )

        RailButton(
            icon = R.drawable.ic_insert_comment,
            label = NumbersUtils.formatCount(item.video.totalCommentsSize.coerceAtLeast(0).toInt()),
            contentDescription = stringResource(R.string.open_comment_section),
            tint = Color.White,
            testTag = HomeScreenTags.OPEN_COMMENTS,
            onClick = { onAction(HomeAction.OpenComments(videoId)) },
        )

        val context = LocalContext.current
        RailButton(
            icon = R.drawable.ic_share,
            label = stringResource(R.string.share),
            contentDescription = stringResource(R.string.share_video),
            tint = Color.White,
            testTag = HomeScreenTags.SHARE,
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, item.video.url)
                }
                context.startActivity(
                    Intent.createChooser(intent, context.getString(R.string.share_video)),
                )
            },
        )
    }
}

@Composable
private fun RailButton(
    icon: Int,
    label: String,
    contentDescription: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.testTag(testTag)) {
            Icon(
                painter = painterResource(icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(32.dp),
                tint = tint,
            )
        }
        Text(text = label, color = Color.White, style = MaterialTheme.typography.labelSmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentSheet(
    sheet: CommentSheetUiState,
    isSignedIn: Boolean,
    onAction: (HomeAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(HomeAction.CloseComments) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.testTag(HomeScreenTags.COMMENT_SHEET),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .imePadding(),
        ) {
            Text(
                text = stringResource(
                    R.string.comment_size,
                    NumbersUtils.formatCount(sheet.commentCount),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(sheet.comments, key = { it.comment.commentId }) { commentUi ->
                    CommentRow(commentUi = commentUi, onAction = onAction)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = sheet.draft,
                    onValueChange = { onAction(HomeAction.CommentDraftChanged(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag(HomeScreenTags.COMMENT_INPUT),
                    placeholder = { Text(stringResource(R.string.add_comment_hint)) },
                    enabled = isSignedIn,
                )
                IconButton(
                    onClick = { onAction(HomeAction.SendCommentClicked) },
                    modifier = Modifier.testTag(HomeScreenTags.SEND_COMMENT),
                    enabled = isSignedIn,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = stringResource(R.string.send_comment),
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentRow(
    commentUi: CommentUi,
    onAction: (HomeAction) -> Unit,
) {
    Row(verticalAlignment = Alignment.Top) {
        AsyncImage(
            model = commentUi.authorProfilePictureUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_perm_identity),
            error = painterResource(R.drawable.ic_perm_identity),
            fallback = painterResource(R.drawable.ic_perm_identity),
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = commentUi.authorUsername ?: "...",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = commentUi.comment.commentText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = { onAction(HomeAction.CommentLikeToggled(commentUi.comment.commentId)) },
            ) {
                Icon(
                    painter = painterResource(R.drawable.heart),
                    contentDescription = stringResource(R.string.like_video),
                    modifier = Modifier.size(16.dp),
                    tint = if (commentUi.isLiked) Color.Red else Color.Gray,
                )
            }
            Text(
                text = NumbersUtils.formatCount(commentUi.comment.commentLikes.toInt()),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

/**
 * One ExoPlayer per composed page (the pager only composes the current page and its
 * neighbours). Released on dispose; paused whenever the page is not the settled one, the user
 * paused it, or the app goes to the background.
 */
@Composable
private fun FeedPlayer(
    url: String,
    playWhenReady: Boolean,
    onEnded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val player = remember(url) {
        SimpleExoPlayer.Builder(context)
            .setUseLazyPreparation(true)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(url)))
                prepare()
            }
    }

    val currentOnEnded by rememberUpdatedState(onEnded)
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) currentOnEnded()
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    LaunchedEffect(player, playWhenReady) {
        player.playWhenReady = playWhenReady
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val currentPlayWhenReady by rememberUpdatedState(playWhenReady)
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> player.pause()
                Lifecycle.Event.ON_RESUME -> player.playWhenReady = currentPlayWhenReady
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                useController = false
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
        },
        update = { it.player = player },
        modifier = modifier,
    )
}
