/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.record

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.user.UserRepo
import com.andre_max.tiktokclone.models.local.LocalRecordLocation
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.repo.local.media.LocalMediaRepo
import com.andre_max.tiktokclone.repo.local.record.RecordVideoRepo
import com.andre_max.tiktokclone.repo.local.space.LocalSpaceRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.FileDescriptor
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
 * Camera recording session state. The camera itself lives in the composable (it is a View);
 * this ViewModel owns the MediaStore session around it: guarding preconditions, opening the
 * pending entry, and turning the finished take into a [LocalVideo] for the preview step.
 */
@HiltViewModel
class RecordVideoViewModel @Inject constructor(
    private val localSpaceRepo: LocalSpaceRepo,
    private val recordVideoRepo: RecordVideoRepo,
    private val localMediaRepo: LocalMediaRepo,
    private val userRepo: UserRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordVideoUiState())
    val uiState: StateFlow<RecordVideoUiState> = _uiState.asStateFlow()

    private val _events = Channel<RecordVideoEvent>(Channel.BUFFERED)
    val events: Flow<RecordVideoEvent> = _events.receiveAsFlow()

    private var timeCreated = 0L
    private var localRecordLocation: LocalRecordLocation? = null

    /**
     * Guards the preconditions and opens the MediaStore entry the camera will write into.
     * @return the descriptor to hand to `CameraView.takeVideo`, or null when recording can't
     * start (signed out, low storage, or the entry could not be created).
     */
    suspend fun startVideo(): FileDescriptor? {
        if (!userRepo.isSignedIn()) {
            _events.trySend(
                RecordVideoEvent.ShowMessage(
                    UiText.Resource(R.string.account_needed_to_record_video),
                ),
            )
            return null
        }
        if (!localSpaceRepo.hasEnoughSpace()) {
            _uiState.update { it.copy(showLittleSpace = true) }
            return null
        }

        _uiState.update { it.copy(hasRecordingStarted = true, isRecording = true) }
        timeCreated = System.currentTimeMillis()
        localRecordLocation = recordVideoRepo.initVideo(timeCreated)
        return localRecordLocation?.fileDescriptor
    }

    fun resumeVideo() {
        _uiState.update { it.copy(isRecording = true) }
    }

    fun pauseVideo() {
        _uiState.update { it.copy(isRecording = false) }
    }

    fun stopVideo() {
        if (!_uiState.value.hasRecordingStarted) return
        _uiState.update { it.copy(hasRecordingStarted = false, isRecording = false) }
        viewModelScope.launch { recordVideoRepo.stopVideo() }
    }

    /** The camera delivered the finished take — hand it to the preview step. */
    fun onVideoTaken() {
        val filePath = localRecordLocation?.filePath ?: return
        viewModelScope.launch {
            val duration = localMediaRepo.getVideoDuration(filePath.toUri())
            _events.trySend(
                RecordVideoEvent.NavigateToPreview(
                    LocalVideo(filePath, duration, timeCreated.toString()),
                ),
            )
        }
    }

    fun resetShowLittleLayout() {
        _uiState.update { it.copy(showLittleSpace = false) }
    }
}
