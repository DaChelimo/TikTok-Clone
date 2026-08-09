/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.basic_phone

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent

/** Stateful screen 5: owns the ViewModel and turns one-shot [AuthEvent]s into navigation calls. */
@Composable
fun BasicPhoneRoute(
    onNavigateToVerificationCode: (verificationId: String) -> Unit,
    onNavigateToUsername: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BasicPhoneViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToVerificationCode ->
                    onNavigateToVerificationCode(event.verificationId)

                AuthEvent.NavigateToUsername -> onNavigateToUsername()
                AuthEvent.NavigateBack -> onNavigateBack()
                else -> Unit
            }
        }
    }

    BasicPhoneScreen(state = state, onAction = viewModel::onAction)
}
