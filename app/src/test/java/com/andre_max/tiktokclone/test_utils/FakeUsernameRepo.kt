/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.auth.UsernameRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import kotlinx.coroutines.delay

/**
 * Hand-written [UsernameRepo] double for use-case and ViewModel tests — deterministic, no
 * mocking framework (spec §4.4). MockK is reserved for the Firebase boundary in
 * `DefaultUsernameRepoTest`.
 */
class FakeUsernameRepo : UsernameRepo {

    /** Usernames that are already claimed. */
    val takenUsernames = mutableSetOf<String>()

    /** When set, every availability check fails with this instead of answering. */
    var availabilityError: DataError.Network? = null

    /** When set, every claim fails with this. */
    var claimError: DataError.Network? = null

    /** Lets a test hold a lookup in flight, so debounce and staleness are observable. */
    var availabilityDelayMillis: Long = 0

    /** Every username passed to [isUsernameAvailable], in call order. */
    val availabilityQueries = mutableListOf<String>()

    /** Every username passed to [claimUsername], in call order. */
    val claimedUsernames = mutableListOf<String>()

    val availabilityCallCount: Int get() = availabilityQueries.size

    override suspend fun isUsernameAvailable(
        username: String,
    ): Result<Boolean, DataError.Network> {
        availabilityQueries += username
        if (availabilityDelayMillis > 0) delay(availabilityDelayMillis)
        availabilityError?.let { return Result.Error(it) }
        return Result.Success(username !in takenUsernames)
    }

    override suspend fun claimUsername(username: String): EmptyResult<DataError.Network> {
        claimedUsernames += username
        claimError?.let { return Result.Error(it) }
        takenUsernames += username
        return Result.Success(Unit)
    }
}
