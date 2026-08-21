/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result

/**
 * Hand-written [AuthRepo] double — deterministic, no mocking framework (spec §4.4).
 *
 * Records calls in order so that `CompleteSignUpUseCase`'s rollback sequencing is assertable,
 * which is the one thing about it that a return-value-only fake could not express.
 */
class FakeAuthRepo : AuthRepo {

    sealed interface Call {
        data class SignUpWithEmail(val email: String, val password: String) : Call
        data class SignInWithEmail(val email: String, val password: String) : Call
        data class SignInWithGoogle(val idToken: String) : Call
        data object DeleteCurrentUser : Call
        data object SignOut : Call
    }

    val calls = mutableListOf<Call>()

    /** The uid handed back by whichever sign-up/sign-in succeeds. */
    var uid: String = "uid-1"

    var signUpError: DataError.Auth? = null
    var signInError: DataError.Auth? = null
    var googleError: DataError.Auth? = null

    /** Lets a test make the rollback itself fail, to pin that the original error still surfaces. */
    var deleteError: DataError.Auth? = null

    var signedInUserId: String? = null

    override fun currentUserId(): String? = signedInUserId

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<String, DataError.Auth> {
        calls += Call.SignUpWithEmail(email, password)
        signUpError?.let { return Result.Error(it) }
        signedInUserId = uid
        return Result.Success(uid)
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<String, DataError.Auth> {
        calls += Call.SignInWithEmail(email, password)
        signInError?.let { return Result.Error(it) }
        signedInUserId = uid
        return Result.Success(uid)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<String, DataError.Auth> {
        calls += Call.SignInWithGoogle(idToken)
        googleError?.let { return Result.Error(it) }
        signedInUserId = uid
        return Result.Success(uid)
    }

    override suspend fun deleteCurrentUser(): EmptyResult<DataError.Auth> {
        calls += Call.DeleteCurrentUser
        deleteError?.let { return Result.Error(it) }
        signedInUserId = null
        return Result.Success(Unit)
    }

    override fun signOut() {
        calls += Call.SignOut
        signedInUserId = null
    }
}
