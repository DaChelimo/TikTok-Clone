/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.phone

import com.andre_max.tiktokclone.domain.auth.PhoneAuthRepo
import com.andre_max.tiktokclone.domain.auth.PhoneAuthStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow

/**
 * Bridges Firebase's callback-based phone verification into a cold [Flow] with `callbackFlow` +
 * `awaitClose`, so cancelling the collector unregisters the Firebase callback rather than
 * leaking it.
 */
class DefaultPhoneAuthRepo(
    private val activityProvider: ActivityProvider,
    private val firebaseAuth: FirebaseAuth = Firebase.auth,
) : PhoneAuthRepo {

    override fun requestVerificationCode(e164PhoneNumber: String): Flow<PhoneAuthStatus> =
        TODO("Pass 2")
}
