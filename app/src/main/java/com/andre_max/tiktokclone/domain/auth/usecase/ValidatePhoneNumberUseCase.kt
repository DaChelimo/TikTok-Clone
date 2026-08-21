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
    operator fun invoke(e164PhoneNumber: String): Result<Unit, PhoneNumberError> {
        val trimmed = e164PhoneNumber.trim()

        if (trimmed.isBlank()) return Result.Error(PhoneNumberError.Empty)
        if (!trimmed.startsWith("+")) return Result.Error(PhoneNumberError.MissingCountryCode)

        val digits = trimmed.removePrefix("+")
        if (!digits.all { it.isDigit() }) return Result.Error(PhoneNumberError.NonNumeric)
        if (digits.length < MIN_DIGITS) return Result.Error(PhoneNumberError.TooShort)
        if (digits.length > MAX_DIGITS) return Result.Error(PhoneNumberError.TooLong)

        return Result.Success(Unit)
    }

    companion object {
        /** E.164: country code + subscriber number, at least 10 digits total. */
        const val MIN_DIGITS = 10

        /** E.164 hard maximum. */
        const val MAX_DIGITS = 15
    }
}
