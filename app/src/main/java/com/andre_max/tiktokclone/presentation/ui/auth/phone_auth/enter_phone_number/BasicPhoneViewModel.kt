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

package com.andre_max.tiktokclone.presentation.ui.auth.phone_auth.enter_phone_number

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.andre_max.tiktokclone.userToken
import com.andre_max.tiktokclone.userVerificationId
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BasicPhoneViewModel : BaseViewModel() {

    val livePhoneNumber = MutableLiveData("")
    val isValid = Transformations.map(livePhoneNumber) { phoneNumber ->
        phoneNumber.length == 9
    }

    private val _liveCredential = MutableLiveData<AuthCredential?>()
    val liveCredential: LiveData<AuthCredential?> = _liveCredential

    fun sendCode(countryCode: String, activity: Activity, onComplete: (String?) -> Unit) {
        val phoneNumber = countryCode + livePhoneNumber.value

        val phoneAuthOptions = PhoneAuthOptions
            .newBuilder()
            .setPhoneNumber(phoneNumber)
            .setTimeout(60, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(getPhoneAuthCallback(onComplete))
            .build()

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
    }

    private fun getPhoneAuthCallback(onComplete: (String?) -> Unit) =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                _liveCredential.value = credential
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Timber.e(p0)
                onComplete(null)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                userVerificationId = verificationId
                userToken = token
                Timber.d("verificationId is $verificationId")
                onComplete(livePhoneNumber.value)
            }
        }

    fun resetLiveCredential() {
        _liveCredential.value = null
    }
}