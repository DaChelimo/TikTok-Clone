/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.components.video

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokColors
import com.andre_max.tiktokclone.utils.NumbersUtils
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView

/**
 * One full-screen video with its gesture overlay (tap play/pause, double-tap like) and action
 * rail. Shared by the feed pager and the single-video detail screen. [repeatOnEnd] selects
 * between the detail screen's looping and the feed's advance-to-next ([onVideoEnded]).
 */
@Composable
fun VideoPage(
    item: VideoItemUi,
    isActive: Boolean,
    onAction: (VideoPageAction) -> Unit,
    modifier: Modifier = Modifier,
    repeatOnEnd: Boolean = false,
    onVideoEnded: () -> Unit = {},
) {
    var isPausedByUser by remember(item.video.videoId) { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        VideoPlayer(
            url = item.video.url,
            playWhenReady = isActive && !isPausedByUser,
            repeatOnEnd = repeatOnEnd,
            onEnded = onVideoEnded,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(item.video.videoId) {
                    detectTapGestures(
                        onTap = { isPausedByUser = !isPausedByUser },
                        onDoubleTap = { onAction(VideoPageAction.LikeToggled(item.video.videoId)) },
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
    item: VideoItemUi,
    onAction: (VideoPageAction) -> Unit,
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
    item: VideoItemUi,
    onAction: (VideoPageAction) -> Unit,
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
                    .clickable { onAction(VideoPageAction.AuthorClicked(item.video.authorUid)) }
                    .testTag(VideoPageTags.AUTHOR_ICON),
            )
            if (item.showFollowButton) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_circle),
                    contentDescription = stringResource(R.string.follow_user),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(20.dp)
                        .clickable { onAction(VideoPageAction.FollowClicked(item.video.authorUid)) }
                        .testTag(VideoPageTags.FOLLOW),
                    tint = Color.Unspecified,
                )
            }
        }

        RailButton(
            icon = R.drawable.heart,
            label = NumbersUtils.formatCount(item.likeCount.toInt()),
            contentDescription = stringResource(R.string.like_video),
            tint = if (item.isLiked) TikTokColors.Red else Color.White,
            testTag = VideoPageTags.LIKE,
            onClick = { onAction(VideoPageAction.LikeToggled(videoId)) },
        )

        RailButton(
            icon = R.drawable.ic_insert_comment,
            label = NumbersUtils.formatCount(item.video.totalCommentsSize.coerceAtLeast(0).toInt()),
            contentDescription = stringResource(R.string.open_comment_section),
            tint = Color.White,
            testTag = VideoPageTags.OPEN_COMMENTS,
            onClick = { onAction(VideoPageAction.OpenComments(videoId)) },
        )

        val context = LocalContext.current
        RailButton(
            icon = R.drawable.ic_share,
            label = stringResource(R.string.share),
            contentDescription = stringResource(R.string.share_video),
            tint = Color.White,
            testTag = VideoPageTags.SHARE,
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

/**
 * One ExoPlayer per composed page. Released on dispose; paused whenever [playWhenReady] is
 * false or the app goes to the background. Public because the upload preview screen plays a
 * local file with exactly this lifecycle; [url] accepts remote urls and file paths alike.
 */
@Composable
fun VideoPlayer(
    url: String,
    playWhenReady: Boolean,
    repeatOnEnd: Boolean,
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
                repeatMode = if (repeatOnEnd) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
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
