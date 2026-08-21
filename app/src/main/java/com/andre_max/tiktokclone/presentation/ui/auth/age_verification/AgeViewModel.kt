/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.usecase.AgeError
import com.andre_max.tiktokclone.domain.auth.usecase.ValidateAgeUseCase
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

/**
 * Screen 1 (spec §4.1).
 *
 * The age rule lives in [ValidateAgeUseCase], not here and not in the composable: a gate enforced
 * only by a disabled button is bypassable, so [AgeAction.ContinueClicked] re-runs the use case
 * against the stored date rather than trusting [AgeUiState.isContinueEnabled].
 */
@HiltViewModel
class AgeViewModel @Inject constructor(
    private val validateAge: ValidateAgeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgeUiState())
    val uiState: StateFlow<AgeUiState> = _uiState.asStateFlow()

    /** One-shot navigation/error events, backed by a `Channel` — never replayed. */
    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onAction(action: AgeAction) {
        when (action) {
            is AgeAction.DateChanged -> onDateChanged(action.date)
            AgeAction.ContinueClicked -> onContinueClicked()
            AgeAction.BackClicked -> _events.trySend(AuthEvent.NavigateBack)
        }
    }

    private fun onDateChanged(date: LocalDate) {
        val result = validateAge(date)
        _uiState.update {
            it.copy(
                dateOfBirth = date,
                error = result.errorText(),
                isContinueEnabled = result is Result.Success,
            )
        }
    }

    private fun onContinueClicked() {
        val dateOfBirth = _uiState.value.dateOfBirth ?: return

        when (val result = validateAge(dateOfBirth)) {
            is Result.Success -> _events.trySend(AuthEvent.NavigateToSelectAuth)
            is Result.Error -> _uiState.update {
                it.copy(error = result.errorText(), isContinueEnabled = false)
            }
        }
    }

    private fun Result<Unit, AgeError>.errorText(): UiText? = when (this) {
        is Result.Success -> null
        is Result.Error -> when (error) {
            AgeError.TooYoung -> UiText.Resource(R.string.user_too_young)
            AgeError.ImplausiblyOld -> UiText.Resource(R.string.enter_valid_year)
            AgeError.DateInFuture -> UiText.Resource(R.string.birthday_cannot_be_in_the_future)
        }
    }
}
