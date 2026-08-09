/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.discover

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoThumbnail
import com.andre_max.tiktokclone.utils.NumbersUtils

/** Stateful discover surface. */
@Composable
fun DiscoverRoute(
    onNavigateToSearch: () -> Unit,
    onNavigateToTag: (Tag) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DiscoverScreen(
        state = state,
        onSearchClicked = onNavigateToSearch,
        onTagClicked = onNavigateToTag,
    )
}

/**
 * Stateless discover — a search entry and one row per popular tag with its videos. Tapping a
 * row (or one of its thumbnails) opens the tag page; the legacy screen rendered these rows
 * but never wired the navigation the spec describes.
 */
@Composable
fun DiscoverScreen(
    state: DiscoverUiState,
    onSearchClicked: () -> Unit,
    onTagClicked: (Tag) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onSearchClicked)
                .testTag(DiscoverScreenTags.SEARCH),
            placeholder = { Text(stringResource(R.string.search)) },
            enabled = false,
        )

        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
            return
        }

        LazyColumn(modifier = Modifier.testTag(DiscoverScreenTags.TAG_LIST)) {
            items(state.tagRows, key = { it.tag.name }) { row ->
                TagRow(row = row, onTagClicked = onTagClicked)
            }
        }
    }
}

@Composable
private fun TagRow(
    row: DiscoverTagUi,
    onTagClicked: (Tag) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTagClicked(row.tag) }
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.hash_tag_name, row.tag.name),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = NumbersUtils.formatCount(row.tag.count),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(row.videos, key = { it.videoId }) { video ->
                VideoThumbnail(
                    video = video,
                    onClick = { onTagClicked(row.tag) },
                    modifier = Modifier.width(96.dp),
                )
            }
        }
    }
}

/** Test/preview tags for the nodes this screen must expose. */
object DiscoverScreenTags {
    const val SEARCH = "discover_search"
    const val TAG_LIST = "discover_tag_list"
}
