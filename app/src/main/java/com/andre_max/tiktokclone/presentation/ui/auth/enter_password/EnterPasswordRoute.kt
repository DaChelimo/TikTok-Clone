/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent

/**
 * Stateful screen 3: owns the ViewModel and turns one-shot [AuthEvent]s into navigation calls.
 *
 * Neither the identity coming in nor the password going out travels through this signature any
 * more — both live in `SignUpSession`, so the password never reaches a nav argument and from
 * there saved-instance-state.
 */
@Composable
fun EnterPasswordRoute(
    onNavigateToUsername: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EnterPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToUsername -> onNavigateToUsername()
                AuthEvent.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    EnterPasswordScreen(state = state, onAction = viewModel::onAction)
}
