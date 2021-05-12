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

package com.andre_max.tiktokclone.repo.network.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andre_max.tiktokclone.BuildConfig
import com.andre_max.tiktokclone.models.sign_up.EmailBody
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

// TODO: Abstract this to an interface to follow Dependency Inversion (SOLID)
class AuthRepo(
    private val fireAuth: FirebaseAuth = Firebase.auth
) {

    val liveGoogleAccount = MutableLiveData<GoogleSignInAccount>()

    private val _liveCredential = MutableLiveData<AuthCredential>()
    val liveCredential: LiveData<AuthCredential> = _liveCredential

    suspend fun signUpWithEmailBody(emailBody: EmailBody) = safeAccess {
        fireAuth.createUserWithEmailAndPassword(emailBody.email, emailBody.password).await()
    }

    suspend fun logInWithEmailBody(emailBody: EmailBody) = safeAccess {
        fireAuth.signInWithEmailAndPassword(emailBody.email, emailBody.password).await()
    }


    suspend fun signInWithCredential(credential: AuthCredential) = safeAccess {
        fireAuth.signInWithCredential(credential).await()
    }

    inner class FacebookAuthRepo {
        private val callbackManager: CallbackManager by lazy { CallbackManager.Factory.create() }
        private val loginManager: LoginManager by lazy { LoginManager.getInstance() }

        fun doFacebookLogin(activity: Activity) {
            loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Timber.d("facebook:onSuccess:$loginResult")

                    _liveCredential.value =
                        FacebookAuthProvider.getCredential(loginResult.accessToken.token)
                }

                override fun onCancel() {
                    Timber.i("facebook:onCancel() called")
                }

                override fun onError(exception: FacebookException) {
                    Timber.e(exception, "Facebook")
                }
            })

            loginManager.logInWithReadPermissions(activity, listOf("email"))
        }

        fun handleFacebookOnResult(requestCode: Int, resultCode: Int, data: Intent?) =
            callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    inner class GoogleAuthRepo {
        private lateinit var googleSignInClient: GoogleSignInClient

        fun doGoogleAuth(context: Context, launcher: ActivityResultLauncher<Intent>) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .requestProfile()
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
            launcher.launch(googleSignInClient.signInIntent)
        }

        fun handleGoogleOnResult(data: Intent?) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task?.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

                Timber.d("middle of handleGoogleOnResult with credential is $credential")

                liveGoogleAccount.value = account ?: return
                _liveCredential.value = credential
            } catch (apiException: ApiException) { // Thrown when the user exits the sign up process midway
                Timber.e(apiException, "Caught exception")
            }
        }
    }

    inner class TwitterAuthRepo {
        private val provider = OAuthProvider.newBuilder("twitter.com").build()

        /**
         * This carries out sign up using Twitter
         *
         * @param activity an activity instance is required to get a twitter auth result
         */
        suspend fun doTwitterSignUp(activity: Activity) {
            val authResult = getTwitterAuthResult(activity).await()
            Timber.d("middle of doTwitterSignUp with authResult is $authResult")
            _liveCredential.value = authResult.credential
        }

        /**
         * This function gets a twitter auth result by checking if we have an existing auth result
         * and providing a new sign up flow if not present. The main reason we may have an existing auth result is because
         * Twitter sign-in creates a new activity and puts ours in the background hence the system may chose
         * to reclaim it.
         *
         * @param activity an activity instance required to get a new twitter auth result
         */
        private fun getTwitterAuthResult(activity: Activity) =
            fireAuth.pendingAuthResult ?: fireAuth
                .startActivityForSignInWithProvider(activity, provider)
    }
}