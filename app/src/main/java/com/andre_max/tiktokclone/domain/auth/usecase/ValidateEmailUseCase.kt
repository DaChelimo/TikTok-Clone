/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.Result
import javax.inject.Inject

/**
 * Validates the format of an email address. Pure logic with no repo dependency.
 *
 * The check is intentionally a loose structural test (non-blank local part, an `@`, a dotted
 * domain) rather than a full RFC 5322 match: the authoritative check is the confirmation email
 * arriving, so a stricter rule here only risks rejecting valid addresses.
 */
class ValidateEmailUseCase @Inject constructor() {

    /**
     * @return [Result.Success] if [email] is well-formed, otherwise the first [EmailError] found.
     */
    operator fun invoke(email: String): Result<Unit, EmailError> {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> Result.Error(EmailError.Empty)
            !trimmed.matches(EMAIL_REGEX) -> Result.Error(EmailError.InvalidFormat)
            else -> Result.Success(Unit)
        }
    }

    private companion object {
        // local-part @ domain, where the domain has at least one dot and a 2+ char TLD.
        val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$")
    }
}
