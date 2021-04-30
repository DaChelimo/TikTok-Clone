package com.andre_max.tiktokclone.presentation.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    val userRepo = UserRepo()
    private var videosRepo = VideosRepo()

    private var _listOfRemoteVideo = MutableLiveData<List<RemoteVideo>>()
    val listOfRemoteVideo: LiveData<List<RemoteVideo>> = _listOfRemoteVideo

    private var isFetching = false

    init {
        fetchVideos()
    }

    fun fetchVideos() {
        if (!isFetching) {
            isFetching = true
            viewModelScope.launch {
                val result = videosRepo.fetchRandomVideos()
                if (result.succeeded)
                    _listOfRemoteVideo.value = result.forceData()
                isFetching = false
            }
        }
    }

}