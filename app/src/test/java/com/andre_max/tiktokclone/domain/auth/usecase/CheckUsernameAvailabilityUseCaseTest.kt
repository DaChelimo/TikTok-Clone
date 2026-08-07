/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.test_utils.FakeUsernameRepo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Screen 4, validation + I/O combined (spec §4.4).
 *
 * The repo double is a hand-written fake, not a mock — MockK is reserved for the Firebase
 * boundary in `DefaultUsernameRepoTest`.
 */
class CheckUsernameAvailabilityUseCaseTest {

    private val repo = FakeUsernameRepo()
    private val checkAvailability = CheckUsernameAvailabilityUseCase(
        validate = ValidateUsernameUseCase(),
        repo = repo,
    )

    @Test
    fun `a free username comes back as available`() = runTest {
        val result = checkAvailability("andrew_max")

        assertThat(result).isEqualTo(Result.Success(true))
    }

    @Test
    fun `a claimed username comes back as unavailable rather than as an error`() = runTest {
        repo.takenUsernames += "andrew_max"

        val result = checkAvailability("andrew_max")

        assertThat(result).isEqualTo(Result.Success(false))
    }

    @Test
    fun `an invalid username short-circuits before the repo is touched`() = runTest {
        checkAvailability("ab")

        assertThat(repo.availabilityCallCount).isEqualTo(0)
    }

    @Test
    fun `an invalid username returns its validation error`() = runTest {
        val result = checkAvailability("ab")

        assertThat(result).isEqualTo(Result.Error(UsernameError.TooShort))
    }

    @Test
    fun `a network failure mid-check returns UsernameError Network and never a Firebase type`() =
        runTest {
            repo.availabilityError = DataError.Network.NO_INTERNET

            val result = checkAvailability("andrew_max")

            assertThat(result)
                .isEqualTo(Result.Error(UsernameError.Network(DataError.Network.NO_INTERNET)))
        }

    @Test
    fun `a valid username is passed to the repo unchanged`() = runTest {
        checkAvailability("andrew_max")

        assertThat(repo.availabilityQueries).containsExactly("andrew_max")
    }
}
