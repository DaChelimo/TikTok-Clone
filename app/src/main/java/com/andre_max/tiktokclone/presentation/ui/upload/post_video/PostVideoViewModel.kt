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

package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.models.upload.Progress
import com.andre_max.tiktokclone.repo.local.space.DefaultLocalSpaceRepo
import com.andre_max.tiktokclone.repo.network.storage.StorageRepo
import com.andre_max.tiktokclone.repo.network.videos.DefaultVideosRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.launch
import java.io.File

class PostVideoViewModel : BaseViewModel() {

    private val localSpaceRepo = DefaultLocalSpaceRepo()
    private val storageRepo = StorageRepo()
    private val videosRepo = DefaultVideosRepo()

    val liveDescription = MutableLiveData("")

    private val _uploadStatus = MutableLiveData(Progress.IDLE)
    val uploadStatus: LiveData<Progress> = _uploadStatus

    fun postVideo(context: Context, localVideo: LocalVideo) {
        val descriptionText = liveDescription.value ?: run {
            showMessage(R.string.video_description_needed)
            return
        }

        _uploadStatus.value = Progress.ACTIVE
        val tags = processTags()

        viewModelScope.launch {
            val newFilePath =
                localSpaceRepo.compressVideo(context, localVideo.filePath ?: return@launch)
            val localUri = File(newFilePath ?: return@launch).toUri()

            val result = storageRepo.uploadVideo(localUri)
            if (!result.succeeded) {
                _uploadStatus.value = Progress.FAILED
                showMessage(R.string.error_occurred_during_video_upload)
                return@launch
            }

            val downloadUrl = result.tryData()?.toString() ?: return@launch

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