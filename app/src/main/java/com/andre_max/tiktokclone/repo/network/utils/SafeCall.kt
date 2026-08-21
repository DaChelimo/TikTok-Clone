/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.utils

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.io.IOException
import java.net.SocketTimeoutException
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

/**
 * Visible beyond this file so other repos (e.g. [com.andre_max.tiktokclone.repo.network.phone.DefaultPhoneAuthRepo], which maps a Firebase callback error rather than a caught exception) can reuse the same mapping instead of duplicating it.
 *
 * Order matters in the plain-http branches: [HttpException] and [java.net.SocketTimeoutException]
 * are both [IOException]s, so the broad `is IOException` arm has to come last or it would swallow
 * them and report every failed request as "no internet".
 */
internal fun Throwable.toNetworkError(): DataError.Network = when (this) {
    is FirebaseNetworkException -> DataError.Network.NO_INTERNET
    is FirebaseAuthException -> DataError.Network.UNAUTHORIZED
    is HttpException -> when (code) {
        401, 403 -> DataError.Network.UNAUTHORIZED
        404 -> DataError.Network.NOT_FOUND
        408 -> DataError.Network.TIMEOUT
        in 500..599 -> DataError.Network.SERVER
        else -> DataError.Network.UNKNOWN
    }
    is SocketTimeoutException -> DataError.Network.TIMEOUT
    is IOException -> DataError.Network.NO_INTERNET
    else -> DataError.Network.UNKNOWN
}

/**
 * [safeCall] for identity-provider calls, translating to [DataError.Auth] instead of
 * [DataError.Network].
 *
 * A separate entry point rather than a parameter on [safeCall] because the two differ only in
 * their error mapping and a boolean-ish switch would be the wrong abstraction (AGENTS.md §5).
 */
suspend fun <T> safeAuthCall(block: suspend () -> T): Result<T, DataError.Auth> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        Result.Error(e.toAuthError())
    }

/**
 * Collapses `ERROR_USER_NOT_FOUND` into [DataError.Auth.INVALID_CREDENTIALS] alongside a wrong
 * password, so that no caller can distinguish "no such account" from "wrong password" and
 * accidentally build an account-enumeration oracle out of the difference.
 */
private fun Throwable.toAuthError(): DataError.Auth = when (this) {
    is FirebaseNetworkException -> DataError.Auth.NO_INTERNET
    is FirebaseTooManyRequestsException -> DataError.Auth.TOO_MANY_REQUESTS
    is FirebaseAuthWeakPasswordException -> DataError.Auth.WEAK_PASSWORD
    is FirebaseAuthUserCollisionException -> DataError.Auth.EMAIL_ALREADY_IN_USE
    is FirebaseAuthInvalidCredentialsException -> DataError.Auth.INVALID_CREDENTIALS
    is FirebaseAuthInvalidUserException -> when (errorCode) {
        "ERROR_USER_DISABLED" -> DataError.Auth.ACCOUNT_DISABLED
        else -> DataError.Auth.INVALID_CREDENTIALS
    }
    else -> DataError.Auth.UNKNOWN
}
