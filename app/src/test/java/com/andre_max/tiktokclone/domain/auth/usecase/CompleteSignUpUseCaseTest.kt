/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.test_utils.FakeAuthRepo
import com.andre_max.tiktokclone.test_utils.FakePhoneAuthRepo
import com.andre_max.tiktokclone.test_utils.FakeUserProfileRepo
import com.andre_max.tiktokclone.test_utils.FakeUsernameRepo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * The step the Compose auth flow was missing entirely: nothing between the password screen and
 * Home ever created an account. These tests pin both the happy path and — more importantly —
 * that a partial failure leaves nothing behind.
 */
class CompleteSignUpUseCaseTest {

    private val authRepo = FakeAuthRepo()
    private val usernameRepo = FakeUsernameRepo()
    private val userProfileRepo = FakeUserProfileRepo()

    private val phoneAuthRepo = FakePhoneAuthRepo()

    private val completeSignUp = CompleteSignUpUseCase(
        authRepo = authRepo,
        usernameRepo = usernameRepo,
        userProfileRepo = userProfileRepo,
        phoneAuthRepo = phoneAuthRepo,
    )

    private val emailCredentials = SignUpCredentials.Email("andrew@example.com", "Passw0rd!")

    @Test
    fun `email sign-up creates the account, claims the name, and writes the profile`() = runTest {
        val result = completeSignUp(emailCredentials, "andrew_max")

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(authRepo.calls).containsExactly(
            FakeAuthRepo.Call.SignUpWithEmail("andrew@example.com", "Passw0rd!"),
        )
        assertThat(usernameRepo.claimedUsernames).containsExactly("andrew_max")
        assertThat(userProfileRepo.createdProfiles).containsExactly(
            FakeUserProfileRepo.CreatedProfile("uid-1", "andrew_max", null),
        )
    }

    @Test
    fun `the username is claimed before the profile that references it is written`() = runTest {
        // A profile pointing at a name someone else could still claim is the race this ordering
        // rules out, so the order is behaviour, not an implementation detail.
        usernameRepo.claimError = DataError.Network.SERVER

        completeSignUp(emailCredentials, "andrew_max")

        assertThat(userProfileRepo.createdProfiles).isEmpty()
    }

    @Test
    fun `google sign-up seeds the profile picture from the google account`() = runTest {
        val credentials = SignUpCredentials.Google("id-token", "https://example.com/me.jpg")

        completeSignUp(credentials, "andrew_max")

        assertThat(authRepo.calls).containsExactly(FakeAuthRepo.Call.SignInWithGoogle("id-token"))
        assertThat(userProfileRepo.createdProfiles).containsExactly(
            FakeUserProfileRepo.CreatedProfile("uid-1", "andrew_max", "https://example.com/me.jpg"),
        )
    }

    @Test
    fun `a failed account creation touches neither the username nor the profile`() = runTest {
        authRepo.signUpError = DataError.Auth.EMAIL_ALREADY_IN_USE

        val result = completeSignUp(emailCredentials, "andrew_max")

        assertThat(result).isEqualTo(Result.Error(DataError.Auth.EMAIL_ALREADY_IN_USE))
        assertThat(usernameRepo.claimedUsernames).isEmpty()
        assertThat(userProfileRepo.createdProfiles).isEmpty()
    }

    @Test
    fun `a failed username claim deletes the account it had already created`() = runTest {
        usernameRepo.claimError = DataError.Network.SERVER

        val result = completeSignUp(emailCredentials, "andrew_max")

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVER))
        assertThat(authRepo.calls).contains(FakeAuthRepo.Call.DeleteCurrentUser)
        assertThat(authRepo.currentUserId()).isNull()
    }

    @Test
    fun `a failed profile write releases the username and deletes the account`() = runTest {
        userProfileRepo.createError = DataError.Network.NO_INTERNET

        val result = completeSignUp(emailCredentials, "andrew_max")

        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
        assertThat(usernameRepo.releasedUsernames).containsExactly("andrew_max")
        assertThat(usernameRepo.takenUsernames).doesNotContain("andrew_max")
        assertThat(authRepo.calls).contains(FakeAuthRepo.Call.DeleteCurrentUser)
    }

    @Test
    fun `a rollback that itself fails still reports the original failure`() = runTest {
        userProfileRepo.createError = DataError.Network.NO_INTERNET
        authRepo.deleteError = DataError.Auth.UNKNOWN

        val result = completeSignUp(emailCredentials, "andrew_max")

        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
    }

    @Test
    fun `no username is left reserved when sign-up fails at the profile step`() = runTest {
        userProfileRepo.createError = DataError.Network.SERVER

        completeSignUp(emailCredentials, "andrew_max")

        // The name must be claimable again by a retry — this is the user-visible consequence of
        // the rollback, and the reason releaseUsername exists on the repo at all.
        assertThat(usernameRepo.isUsernameAvailable("andrew_max"))
            .isEqualTo(Result.Success(true))
    }

    @Test
    fun `phone sign-up redeems the code rather than the email or google paths`() = runTest {
        val result = completeSignUp(SignUpCredentials.Phone("vid", "123456"), "andrew_max")

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(phoneAuthRepo.submittedCodes).containsExactly("vid" to "123456")
        assertThat(authRepo.calls).isEmpty()
    }

    @Test
    fun `auto-verified phone sign-up redeems the credential held in the data layer`() = runTest {
        completeSignUp(SignUpCredentials.AutoVerifiedPhone, "andrew_max")

        assertThat(phoneAuthRepo.autoVerifiedRedemptions).isEqualTo(1)
        assertThat(usernameRepo.claimedUsernames).containsExactly("andrew_max")
    }

    @Test
    fun `a rejected sms code leaves no username claimed`() = runTest {
        phoneAuthRepo.signInError = DataError.Auth.INVALID_CREDENTIALS

        val result = completeSignUp(SignUpCredentials.Phone("vid", "000000"), "andrew_max")

        assertThat(result).isEqualTo(Result.Error(DataError.Auth.INVALID_CREDENTIALS))
        assertThat(usernameRepo.claimedUsernames).isEmpty()
    }
}
