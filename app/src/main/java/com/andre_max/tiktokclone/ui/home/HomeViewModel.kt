package com.andre_max.tiktokclone.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.RemoteUserVideo
import com.andre_max.tiktokclone.utils.FirestoreUtils
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel : ViewModel() {

    private var _listOfRemoteVideo = MutableLiveData<List<RemoteUserVideo>>()
    val listOfRemoteVideo: LiveData<List<RemoteUserVideo>> = _listOfRemoteVideo

    init {
        getAllVideos()
    }

    fun getAllVideos() {
        viewModelScope.launch {
            FirestoreUtils.getAllVideos()
                .collect { list ->
                    Timber.d("listOfRemoteVideo collected as $list")
                    _listOfRemoteVideo.value = list.distinctBy { it.firestoreId }
                }
        }
    }

}