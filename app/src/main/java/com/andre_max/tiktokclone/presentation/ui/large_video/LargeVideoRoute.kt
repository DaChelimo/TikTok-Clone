/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.large_video

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.core.asString

/** Stateful single-video detail. */
@Composable
fun LargeVideoRoute(
    onNavigateToProfile: (String) -> Unit,
    viewModel: LargeVideoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingMessage by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LargeVideoEvent.NavigateToProfile -> onNavigateToProfile(event.uid)
                is LargeVideoEvent.ShowMessage -> pendingMessage = event.message
            }
        }
    }

    val message = pendingMessage?.asString()
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            pendingMessage = null
        }
    }

    LargeVideoScreen(
        state = state,
        onPageAction = viewModel::onPageAction,
        onSheetAction = viewModel::onSheetAction,
        snackbarHostState = snackbarHostState,
    )
}
