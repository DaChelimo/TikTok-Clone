/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.selected_tag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.domain.core.fold
import com.andre_max.tiktokclone.domain.tags.usecase.GetTagVideosUseCase
import com.andre_max.tiktokclone.models.video.RemoteVideo
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

data class SelectedTagUiState(
    val tagName: String = "",
    val videos: ImmutableList<RemoteVideo> = persistentListOf(),
    val isLoading: Boolean = false,
)

/** All videos carrying the tag from the `tagName` nav argument. */
@HiltViewModel
class SelectedTagViewModel @Inject constructor(
    private val getTagVideos: GetTagVideosUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectedTagUiState())
    val uiState: StateFlow<SelectedTagUiState> = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>(NAV_ARG_TAG_NAME)?.let { tagName ->
            _uiState.update { it.copy(tagName = tagName, isLoading = true) }
            viewModelScope.launch {
                val videos = getTagVideos(tagName).fold({ it }, { emptyList() })
                _uiState.update {
                    it.copy(videos = videos.toImmutableList(), isLoading = false)
                }
            }
        }
    }

    companion object {
        /** Must match the argument name in mobile_navigation.xml. */
        const val NAV_ARG_TAG_NAME = "tagName"
    }
}
