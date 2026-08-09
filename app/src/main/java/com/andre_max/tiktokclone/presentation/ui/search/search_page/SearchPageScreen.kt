/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.search.search_page

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.user.User

/**
 * Stateless search page — the input (focused on entry, as the legacy screen forced the
 * keyboard open) and the suggestion list beneath it.
 */
@Composable
fun SearchPageScreen(
    state: SearchPageUiState,
    onAction: (SearchPageAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = state.query,
            onValueChange = { onAction(SearchPageAction.QueryChanged(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .focusRequester(focusRequester)
                .testTag(SearchPageTags.INPUT),
            placeholder = { Text(stringResource(R.string.search)) },
            singleLine = true,
        )

        LazyColumn(modifier = Modifier.testTag(SearchPageTags.SUGGESTION_LIST)) {
            items(state.suggestions, key = { it.uid }) { user ->
                SuggestionRow(user = user, onAction = onAction)
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    user: User,
    onAction: (SearchPageAction) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction(SearchPageAction.SuggestionClicked(user.uid)) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = user.profilePictureUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_perm_identity),
            error = painterResource(R.drawable.ic_perm_identity),
            fallback = painterResource(R.drawable.ic_perm_identity),
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = user.username,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
        )
        IconButton(onClick = { onAction(SearchPageAction.InsertSuggestion(user.username)) }) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = stringResource(R.string.search),
                tint = Color.Gray,
            )
        }
    }
}
