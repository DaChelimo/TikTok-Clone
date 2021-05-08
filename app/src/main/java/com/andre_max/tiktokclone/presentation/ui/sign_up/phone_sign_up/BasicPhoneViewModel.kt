package com.andre_max.tiktokclone.presentation.ui.sign_up.phone_sign_up

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