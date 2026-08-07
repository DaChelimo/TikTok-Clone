/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.AppError

/**
 * Reasons [ValidateAgeUseCase] can reject a date of birth.
 *
 * Scoped to the auth feature rather than the shared `DataError` file — a validation failure is
 * not a data failure.
 */
sealed interface AgeError : AppError {
    /** Under [ValidateAgeUseCase.MIN_AGE] years old, computed from the full date (D5). */
    data object TooYoung : AgeError

    /**
     * The date of birth is after today. Previously only prevented by the `DatePicker`'s
     * `maxDate`, i.e. enforced by the UI alone; now a domain rule.
     */
    data object DateInFuture : AgeError

    /** Older than [ValidateAgeUseCase.MAX_AGE] years — preserves the legacy upper bound. */
    data object ImplausiblyOld : AgeError
}
