/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.username

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
 * Stateful screen 4: owns the ViewModel and turns one-shot [AuthEvent]s into navigation calls.
 *
 * The only screen in the flow that surfaces [AuthEvent.ShowError] — it is where the account is
 * actually created, so it is the only one that can fail for a reason outside the text field.
 *
 * The pending error is held as state rather than shown straight from the event collector,
 * because resolving a [UiText] is a `@Composable` call and the collector is a coroutine. Parking
 * it in state moves the resolution back into composition, which is where presentation/AGENTS.md
 * requires it to happen.
 */
@Composable
fun CreateUsernameRoute(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CreateUsernameViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingError by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
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

    CreateUsernameScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
