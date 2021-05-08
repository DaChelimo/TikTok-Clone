package com.andre_max.tiktokclone.presentation.ui.sign_up.email_sign_up

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel

class BasicEmailViewModel : BaseViewModel() {

    val liveEmail = MutableLiveData("")
    val isValid = Transformations.map(liveEmail) {  email ->
        email.contains("@") && email.endsWith(".com") && email.first().isLetter()
    }

    private val _shouldNavigate = MutableLiveData(false)
    val shouldNavigate: LiveData<Boolean> = _shouldNavigate

    fun appendEmailExtension(emailExt: String) {
        val plainEmail = liveEmail.value?.substringBefore("@") ?: ""
        liveEmail.value = plainEmail + emailExt
    }

    fun proceedWithEmail() {
        val email = liveEmail.value ?: ""
        if (!email.endsWith(".com"))
            showMessage(R.string.enter_valid_email)
        else
           _shouldNavigate.value = true
    }

    fun resetShouldNavigate() {
        _shouldNavigate.value = false
    }
}
