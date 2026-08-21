/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

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
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EnterPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingError by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToUsername -> onNavigateToUsername()
                // Log-in mode signs in from this screen, so it ends the flow here rather than
                // going on to pick a username.
                AuthEvent.NavigateToHome -> onNavigateToHome()
                AuthEvent.NavigateBack -> onNavigateBack()
                is AuthEvent.ShowError -> pendingError = event.message
                else -> Unit
            }
        }
    }

    val errorMessage = pendingError?.asString()
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            pendingError = null
        }
    }

    EnterPasswordScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
