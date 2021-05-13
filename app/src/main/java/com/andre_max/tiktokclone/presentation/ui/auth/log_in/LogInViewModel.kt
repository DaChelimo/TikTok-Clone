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

package com.andre_max.tiktokclone.presentation.ui.auth.log_in

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.repo.network.auth.AuthRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import com.google.firebase.auth.AuthCredential
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import kotlinx.coroutines.launch
import timber.log.Timber

class LogInViewModel : BaseViewModel() {

    private val authRepo = AuthRepo()
    val googleAuthRepo = authRepo.GoogleAuthRepo()
    val twitterAuthRepo = authRepo.TwitterAuthRepo()
    val facebookAuthRepo = authRepo.FacebookAuthRepo()

    private val _navigateToMyProfile = MutableLiveData(false)
    val navigateToMyProfile: LiveData<Boolean> = _navigateToMyProfile.distinctUntilChanged()

    private val liveCredentialObserver: (AuthCredential?) -> Unit = { credential ->
        credential?.let {
            viewModelScope.launch {
                val result = authRepo.signInWithCredential(credential)
                if (!result.succeeded)
                    showMessage(R.string.error_occurred_during_log_in)

                _navigateToMyProfile.value = result.succeeded
            }
        }
    }

    val twitterCallback = object : Callback<TwitterSession>() {
        override fun success(result: Result<TwitterSession>?) {
            Timber.d("Twitter sign in successful")
            showMessage(R.string.successfully_signed_up)
        }

        override fun failure(exception: TwitterException?) {
            Timber.e(exception)
            showMessage(R.string.error_occurred_during_sign_up)
        }
    }

    init {
        authRepo.liveCredential.observeForever(liveCredentialObserver)
    }

    fun resetNavigateToMyProfile() {
        _navigateToMyProfile.value = false
    }

    override fun onCleared() {
        super.onCleared()
        authRepo.liveCredential.removeObserver(liveCredentialObserver)
    }
}