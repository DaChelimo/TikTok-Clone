/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.log_in

import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.andre_max.tiktokclone.presentation.ui.auth.sign_up.GoogleSignInContract

/** Stateful log-in landing. */
@Composable
fun LogInRoute(
    onNavigateToSelectAuth: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LogInViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingError by remember { mutableStateOf<UiText?>(null) }

    val webClientId = viewModel.googleWebClientId
    val googleSignIn = webClientId?.let { id ->
        rememberLauncherForActivityResult(remember(id) { GoogleSignInContract(id) }) { idToken ->
            viewModel.onAction(LogInAction.GoogleTokenReceived(idToken))
        }
    }

    LaunchedEffect(viewModel, googleSignIn) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToSelectAuth -> onNavigateToSelectAuth()
                AuthEvent.NavigateToHome -> onNavigateToHome()
                AuthEvent.NavigateBack -> onNavigateBack()
                is AuthEvent.ShowError -> pendingError = event.message
                AuthEvent.LaunchGoogleSignIn ->
                    googleSignIn?.launch(Unit) ?: viewModel.onGoogleSignInUnavailable()

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

    LogInScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
