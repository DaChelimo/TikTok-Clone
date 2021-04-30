package com.andre_max.tiktokclone.presentation.ui.search

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.tag.TagRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.viewModel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SearchViewModel : BaseViewModel() {
    private val tagRepo = TagRepo()
    private val videosRepo = VideosRepo()
    private val mediaMetadataRetriever = MediaMetadataRetriever()

    private val _listOfPopularTags = MutableLiveData<List<Tag>>()
    val listOfPopularTags: LiveData<List<Tag>> = _listOfPopularTags

    init {
        viewModelScope.launch {
            _listOfPopularTags.value = tagRepo.fetchPopularTags().getData()
        }
    }

    suspend fun getVideoThumbnail(context: Context, remoteVideo: RemoteVideo) =
        withContext(Dispatchers.IO) {
            mediaMetadataRetriever.setDataSource(context, remoteVideo.url.toUri())
            mediaMetadataRetriever.frameAtTime
        }

    // Get the video ids then map those to actual videos
    suspend fun fetchVideos(popularTag: Tag): List<RemoteVideo> {
        val listOfTagVideoIds = tagRepo.fetchTagVideos(popularTag.name).getData() ?: listOf()
        val listOfVideo =
            listOfTagVideoIds.mapNotNull { videoId -> videosRepo.fetchVideo(videoId).getData() }
        Timber.d("listOfVideo.size is ${listOfVideo.size}")
        return listOfVideo
    }

}