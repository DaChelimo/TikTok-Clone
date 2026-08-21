/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import javax.inject.Inject

/**
 * Validates a password against the app's strength rules. Pure logic with no repo dependency.
 *
 * Requirements: between [MIN_LENGTH] and [MAX_LENGTH] characters, at least one digit, special
 * character, uppercase letter and lowercase letter, and no occurrence of the user's own
 * identity.
 *
 * Returns a `List` rather than a `Result` (D6): a validation that reports *all* failures has no
 * single error to wrap, and an empty list is an unambiguous "valid". The UI renders a live rule
 * checklist off this list, which is the whole point of reporting every failure.
 */
class ValidatePasswordUseCase @Inject constructor() {

    /**
     * @param identity the email local-part or username. Matching is a **case-insensitive
     * substring** check, skipped entirely when [identity] is null or shorter than
     * [MIN_IDENTITY_LENGTH] (a 2-character name would reject almost every password).
     *
     * @return an empty list when [password] is valid; otherwise EVERY rule violated, in
     * [PasswordError] declaration order.
     */
    operator fun invoke(password: String, identity: String? = null): List<PasswordError> =
        buildList {
            if (password.length < MIN_LENGTH) add(PasswordError.TooShort)
            if (password.length > MAX_LENGTH) add(PasswordError.TooLong)
            if (password.none(Char::isDigit)) add(PasswordError.NoDigit)
            // "Special" is defined by exclusion rather than an allow-list, so that a character the
            // app never anticipated (a currency symbol, an emoji) still counts instead of silently
            // failing a rule the user believes they satisfied.
            if (password.all(Char::isLetterOrDigit)) add(PasswordError.NoSpecialCharacter)
            if (password.none(Char::isUpperCase)) add(PasswordError.NoUppercase)
            if (password.none(Char::isLowerCase)) add(PasswordError.NoLowercase)
            if (containsIdentity(password, identity)) add(PasswordError.ContainsIdentity)
        }

    private fun containsIdentity(password: String, identity: String?): Boolean =
        identity != null &&
            identity.length >= MIN_IDENTITY_LENGTH &&
            password.contains(identity, ignoreCase = true)

    companion object {
        const val MIN_LENGTH = 8

        /** D9 — matches the Mobbin checklist exactly. */
        const val MAX_LENGTH = 20

        /** Identities shorter than this are ignored by the containment rule. */
        const val MIN_IDENTITY_LENGTH = 3
    }
}
