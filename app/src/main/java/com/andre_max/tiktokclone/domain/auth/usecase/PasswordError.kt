/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.AppError

/**
 * Reasons [ValidatePasswordUseCase] can reject a password.
 *
 * Scoped to the auth feature rather than the shared `DataError` file, since no other feature
 * deals with passwords.
 *
 * The declaration order below is the order [ValidatePasswordUseCase] reports failures in (D6
 * returns *every* violated rule, so the order is part of the contract).
 */
sealed interface PasswordError : AppError {
    /** Fewer than [ValidatePasswordUseCase.MIN_LENGTH] characters. */
    data object TooShort : PasswordError

    /** More than [ValidatePasswordUseCase.MAX_LENGTH] characters (D9). */
    data object TooLong : PasswordError

    /** Contains no digit. */
    data object NoDigit : PasswordError

    /** Contains no special (non-alphanumeric) character. */
    data object NoSpecialCharacter : PasswordError

    /** Contains no uppercase letter. */
    data object NoUppercase : PasswordError

    /** Contains no lowercase letter. */
    data object NoLowercase : PasswordError

    /**
     * Contains the user's own identity — email local-part or username (D6). Matching is
     * case-insensitive substring, and skipped when the identity is null or under 3 characters.
     */
    data object ContainsIdentity : PasswordError
}
