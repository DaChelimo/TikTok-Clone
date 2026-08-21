/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.large_video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheet
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheetAction
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoPage
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoPageAction

/**
 * Stateless single-video detail — one looping [VideoPage] (legacy restarted the video on end)
 * plus the shared comment sheet and the bottom add-comment bar this screen alone shows.
 */
@Composable
fun LargeVideoScreen(
    state: LargeVideoUiState,
    onPageAction: (VideoPageAction) -> Unit,
    onSheetAction: (CommentSheetAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        state.item?.let { item ->
            VideoPage(
                item = item,
                isActive = true,
                repeatOnEnd = true,
                onAction = onPageAction,
            )

            TextButton(
                onClick = { onPageAction(VideoPageAction.OpenComments(item.video.videoId)) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .testTag(LargeVideoScreenTags.ADD_COMMENT),
            ) {
                Text(
                    text = stringResource(R.string.add_comment_hint),
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }

        state.commentSheet?.let { sheet ->
            CommentSheet(
                sheet = sheet,
                isSignedIn = state.isSignedIn,
                onAction = onSheetAction,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Test/preview tags for the nodes this screen must expose. */
object LargeVideoScreenTags {
    const val ADD_COMMENT = "large_video_add_comment"
}
