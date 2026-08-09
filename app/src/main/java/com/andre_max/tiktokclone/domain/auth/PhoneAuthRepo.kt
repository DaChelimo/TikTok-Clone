/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import kotlinx.coroutines.flow.Flow

/**
 * Phone verification, as the domain sees it.
 *
 * Firebase's phone auth is callback-based and needs an `Activity`; that is the implementation's
 * problem, not the domain's — the impl obtains its `Activity` from an injected provider and
 * bridges the callbacks with `callbackFlow` + `awaitClose`. No `Activity` appears here
 * (non-negotiable #6).
 */
interface PhoneAuthRepo {
    fun requestVerificationCode(e164PhoneNumber: String): Flow<PhoneAuthStatus>

    suspend fun signInWithCode(
        verificationId: String,
        code: String,
    ): Result<String, DataError.Auth>

    /**
     * Completes a sign-in Firebase already verified without the user typing anything.
     *
     * The provider credential from auto-retrieval never leaves the data layer — it is held
     * there and redeemed here — which is what lets [PhoneAuthStatus.AutoVerified] stay an empty
     * marker instead of carrying a Firebase type up into `domain`.
     *
     * @return an error if no auto-verified credential is pending.
     */
    suspend fun signInWithAutoVerified(): Result<String, DataError.Auth>
}

sealed interface PhoneAuthStatus {
    data class CodeSent(val verificationId: String) : PhoneAuthStatus

    /** Firebase auto-retrieved the SMS code; no code entry is needed. */
    data object AutoVerified : PhoneAuthStatus

    data class Failed(val error: DataError.Network) : PhoneAuthStatus
}
