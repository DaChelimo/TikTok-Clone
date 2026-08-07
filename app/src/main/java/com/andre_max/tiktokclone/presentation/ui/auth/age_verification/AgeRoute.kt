/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent

/**
 * Stateful half of screen 1: owns the ViewModel and turns one-shot [AuthEvent]s into navigation
 * callbacks. [AgeScreen] itself stays a pure function of state so it can be previewed and tested.
 */
@Composable
fun AgeRoute(
    onNavigateToSelectAuth: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AgeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToSelectAuth -> onNavigateToSelectAuth()
                AuthEvent.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    AgeScreen(state = state, onAction = viewModel::onAction)
}
