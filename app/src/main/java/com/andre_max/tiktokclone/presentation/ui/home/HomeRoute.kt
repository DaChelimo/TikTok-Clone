/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

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

/**
 * Stateful feed. [onCommentSheetVisibilityChanged] exists because the bottom nav bar is a View
 * owned by the activity — the fragment shell hides/shows it, the way the legacy
 * `onCommentVisibilityChanged` callback did.
 */
@Composable
fun HomeRoute(
    onNavigateToProfile: (String) -> Unit,
    onCommentSheetVisibilityChanged: (Boolean) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingMessage by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.NavigateToProfile -> onNavigateToProfile(event.uid)
                is HomeEvent.ShowMessage -> pendingMessage = event.message
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

    val sheetVisible = state.commentSheet != null
    LaunchedEffect(sheetVisible) { onCommentSheetVisibilityChanged(sheetVisible) }

    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
