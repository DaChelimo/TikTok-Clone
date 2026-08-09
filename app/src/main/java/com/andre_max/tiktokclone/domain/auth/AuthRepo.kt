/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result

/**
 * The identity provider, as `domain` is allowed to see it.
 *
 * Every method takes and returns plain types. Social sign-in is expressed as
 * [signInWithGoogle] taking a raw ID token rather than a single `signInWithCredential` taking a
 * provider credential, because the latter is a Firebase type and would put the SDK on the wrong
 * side of the dependency boundary (AGENTS.md non-negotiable #6). Building the provider credential
 * is the repo's job.
 *
 * Coexists with the legacy `repo.network.auth.AuthRepo` until nothing references it
 * (strangler-fig, non-negotiable #2). That one hands out `AuthCredential` and `AuthResult`.
 */
interface AuthRepo {

    /** The signed-in user's id, or null when nobody is signed in. */
    fun currentUserId(): String?

    suspend fun signUpWithEmail(email: String, password: String): Result<String, DataError.Auth>

    suspend fun signInWithEmail(email: String, password: String): Result<String, DataError.Auth>

    suspend fun signInWithGoogle(idToken: String): Result<String, DataError.Auth>

    /**
     * Deletes the account that is currently signed in.
     *
     * Exists for sign-up rollback, not as a user-facing "delete my account" feature: an account
     * whose profile write failed is unreachable — the user owns credentials that resolve to no
     * profile — so it has to be undone rather than left behind. See `CompleteSignUpUseCase`.
     */
    suspend fun deleteCurrentUser(): EmptyResult<DataError.Auth>

    fun signOut()
}
