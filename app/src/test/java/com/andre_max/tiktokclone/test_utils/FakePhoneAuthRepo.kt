/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.auth.PhoneAuthRepo
import com.andre_max.tiktokclone.domain.auth.PhoneAuthStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Hand-written [PhoneAuthRepo] double — deterministic, no mocking framework.
 *
 * Mirrors the real implementation's `callbackFlow` shape so that a ViewModel test can observe
 * whether the collector was torn down ([wasClosed]).
 */
class FakePhoneAuthRepo : PhoneAuthRepo {

    /** Statuses emitted, in order, for every request. */
    var statuses: List<PhoneAuthStatus> = emptyList()

    /** Numbers passed to [requestVerificationCode], in call order. */
    val requestedNumbers = mutableListOf<String>()

    /** True once the returned flow's `awaitClose` has run. */
    var wasClosed: Boolean = false
        private set

    override fun requestVerificationCode(e164PhoneNumber: String): Flow<PhoneAuthStatus> =
        callbackFlow {
            requestedNumbers += e164PhoneNumber
            statuses.forEach { trySend(it) }
            awaitClose { wasClosed = true }
        }
}
