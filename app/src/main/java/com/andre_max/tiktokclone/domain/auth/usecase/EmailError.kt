/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.AppError

/**
 * Reasons [ValidateEmailUseCase] can reject an email address.
 *
 * Scoped to the auth feature rather than the shared `DataError` file, since no other feature
 * deals with emails.
 */
sealed interface EmailError : AppError {
    /** The field was blank. */
    data object Empty : EmailError

    /** The value is not a recognizable email address. */
    data object InvalidFormat : EmailError
}
