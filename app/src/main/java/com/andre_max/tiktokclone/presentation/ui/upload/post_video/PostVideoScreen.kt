/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.upload.Progress

/** Stateless post step — thumbnail, description box, post button, upload overlay. */
@Composable
fun PostVideoScreen(
    state: PostVideoUiState,
    onAction: (PostVideoAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                TextField(
                    value = state.description,
                    onValueChange = { onAction(PostVideoAction.DescriptionChanged(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag(PostVideoScreenTags.DESCRIPTION),
                    placeholder = { Text(stringResource(R.string.video_description)) },
                    enabled = state.uploadStatus != Progress.ACTIVE,
                )

                Spacer(modifier = Modifier.width(12.dp))

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(state.videoFilePath)
                        .decoderFactory(VideoFrameDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .width(84.dp)
                        .aspectRatio(9f / 16f)
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onAction(PostVideoAction.PostClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PostVideoScreenTags.POST),
                enabled = state.uploadStatus != Progress.ACTIVE,
            ) {
                Text(stringResource(R.string.post_video))
            }
        }

        if (state.uploadStatus == Progress.ACTIVE) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = stringResource(R.string.uploading_video),
                    modifier = Modifier.padding(top = 12.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
