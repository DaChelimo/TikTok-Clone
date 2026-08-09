/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.phone

import com.andre_max.tiktokclone.domain.auth.PhoneAuthRepo
import com.andre_max.tiktokclone.domain.auth.PhoneAuthStatus
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.repo.network.utils.toNetworkError
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Bridges Firebase's callback-based phone verification into a cold [Flow] with `callbackFlow` +
 * `awaitClose`, so cancelling the collector unregisters the Firebase callback rather than
 * leaking it.
 *
 * There is no Firebase API to cancel an in-flight `verifyPhoneNumber` call, so `awaitClose`'s
 * block is empty — the point of `callbackFlow` here is that cancelling the collector stops
 * `trySend` from reaching a dead collector, not that Firebase itself is told to stop.
 */
class DefaultPhoneAuthRepo(
    private val activityProvider: ActivityProvider,
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
) : PhoneAuthRepo {

    override fun requestVerificationCode(e164PhoneNumber: String): Flow<PhoneAuthStatus> =
        callbackFlow {
            val activity = activityProvider.current()
            if (activity == null) {
                trySend(PhoneAuthStatus.Failed(DataError.Network.UNKNOWN))
                close()
                return@callbackFlow
            }

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    trySend(PhoneAuthStatus.AutoVerified)
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    trySend(PhoneAuthStatus.Failed(exception.toNetworkError()))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    trySend(PhoneAuthStatus.CodeSent(verificationId))
                }
            }

            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(e164PhoneNumber)
                .setTimeout(60, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)

            awaitClose { }
        }
}
