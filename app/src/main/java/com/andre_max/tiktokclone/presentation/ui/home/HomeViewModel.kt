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

package com.andre_max.tiktokclone.presentation.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.comment.CommentRepo
import com.andre_max.tiktokclone.repo.network.comment.DefaultCommentRepo
import com.andre_max.tiktokclone.repo.network.user.DefaultUserRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.videos.DefaultVideosRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel (
    val userRepo: UserRepo = DefaultUserRepo(),
    val commentRepo: CommentRepo = DefaultCommentRepo(),
    val videosRepo: VideosRepo = DefaultVideosRepo()
): BaseViewModel() {

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