/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent

/** Stateful screen 2: owns the ViewModel and turns one-shot [AuthEvent]s into navigation calls. */
@Composable
fun BasicEmailRoute(
    isLogin: Boolean,
    onNavigateToPassword: (email: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BasicEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToPassword -> onNavigateToPassword(event.email)
                AuthEvent.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    BasicEmailScreen(state = state, onAction = viewModel::onAction, isLogin = isLogin)
}
