package com.andre_max.tiktokclone.utils.architecture

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged

open class BaseViewModel: ViewModel() {
    private val _snackBarMessageRes = MutableLiveData<Int?>()
    val snackBarMessageRes: LiveData<Int?> = _snackBarMessageRes.distinctUntilChanged()

    fun showMessage(@StringRes messageRes: Int) {
        _snackBarMessageRes.value = messageRes
    }
    fun clearMessage() {
        _snackBarMessageRes.value = null
    }
}