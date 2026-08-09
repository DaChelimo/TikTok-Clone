/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.domain.core.fold
import com.andre_max.tiktokclone.domain.core.onError
import com.andre_max.tiktokclone.domain.user.UserRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val showLikedVideos: Boolean = false,
    val isLoading: Boolean = true,
)

sealed interface SettingsAction {
    data object ShowLikedVideosToggled : SettingsAction
}

/**
 * App settings. One real setting today: the liked-tab visibility flag the `User` model always
 * carried with a "no settings UI yet" TODO — this screen is that UI.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepo: UserRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val myUid = userRepo.currentUid()
        if (myUid == null) {
            _uiState.update { it.copy(isLoading = false) }
        } else {
            viewModelScope.launch {
                val showLiked = userRepo.getUserProfile(myUid)
                    .fold({ it.showLikedVideos }, { false })
                _uiState.update { it.copy(showLikedVideos = showLiked, isLoading = false) }
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.ShowLikedVideosToggled -> onToggleShowLikedVideos()
        }
    }

    private fun onToggleShowLikedVideos() {
        val newValue = !_uiState.value.showLikedVideos
        _uiState.update { it.copy(showLikedVideos = newValue) }

        viewModelScope.launch {
            userRepo.updateShowLikedVideos(newValue).onError {
                _uiState.update { it.copy(showLikedVideos = !newValue) }
            }
        }
    }
}
