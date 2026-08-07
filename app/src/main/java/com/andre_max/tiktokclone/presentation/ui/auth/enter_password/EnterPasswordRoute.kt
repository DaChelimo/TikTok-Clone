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
 * [identity] is the email local-part carried in from screen 2, and is what makes
 * `ValidatePasswordUseCase`'s `ContainsIdentity` rule (D6) possible. It is assigned rather than
 * constructor-injected because the ViewModel's constructor is pinned by spec §4.3.
 */
@Composable
fun EnterPasswordRoute(
    identity: String?,
    onNavigateToUsername: (password: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EnterPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, identity) {
        viewModel.identity = identity
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                // Read from the ViewModel, not the captured `state`: this collector is keyed on
                // the ViewModel alone, so `state` here would be the value at first composition.
                AuthEvent.NavigateToUsername ->
                    onNavigateToUsername(viewModel.uiState.value.password)

                AuthEvent.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    EnterPasswordScreen(state = state, onAction = viewModel::onAction)
}
