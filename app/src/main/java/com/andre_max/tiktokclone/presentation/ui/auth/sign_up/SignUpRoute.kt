/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.sign_up

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

/**
 * Stateful screen 7.
 *
 * `rememberLauncherForActivityResult` replaces the deprecated `onActivityResult` override the
 * old Fragment used, and the ViewModel asks for the chooser through an event instead of the
 * Fragment reaching into `viewModel.googleAuthRepo` to start it itself.
 */
@Composable
fun SignUpRoute(
    onNavigateToAge: () -> Unit,
    onNavigateToLogIn: () -> Unit,
    onNavigateToUsername: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingError by remember { mutableStateOf<UiText?>(null) }

    val webClientId = viewModel.googleWebClientId
    val googleSignIn = webClientId?.let { id ->
        rememberLauncherForActivityResult(remember(id) { GoogleSignInContract(id) }) { idToken ->
            viewModel.onAction(SignUpAction.GoogleTokenReceived(idToken))
        }
    }

    LaunchedEffect(viewModel, googleSignIn) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToAge -> onNavigateToAge()
                AuthEvent.NavigateToLogIn -> onNavigateToLogIn()
                AuthEvent.NavigateToUsername -> onNavigateToUsername()
                AuthEvent.NavigateBack -> onNavigateBack()
                is AuthEvent.ShowError -> pendingError = event.message
                AuthEvent.LaunchGoogleSignIn ->
                    googleSignIn?.launch(Unit) ?: viewModel.onGoogleSignInFailed()

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

    SignUpScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
    )
}
