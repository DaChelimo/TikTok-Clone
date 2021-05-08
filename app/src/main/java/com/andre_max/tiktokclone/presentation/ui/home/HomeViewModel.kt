package com.andre_max.tiktokclone.presentation.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.comment.CommentRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel : BaseViewModel() {
    val userRepo = UserRepo()
    val commentRepo = CommentRepo()
    val videosRepo = VideosRepo()

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
                Timber.d("result.data.size is ${result.tryData()?.size}")

                if (result.succeeded)
                    _listOfRemoteVideo.value = result.tryData() ?: listOf()
                isFetching = false
            }
        }
    }

}