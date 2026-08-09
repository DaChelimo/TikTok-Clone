/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import javax.inject.Inject

/**
 * Validates the *shape* of a username. Pure logic, no I/O — availability lives in
 * [CheckUsernameAvailabilityUseCase].
 *
 * Rules are the ones `DefaultNameRepo.getErrorFromUsername` enforces today (spec §4.4), lifted
 * out of the data layer so nothing below `presentation` returns an `R.string` id.
 */
class ValidateUsernameUseCase @Inject constructor() {

    /** @return an empty list when valid; otherwise every rule violated, in declaration order. */
    operator fun invoke(username: String): List<UsernameError> {
        if (username.isBlank()) return listOf(UsernameError.Empty)

        return buildList {
            if (username.length < MIN_LENGTH) add(UsernameError.TooShort)
            if (username.contains(" ")) add(UsernameError.ContainsSpaces)
            if (username.length >= MAX_LENGTH) add(UsernameError.TooLong)
        }
    }

    companion object {
        /** Legacy: `username.length < 4 -> R.string.short_username`. */
        const val MIN_LENGTH = 4

        /** Legacy: `username.length >= 25 -> R.string.long_username` — bound is *exclusive*. */
        const val MAX_LENGTH = 25
    }
}
