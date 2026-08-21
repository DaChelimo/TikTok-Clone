/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.select_media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.utils.TimeUtils
import kotlinx.coroutines.launch

/** Stateful gallery picker. */
@Composable
fun SelectMediaRoute(
    onNavigateToPreview: (LocalVideo) -> Unit,
    onClose: () -> Unit,
    viewModel: SelectMediaViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SelectMediaScreen(state = state, onVideoClicked = onNavigateToPreview, onClose = onClose)
}

/**
 * Stateless gallery picker — Images/Videos tabs of 4-column grids. Only videos are
 * selectable; the legacy image tap was an empty lambda and picking an image still leads
 * nowhere in the product.
 */
@Composable
fun SelectMediaScreen(
    state: SelectMediaUiState,
    onVideoClicked: (LocalVideo) -> Unit,
    onClose: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag(SelectMediaTags.CLOSE)) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_close),
                    contentDescription = stringResource(R.string.close_page),
                )
            }
        }

        val pagerState = rememberPagerState(pageCount = { 2 })
        val scope = rememberCoroutineScope()

        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text(stringResource(R.string.images)) },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text(stringResource(R.string.videos)) },
            )
        }

        HorizontalPager(state = pagerState) { page ->
            if (page == 0) ImageGrid(state) else VideoGrid(state, onVideoClicked)
        }
    }
}

@Composable
private fun ImageGrid(state: SelectMediaUiState) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxSize()
            .testTag(SelectMediaTags.IMAGE_GRID),
    ) {
        // Indexed keys: MediaStore rows can repeat paths and LazyGrid keys must be unique.
        itemsIndexed(state.images, key = { index, _ -> index }) { _, image ->
            AsyncImage(
                model = image.url,
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(1.dp)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun VideoGrid(
    state: SelectMediaUiState,
    onVideoClicked: (LocalVideo) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxSize()
            .testTag(SelectMediaTags.VIDEO_GRID),
    ) {
        itemsIndexed(state.videos, key = { index, _ -> index }) { _, video ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(1.dp)
                    .background(Color.DarkGray)
                    .clickable { onVideoClicked(video) },
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.filePath)
                        .decoderFactory(VideoFrameDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = TimeUtils.convertTimeToDisplayTime(video.duration ?: 0),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

object SelectMediaTags {
    const val CLOSE = "select_media_close"
    const val IMAGE_GRID = "select_media_image_grid"
    const val VIDEO_GRID = "select_media_video_grid"
}
