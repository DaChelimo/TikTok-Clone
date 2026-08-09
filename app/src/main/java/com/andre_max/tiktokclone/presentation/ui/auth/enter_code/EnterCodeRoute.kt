/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_code

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent

/** Stateful SMS-code screen. */
@Composable
fun EnterCodeRoute(
    onNavigateToUsername: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EnterCodeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToUsername -> onNavigateToUsername()
                AuthEvent.NavigateToHome -> onNavigateToHome()
                AuthEvent.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    EnterCodeScreen(state = state, onAction = viewModel::onAction)
}
