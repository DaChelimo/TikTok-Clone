/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.utils.TimeUtils
import kotlinx.coroutines.launch

/**
 * Stateless profile — header, the visibility-filtered tab row, and one 3-column video grid
 * per tab. Whether the sign-out menu and edit button exist at all follows
 * [ProfileUiState.isOwnProfile]; the legacy screen rendered an inert menu on other users'
 * profiles, which read as broken rather than restricted.
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ProfileHeader(state = state, onAction = onAction)
        ProfileTabs(state = state, onAction = onAction)
    }

    if (state.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { onAction(ProfileAction.SignOutDismissed) },
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text(stringResource(R.string.sign_out_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = { onAction(ProfileAction.SignOutConfirmed) },
                    modifier = Modifier.testTag(ProfileScreenTags.SIGN_OUT_CONFIRM),
                ) { Text(stringResource(R.string.sign_out)) }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ProfileAction.SignOutDismissed) }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun ProfileHeader(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.isOwnProfile) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = { onAction(ProfileAction.SignOutClicked) },
                    modifier = Modifier.testTag(ProfileScreenTags.MENU),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.open_menu),
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }

        AsyncImage(
            model = state.user?.profilePictureUrl,
            contentDescription = stringResource(R.string.creator_profile),
            placeholder = painterResource(R.drawable.white_person_icon),
            error = painterResource(R.drawable.white_person_icon),
            fallback = painterResource(R.drawable.white_person_icon),
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop,
        )

        Text(
            text = stringResource(R.string.author_username, state.user?.username ?: "..."),
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            CountColumn(count = state.user?.following ?: 0, label = R.string.following)
            CountColumn(count = state.user?.followers ?: 0, label = R.string.followers)
            CountColumn(count = (state.user?.totalLikes ?: 0).toInt(), label = R.string.likes)
        }

        if (state.isOwnProfile) {
            OutlinedButton(
                onClick = { /* Legacy parity: EditProfile screen is still a TODO. */ },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .testTag(ProfileScreenTags.EDIT_PROFILE),
            ) {
                Text(stringResource(R.string.edit_profile))
            }
        }
    }
}

@Composable
private fun CountColumn(count: Int, label: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ProfileTabs(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
) {
    if (state.tabs.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { state.tabs.size })
    val scope = rememberCoroutineScope()

    // A shrinking tab set (profile user changed) must not leave the pager out of range.
    LaunchedEffect(state.tabs.size) {
        if (pagerState.currentPage >= state.tabs.size) pagerState.scrollToPage(0)
    }

    TabRow(
        selectedTabIndex = pagerState.currentPage.coerceAtMost(state.tabs.size - 1),
        modifier = Modifier.testTag(ProfileScreenTags.TAB_ROW),
    ) {
        state.tabs.forEachIndexed { index, tab ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                text = { Text(stringResource(tab.videoType.labelRes())) },
            )
        }
    }

    HorizontalPager(state = pagerState) { page ->
        VideoGrid(videos = state.tabs[page].videos, onAction = onAction)
    }
}

@Composable
private fun VideoGrid(
    videos: kotlinx.collections.immutable.ImmutableList<RemoteVideo>,
    onAction: (ProfileAction) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .testTag(ProfileScreenTags.VIDEO_GRID),
    ) {
        items(videos, key = { it.videoId }) { video ->
            VideoThumbnail(video = video, onClick = { onAction(ProfileAction.VideoClicked(video)) })
        }
    }
}

@Composable
fun VideoThumbnail(video: RemoteVideo, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .padding(1.dp)
            .background(Color.DarkGray)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(video.url)
                .decoderFactory(VideoFrameDecoder.Factory())
                .build(),
            contentDescription = video.description,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Text(
            text = TimeUtils.convertTimeToDisplayTime(video.duration),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun VideoType.labelRes(): Int = when (this) {
    VideoType.PUBLIC -> R.string.my_videos
    VideoType.LIKED -> R.string.my_liked_videos
    VideoType.PRIVATE -> R.string.my_private_videos
}
