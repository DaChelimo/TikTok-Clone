package com.andre_max.tiktokclone.utils

import android.app.Activity
import com.andre_max.tiktokclone.firebaseAuth
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

object FirebaseAuthUtils {

    fun getTwitterAuthResult(activity: Activity) =
        firebaseAuth.pendingAuthResult ?: firebaseAuth.startActivityForSignInWithProvider(
            activity, OAuthProvider.newBuilder("twitter.com").build()
        )

    fun createTwitterAccount(
        username: String,
        twitterResultTask: Task<AuthResult>,
        isSuccessLambda: (Boolean) -> Unit
    ) {
        twitterResultTask
            .addOnSuccessListener {
                Timber.d("Pending result successful")
                createAccountInFirestoreDatabase(username, it, isSuccessLambda)
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

    private fun createAccountInFirestoreDatabase(
        username: String,
        authResult: AuthResult,
        isSuccessLambda: (Boolean) -> Unit,
        googleProfilePicture: String = ""
    ) {
        GlobalScope.launch {
            FirestoreUtils.createAccountInDatabase(username, authResult, googleProfilePicture)
                .collect { resultUser ->
                    resultUser.getOrNull()?.let {
                        FirestoreUtils.registerUserName(username)
                        isSuccessLambda(true)
//                    findNavController().navigate(CreateUsernameFragmentDirections.actionCreateUsernameFragmentToHomeFragment())
                    } ?: run {
                        isSuccessLambda(false)
//                    ViewUtils.showLongToast(
//                        requireContext(),
//                        "Error occurred creating account in database. Try again later."
//                    )
                    }
                }
        }
    }
}