package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.models.upload.Progress
import com.andre_max.tiktokclone.repo.network.storage.StorageRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.launch
import java.io.File

class PostVideoViewModel : BaseViewModel() {

    private val storageRepo = StorageRepo()
    private val videosRepo = VideosRepo()

    val liveDescription = MutableLiveData("")

    private val _uploadStatus = MutableLiveData(Progress.IDLE)
    val uploadStatus: LiveData<Progress> = _uploadStatus

    fun postVideo(localVideo: LocalVideo) {
        val descriptionText = liveDescription.value ?: run {
            showMessage(R.string.video_description_needed)
            return
        }

        _uploadStatus.value = Progress.ACTIVE
        val tags = processTags()

        viewModelScope.launch {
            val localUri = File(localVideo.filePath ?: return@launch).toUri()
            val downloadUrl =
                storageRepo.uploadVideo(localUri).tryData()?.toString() ?: return@launch

            videosRepo.saveVideoToFireDB(
                isPrivate = false,
                videoUrl = downloadUrl,
                descriptionText = descriptionText,
                tags = tags,
                duration = localVideo.duration,
                onComplete = { succeeded ->
                    _uploadStatus.value = if (succeeded) Progress.DONE else Progress.FAILED
                }
            )
        }

    }


    private fun processTags(): Map<String, String> {
        val descriptionList = liveDescription.value?.split(" ") ?: listOf()
        return descriptionList.filter { it.startsWith("#") }.associateBy { it.replace("#", "") }
    }
}