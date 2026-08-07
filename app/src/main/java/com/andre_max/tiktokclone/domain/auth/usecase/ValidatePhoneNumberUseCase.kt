/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.Result
import javax.inject.Inject

/**
 * Validates that a phone number is in E.164 form before it is handed to Firebase. Pure logic,
 * no I/O.
 */
class ValidatePhoneNumberUseCase @Inject constructor() {

    /**
     * @param e164PhoneNumber the full number including the `+` and country code. Surrounding
     * whitespace is trimmed; anything else non-numeric is a rejection.
     */
    operator fun invoke(e164PhoneNumber: String): Result<Unit, PhoneNumberError> = TODO("Pass 2")

    companion object {
        /** E.164: country code + subscriber number, at least 4 digits total. */
        const val MIN_DIGITS = 4

        /** E.164 hard maximum. */
        const val MAX_DIGITS = 15
    }
}
