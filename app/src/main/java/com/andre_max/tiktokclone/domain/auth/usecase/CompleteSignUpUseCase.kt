/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.domain.auth.UserProfileRepo
import com.andre_max.tiktokclone.domain.auth.UsernameRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import javax.inject.Inject

/**
 * Turns collected credentials + a chosen username into a usable account.
 *
 * **The account is created here, on the last screen, not on the screen that collected the
 * credentials.** That ordering is inherited deliberately from the legacy
 * `create_username.CreateUsernameViewModel`, whose comment gives the reason: a user who abandons
 * sign-up halfway would otherwise leave a real, credentialed account behind with no username and
 * no profile.
 *
 * The three writes are not transactional — Firebase Auth and Realtime Database have no shared
 * transaction — so each step undoes the ones before it on failure. Rollback is best-effort by
 * construction: if the undo itself fails there is nothing further to try, and the original
 * failure is what the user needs to see, so undo errors are discarded rather than replacing it.
 */
class CompleteSignUpUseCase @Inject constructor(
    private val authRepo: AuthRepo,
    private val usernameRepo: UsernameRepo,
    private val userProfileRepo: UserProfileRepo,
) {

    suspend operator fun invoke(
        credentials: SignUpCredentials,
        username: String,
    ): EmptyResult<DataError> {
        val uid = when (val account = createAccount(credentials)) {
            is Result.Success -> account.data
            is Result.Error -> return Result.Error(account.error)
        }

        // Claimed before the profile is written, so two users racing the same name cannot both
        // end up with a profile pointing at it — the loser fails here, before anything durable
        // references the name.
        usernameRepo.claimUsername(username).let { claim ->
            if (claim is Result.Error) {
                authRepo.deleteCurrentUser()
                return Result.Error(claim.error)
            }
        }

        val profilePictureUrl = (credentials as? SignUpCredentials.Google)?.profilePictureUrl
        userProfileRepo.createProfile(uid, username, profilePictureUrl).let { profile ->
            if (profile is Result.Error) {
                usernameRepo.releaseUsername(username)
                authRepo.deleteCurrentUser()
                return Result.Error(profile.error)
            }
        }

        return Result.Success(Unit)
    }

    private suspend fun createAccount(
        credentials: SignUpCredentials,
    ): Result<String, DataError.Auth> = when (credentials) {
        is SignUpCredentials.Email ->
            authRepo.signUpWithEmail(credentials.email, credentials.password)
        is SignUpCredentials.Google ->
            authRepo.signInWithGoogle(credentials.idToken)
    }
}
