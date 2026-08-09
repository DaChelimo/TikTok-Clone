/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.core.onError
import com.andre_max.tiktokclone.domain.core.onSuccess
import com.andre_max.tiktokclone.domain.upload.usecase.PostVideoUseCase
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.models.upload.Progress
import com.andre_max.tiktokclone.presentation.core.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The post step of the upload flow. Posts are public — the legacy screen hardcoded
 * `isPrivate = false` and never exposed a privacy toggle, so neither does this one (adding
 * one is a product decision, not a migration step).
 */
@HiltViewModel
class PostVideoViewModel @Inject constructor(
    private val postVideo: PostVideoUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val localVideo: LocalVideo? = savedStateHandle[NAV_ARG_LOCAL_VIDEO]

    private val _uiState = MutableStateFlow(
        PostVideoUiState(videoFilePath = localVideo?.filePath),
    )
    val uiState: StateFlow<PostVideoUiState> = _uiState.asStateFlow()

    private val _events = Channel<PostVideoEvent>(Channel.BUFFERED)
    val events: Flow<PostVideoEvent> = _events.receiveAsFlow()

    fun onAction(action: PostVideoAction) {
        when (action) {
            is PostVideoAction.DescriptionChanged ->
                _uiState.update { it.copy(description = action.text) }

            PostVideoAction.PostClicked -> onPostClicked()
        }
    }

    private fun onPostClicked() {
        val filePath = localVideo?.filePath ?: return
        if (_uiState.value.uploadStatus == Progress.ACTIVE) return

        _uiState.update { it.copy(uploadStatus = Progress.ACTIVE) }
        viewModelScope.launch {
            postVideo(
                localFilePath = filePath,
                duration = localVideo.duration ?: 0,
                description = _uiState.value.description,
                isPrivate = false,
            ).onSuccess {
                _uiState.update { it.copy(uploadStatus = Progress.DONE) }
                _events.trySend(PostVideoEvent.NavigateHome)
            }.onError {
                _uiState.update { it.copy(uploadStatus = Progress.FAILED) }
                _events.trySend(
                    PostVideoEvent.ShowMessage(
                        UiText.Resource(R.string.error_occurred_during_video_upload),
                    ),
                )
            }
        }
    }

    companion object {
        /** Must match the argument name in mobile_navigation.xml. */
        const val NAV_ARG_LOCAL_VIDEO = "localVideo"
    }
}
