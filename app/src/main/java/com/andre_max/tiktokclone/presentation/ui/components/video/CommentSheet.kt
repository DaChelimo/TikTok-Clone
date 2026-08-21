/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.components.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokColors
import com.andre_max.tiktokclone.utils.NumbersUtils

/** The comment sheet over a video — live thread, comment-likes, and the send box. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentSheet(
    sheet: CommentSheetUiState,
    isSignedIn: Boolean,
    onAction: (CommentSheetAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(CommentSheetAction.Close) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        modifier = Modifier.testTag(VideoPageTags.COMMENT_SHEET),
        containerColor = MaterialTheme.colorScheme.surface,
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
                textAlign = TextAlign.Center,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                // Reference comment sheet: the input is a rounded light-gray pill.
                TextField(
                    value = sheet.draft,
                    onValueChange = { onAction(CommentSheetAction.DraftChanged(it)) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag(VideoPageTags.COMMENT_INPUT),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.add_comment_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    enabled = isSignedIn,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                IconButton(
                    onClick = { onAction(CommentSheetAction.Send) },
                    modifier = Modifier.testTag(VideoPageTags.SEND_COMMENT),
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
    onAction: (CommentSheetAction) -> Unit,
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
                onClick = {
                    onAction(CommentSheetAction.CommentLikeToggled(commentUi.comment.commentId))
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.heart),
                    contentDescription = stringResource(R.string.like_video),
                    modifier = Modifier.size(16.dp),
                    tint = if (commentUi.isLiked) TikTokColors.Red else Color.Gray,
                )
            }
            Text(
                text = NumbersUtils.formatCount(commentUi.comment.commentLikes.toInt()),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
