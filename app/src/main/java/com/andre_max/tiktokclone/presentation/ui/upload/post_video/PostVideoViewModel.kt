package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.repo.network.storage.StorageRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.viewModel.BaseViewModel
import kotlinx.coroutines.launch

class PostVideoViewModel : BaseViewModel() {

    private val storageRepo = StorageRepo()
    private val videosRepo = VideosRepo()

    val liveDescription = MutableLiveData("")

    private val _isPosting = MutableLiveData(false)
    val isPosting: LiveData<Boolean> = _isPosting

    fun postVideo(localVideo: LocalVideo) {
        val descriptionText = liveDescription.value ?: run {
            showMessage(R.string.video_description_needed)
            return
        }
        val tags = processTags()

        viewModelScope.launch {
            storageRepo.uploadVideo(localVideo.url?.toUri()).getData()?.let { videoUrl ->
                videosRepo.saveVideoToFireDB(
                    false,
                    videoUrl.toString(),
                    descriptionText,
                    tags,
                    localVideo.duration
                )
            }

        }
    }

    private fun processTags(): List<String> {
        val descriptionList = liveDescription.value?.split(" ") ?: listOf()
        return descriptionList.filter { it.startsWith("#") }
    }
}