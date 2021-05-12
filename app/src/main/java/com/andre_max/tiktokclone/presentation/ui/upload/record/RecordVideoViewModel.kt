/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.presentation.ui.upload.record

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.local.LocalRecordLocation
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.repo.local.record.RecordVideoRepo
import com.andre_max.tiktokclone.repo.local.record.DefaultRecordVideoRepo
import com.andre_max.tiktokclone.repo.local.space.LocalSpaceRepo
import com.andre_max.tiktokclone.repo.local.space.DefaultLocalSpaceRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.user.DefaultUserRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileDescriptor
import kotlin.properties.Delegates

class RecordVideoViewModel (
    private val localSpaceRepo: LocalSpaceRepo = DefaultLocalSpaceRepo(),
    private val recordVideoRepo: RecordVideoRepo = DefaultRecordVideoRepo(),
    private val userRepo: UserRepo = DefaultUserRepo()
) : BaseViewModel() {

    private var timeCreated by Delegates.notNull<Long>()
    private var localRecordLocation: LocalRecordLocation? = null

    private val _localVideo = MutableLiveData<LocalVideo?>()
    val localVideo: LiveData<LocalVideo?> = _localVideo

    private val _showLittleSpace = MutableLiveData(false)
    val showLittleSpace: LiveData<Boolean> = _showLittleSpace

    private val _hasRecordingStarted = MutableLiveData(false)
    val hasRecordingStarted: LiveData<Boolean> = _hasRecordingStarted

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    /**
     * This function sets hasRecordingStarted to true and retrieves a new uri and its corresponding file descriptor
     */
    suspend fun startVideo(context: Context): FileDescriptor? {
        if (!userRepo.doesDeviceHaveAnAccount()) {
            showMessage(R.string.account_needed_to_record_video)
            return null
        }
        if (!localSpaceRepo.hasEnoughSpace()) {
            _showLittleSpace.value = true
            return null
        }
        _hasRecordingStarted.value = true
        _isRecording.value = true
        timeCreated = System.currentTimeMillis()

        localRecordLocation = recordVideoRepo.initVideo(context, timeCreated)
        return localRecordLocation?.fileDescriptor
    }

    fun resumeVideo() {
        _isRecording.value = true
    }

    fun pauseVideo() {
        _isRecording.value = false
    }

    fun stopVideo(context: Context) {
        if (_hasRecordingStarted.value == true) {
            _isRecording.value = false
            _hasRecordingStarted.value = false

            viewModelScope.launch {
                recordVideoRepo.stopVideo(context)
            }
        }
    }

    fun getCameraListener(context: Context) = object : CameraListener() {
        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            Timber.d("Video recording has started")
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            Timber.d("Video recording has ended")
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            Timber.d("Video has been taken. Result.contentUri ${localRecordLocation?.contentUri} and result.filePath is ${localRecordLocation?.filePath}")


            viewModelScope.launch {
                val duration = getVideoDuration(context)
                Timber.d("duration is $duration")

                _localVideo.value = LocalVideo(
                    localRecordLocation?.filePath,
                    duration?.toLong() ?: 0,
                    timeCreated.toString()
                )
            }
        }
    }

    suspend fun getVideoDuration(context: Context) = withContext(Dispatchers.IO) {
        val mediaPlayer = MediaPlayer.create(context, localRecordLocation?.filePath?.toUri())
        val duration = mediaPlayer?.duration
        mediaPlayer?.release()

        return@withContext duration
    }

    fun resetLocalVideo() {
        _localVideo.value = null
    }

    fun resetShowLittleLayout() {
        _showLittleSpace.value = false
    }
}