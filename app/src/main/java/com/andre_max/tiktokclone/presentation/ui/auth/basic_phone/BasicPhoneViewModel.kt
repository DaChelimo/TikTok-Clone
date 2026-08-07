/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.basic_phone

import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.domain.auth.PhoneAuthRepo
import com.andre_max.tiktokclone.domain.auth.usecase.ValidatePhoneNumberUseCase
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Screen 5. */
@HiltViewModel
class BasicPhoneViewModel @Inject constructor(
    private val validatePhoneNumber: ValidatePhoneNumberUseCase,
    private val phoneAuthRepo: PhoneAuthRepo,
) : ViewModel() {

    val uiState: StateFlow<BasicPhoneUiState> get() = TODO("Pass 2")

    /** One-shot navigation/error events, backed by a `Channel` — never replayed. */
    val events: Flow<AuthEvent> get() = TODO("Pass 2")

    fun onAction(action: BasicPhoneAction): Unit = TODO("Pass 2")
}
