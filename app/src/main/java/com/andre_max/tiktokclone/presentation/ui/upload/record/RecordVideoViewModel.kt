package com.andre_max.tiktokclone.presentation.ui.upload.record

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.models.local.LocalRecordLocation
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.repo.local.record.RecordVideoRepo
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import timber.log.Timber
import java.io.FileDescriptor
import kotlin.properties.Delegates

class RecordVideoViewModel: ViewModel() {

    private val recordVideoRepo = RecordVideoRepo()
    private var timeCreated by Delegates.notNull<Long>()
    private var localRecordLocation: LocalRecordLocation? = null
    
    private val _localVideo = MutableLiveData<LocalVideo>()
    val localVideo: LiveData<LocalVideo> = _localVideo

    private val _hasRecordingStarted = MutableLiveData(false)
    val hasRecordingStarted: LiveData<Boolean> = _hasRecordingStarted

    /**
     * This function sets hasRecordingStarted to true and retrieves a new uri and its corresponding file descriptor
     */
    fun takeVideo(context: Context): FileDescriptor? {
        _hasRecordingStarted.value = true
        timeCreated = System.currentTimeMillis()

        localRecordLocation = recordVideoRepo.getLocalRecordLocation(context, timeCreated)
        return localRecordLocation?.fileDescriptor
    }

    fun stopVideo(context: Context) {
        _hasRecordingStarted.value = false
        recordVideoRepo.stopVideo(context, localRecordLocation?.fileUri)
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
            Timber.d("Video has been taken and result.size is ${result.size}")

            val mediaPlayer = MediaPlayer.create(context, result.file.toUri())
            val duration = mediaPlayer.duration
            Timber.d("result.duration is $duration")

            _localVideo.value = LocalVideo(
                result.file.toUri().toString(),
                duration.toLong(),
                timeCreated.toString()
            )

            mediaPlayer.release()
        }
    }


}