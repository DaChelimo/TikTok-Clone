/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.sign_up

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import timber.log.Timber

/**
 * Runs the Play Services account chooser and returns the Google ID token, or null.
 *
 * An [ActivityResultContract] rather than the old pattern of a launcher plus a separate
 * `handleGoogleOnResult` on a repo: the request and its result belong to the same object, so
 * there is no way to launch it and forget to wire the result up.
 *
 * Firebase is deliberately absent here — this produces a raw ID token, and turning that into a
 * session is `AuthRepo.signInWithGoogle`'s job in the data layer.
 */
class GoogleSignInContract(
    private val webClientId: String,
) : ActivityResultContract<Unit, String?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestProfile()
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, options).signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? = try {
        GoogleSignIn.getSignedInAccountFromIntent(intent)
            .getResult(ApiException::class.java)
            ?.idToken
    } catch (e: ApiException) {
        // Thrown for a cancelled chooser as well as for a real failure (status 10 =
        // DEVELOPER_ERROR, which is what an unregistered SHA-1 or a foreign web client id
        // looks like from here). Logged rather than rethrown; the caller treats null as
        // "no token" and lets the user try again.
        Timber.e(e, "Google sign-in returned no ID token")
        null
    }
}
