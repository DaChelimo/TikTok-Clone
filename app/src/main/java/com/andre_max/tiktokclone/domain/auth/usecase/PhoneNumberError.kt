/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.AppError

/**
 * Reasons [ValidatePhoneNumberUseCase] can reject a phone number.
 *
 * Legacy `BasicPhoneViewModel` only checked `phoneNumber.length == 9` on the subscriber part and
 * concatenated a country code picked in the UI — i.e. there was no domain rule at all. These
 * errors are the domain rule that replaces it.
 */
sealed interface PhoneNumberError : AppError {
    data object Empty : PhoneNumberError

    /** Not in E.164 form — no leading `+` and country code. */
    data object MissingCountryCode : PhoneNumberError

    /** Contains something other than `+` and digits. */
    data object NonNumeric : PhoneNumberError

    /** Fewer than [ValidatePhoneNumberUseCase.MIN_DIGITS] digits after the `+`. */
    data object TooShort : PhoneNumberError

    /** More than [ValidatePhoneNumberUseCase.MAX_DIGITS] digits after the `+` (E.164 caps at 15). */
    data object TooLong : PhoneNumberError
}
