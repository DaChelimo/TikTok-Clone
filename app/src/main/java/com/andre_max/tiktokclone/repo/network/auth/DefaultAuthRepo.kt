/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.auth

import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.map
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.repo.network.utils.safeAuthCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of the domain [AuthRepo].
 *
 * The provider credential is built here rather than passed in, which is what keeps
 * `GoogleAuthProvider` — and Firebase generally — out of `domain`.
 *
 * Coexists with the legacy [AuthRepo][com.andre_max.tiktokclone.repo.network.auth.AuthRepo] class
 * in this same package (strangler-fig, non-negotiable #2) until the legacy sign-up/log-in
 * ViewModels are gone.
 */
class DefaultAuthRepo(
    private val fireAuth: FirebaseAuth = Firebase.auth,
) : AuthRepo {

    override fun currentUserId(): String? = fireAuth.currentUser?.uid

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<String, DataError.Auth> = safeAuthCall {
        fireAuth.createUserWithEmailAndPassword(email, password).await()
    }.map { it.user?.uid.orEmpty() }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<String, DataError.Auth> = safeAuthCall {
        fireAuth.signInWithEmailAndPassword(email, password).await()
    }.map { it.user?.uid.orEmpty() }

    override suspend fun signInWithGoogle(idToken: String): Result<String, DataError.Auth> =
        safeAuthCall {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            fireAuth.signInWithCredential(credential).await()
        }.map { it.user?.uid.orEmpty() }

    override suspend fun deleteCurrentUser(): EmptyResult<DataError.Auth> = safeAuthCall {
        fireAuth.currentUser?.delete()?.await()
    }.map { }

    override fun signOut() = fireAuth.signOut()
}
