package com.andre_max.tiktokclone.presentation.ui.profile.with_account.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.launch

class ProfileVideoViewModel: BaseViewModel() {
    private val videosRepo = VideosRepo()

    private val _listOfRemoteVideo = MutableLiveData<List<RemoteVideo>>()
    val listOfRemoteVideo: LiveData<List<RemoteVideo>> = _listOfRemoteVideo

    fun fetchVideos(profileUid: String, videoType: VideoType) {
        viewModelScope.launch {
            val result = videosRepo.getUserVideos(profileUid, videoType)
            _listOfRemoteVideo.value = result.tryData()?.filterNotNull()
        }
    }

}