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

package com.andre_max.tiktokclone.presentation.ui.discover

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.tag.DefaultTagRepo
import com.andre_max.tiktokclone.repo.network.videos.DefaultVideosRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DiscoverViewModel : BaseViewModel() {
    private val tagRepo = DefaultTagRepo()
    private val videosRepo = DefaultVideosRepo()
    private val mediaMetadataRetriever = MediaMetadataRetriever()

    private val _listOfPopularTags = MutableLiveData<List<Tag>>()
    val listOfPopularTags: LiveData<List<Tag>> = _listOfPopularTags

    init {
        viewModelScope.launch {
            _listOfPopularTags.value = tagRepo.fetchPopularTags().tryData()
        }
    }

    suspend fun getVideoThumbnail(context: Context, remoteVideo: RemoteVideo) =
        withContext(Dispatchers.IO) {
            mediaMetadataRetriever.setDataSource(context, remoteVideo.url.toUri())
            mediaMetadataRetriever.frameAtTime
        }

    // Get the video ids then map those to actual videos
    suspend fun fetchVideos(popularTag: Tag): List<RemoteVideo> {
        val listOfTagVideoIds = tagRepo.fetchTagVideos(popularTag.name).tryData() ?: listOf()
        val listOfVideo =
            listOfTagVideoIds.mapNotNull { videoId -> videosRepo.fetchVideo(videoId).tryData() }
        Timber.d("listOfVideo.size is ${listOfVideo.size}")
        return listOfVideo
    }

    override fun onCleared() {
        super.onCleared()
        mediaMetadataRetriever.release()
    }
}