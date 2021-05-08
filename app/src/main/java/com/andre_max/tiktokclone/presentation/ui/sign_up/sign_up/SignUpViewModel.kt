package com.andre_max.tiktokclone.presentation.ui.sign_up.sign_up

import androidx.lifecycle.LiveData
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.repo.network.auth.AuthRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import timber.log.Timber

class SignUpViewModel : BaseViewModel() {

    private val authRepo = AuthRepo()
    val googleAuthRepo = authRepo.GoogleAuthRepo()
    val twitterAuthRepo = authRepo.TwitterAuthRepo()
    val facebookAuthRepo = authRepo.FacebookAuthRepo()

    val liveCredential: LiveData<AuthCredential>
        get() = authRepo.liveCredential

    // If the user uses google sign in, this will have a value
    val googleAccount: GoogleSignInAccount?
        get() = authRepo.liveGoogleAccount.value

    private val mAuthListener = FirebaseAuth.AuthStateListener { p0 ->
        if (p0.currentUser != null) Timber.i("Sign in succeeded") else Timber.i("Sign in failed")
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
        Firebase.auth.addAuthStateListener(mAuthListener)
    }

    override fun onCleared() {
        super.onCleared()
        Firebase.auth.removeAuthStateListener(mAuthListener)
    }
}