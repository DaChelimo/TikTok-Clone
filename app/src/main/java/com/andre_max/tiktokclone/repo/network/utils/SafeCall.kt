/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.utils

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

/**
 * The single place where a platform exception becomes a domain error.
 *
 * Everything ABOVE the data layer (use cases, ViewModels) sees only [DataError] — never a
 * FirebaseException. That's the whole point: the presentation layer must not know Firebase exists.
 *
 * NOTE the two catch blocks and their order:
 *  1. CancellationException is rethrown. If we swallowed it, a cancelled coroutine (user left the
 *     screen) would carry on as a "failure" and could update dead UI. Structured concurrency relies
 *     on cancellation propagating, so we MUST let it through.
 *  2. Only then do we catch everything else and translate it.
 *
 * This replaces the old `safeAccess`, which caught `Exception` (swallowing cancellation) and
 * returned the raw exception (leaking Firebase into every layer).
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T, DataError.Network> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        Result.Error(e.toNetworkError())
    }

private fun Throwable.toNetworkError(): DataError.Network = when (this) {
    is FirebaseNetworkException -> DataError.Network.NO_INTERNET
    is FirebaseAuthException -> DataError.Network.UNAUTHORIZED
    else -> DataError.Network.UNKNOWN
}
