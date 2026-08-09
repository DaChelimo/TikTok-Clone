/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.select_media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.local.LocalImage
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.repo.local.media.LocalMediaRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SelectMediaUiState(
    val images: ImmutableList<LocalImage> = persistentListOf(),
    val videos: ImmutableList<LocalVideo> = persistentListOf(),
)

/** Loads the device gallery for the two picker tabs. */
@HiltViewModel
class SelectMediaViewModel @Inject constructor(
    private val localMediaRepo: LocalMediaRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectMediaUiState())
    val uiState: StateFlow<SelectMediaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                val images = localMediaRepo.getAllImages().toImmutableList()
                _uiState.update { it.copy(images = images) }
            }
            launch {
                val videos = localMediaRepo.getAllVideos().toImmutableList()
                _uiState.update { it.copy(videos = videos) }
            }
        }
    }
}
