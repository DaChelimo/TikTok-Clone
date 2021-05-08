package com.andre_max.tiktokclone.presentation.ui.upload.select_media

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.repo.local.media.LocalMediaRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.launch

class SelectMediaViewModel : BaseViewModel() {

    val localMediaRepo by lazy { LocalMediaRepo() }

    fun initViewModel(context: Context) {
        viewModelScope.launch {
            localMediaRepo.getAllImages(context)
            localMediaRepo.getAllVideos(context)
        }
    }

}