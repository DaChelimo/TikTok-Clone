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

package com.andre_max.tiktokclone.presentation.ui.auth.phone_auth.enter_code

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.repo.network.auth.AuthRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException

class EnterCodeViewModel : BaseViewModel() {

    private val authRepo = AuthRepo()

    private val _navigateToMyProfile = MutableLiveData(false)
    val navigateToMyProfile: LiveData<Boolean> = _navigateToMyProfile

    fun logInWithCredential(credential: AuthCredential) {
        viewModelScope.launch {
            val result = authRepo.signInWithCredential(credential)
            if (result.succeeded && result.tryData() != null)
                // User is logged in so let's navigate to their profile.
                _navigateToMyProfile.value = true
            else
                handleLoginError(result.error() ?: return@launch)
        }

    }

    private fun handleLoginError(exception: Exception) {
        Timber.e(exception, "handleLoginError:")
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                // The code that has been entered is invalid. Show a snackBar.
                showMessage(R.string.code_invalid)
            }
            is FirebaseTooManyRequestsException -> {
                // Too many requests from the same device have been sent to Firebase. Let's request the user to try again latter
                showMessage(R.string.too_many_requests)
            }
            is IOException -> {
                // No network connection
                showMessage(R.string.no_network_connection)
            }
        }
    }

    fun resetNavigateToMyProfile() {
        _navigateToMyProfile.value = false
    }
}