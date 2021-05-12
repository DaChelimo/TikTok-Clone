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

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email

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
