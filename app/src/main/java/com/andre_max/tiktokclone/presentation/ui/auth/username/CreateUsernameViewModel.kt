/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.username

import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.domain.auth.UsernameRepo
import com.andre_max.tiktokclone.domain.auth.usecase.CheckUsernameAvailabilityUseCase
import com.andre_max.tiktokclone.domain.auth.usecase.ValidateUsernameUseCase
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Screen 4 — the only screen this phase with real I/O.
 *
 * Availability is checked **debounced by [DEBOUNCE_MILLIS] after typing stops**, never per
 * keystroke, and a stale in-flight result must never overwrite a newer query.
 */
@HiltViewModel
class CreateUsernameViewModel @Inject constructor(
    private val validateUsername: ValidateUsernameUseCase,
    private val checkAvailability: CheckUsernameAvailabilityUseCase,
    private val usernameRepo: UsernameRepo,
) : ViewModel() {

    val uiState: StateFlow<CreateUsernameUiState> get() = TODO("Pass 2")

    /** One-shot navigation/error events, backed by a `Channel` — never replayed. */
    val events: Flow<AuthEvent> get() = TODO("Pass 2")

    fun onAction(action: CreateUsernameAction): Unit = TODO("Pass 2")

    companion object {
        const val DEBOUNCE_MILLIS = 400L
    }
}
