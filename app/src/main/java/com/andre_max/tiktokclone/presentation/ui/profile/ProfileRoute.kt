/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.models.video.RemoteVideo

/** Stateful profile. */
@Composable
fun ProfileRoute(
    onNavigateToVideo: (RemoteVideo) -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.NavigateToVideo -> onNavigateToVideo(event.video)
                ProfileEvent.SignedOut -> onSignedOut()
            }
        }
    }

    ProfileScreen(state = state, onAction = viewModel::onAction)
}
