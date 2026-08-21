/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.Result
import java.time.LocalDate
import java.time.Period
import javax.inject.Inject

/**
 * Validates that a date of birth clears the minimum sign-up age. Pure logic with no repo
 * dependency.
 *
 * [today] is a parameter rather than a hidden `LocalDate.now()` call so that boundary tests
 * (13th birthday tomorrow, leap-day birthdays, year boundaries) are deterministic.
 */
class ValidateAgeUseCase @Inject constructor() {

    /**
     * @return [Result.Success] if [dateOfBirth] is acceptable, otherwise the [AgeError] found.
     *
     * A future date is reported as [AgeError.DateInFuture] rather than [AgeError.TooYoung]: it is
     * also "under 13", but the future date is the more specific truth and the more useful message.
     *
     * Age is [Period.between]'s whole-year count, which is calendar-correct — it rolls a Feb 29
     * birthday over on Mar 1 in a non-leap year, and it does not treat a December-born child as a
     * year older on January 1st the way the legacy year-only comparison did (D5).
     */
    operator fun invoke(
        dateOfBirth: LocalDate,
        today: LocalDate = LocalDate.now(),
    ): Result<Unit, AgeError> {
        if (dateOfBirth.isAfter(today)) return Result.Error(AgeError.DateInFuture)

        val ageInYears = Period.between(dateOfBirth, today).years

        return when {
            ageInYears < MIN_AGE -> Result.Error(AgeError.TooYoung)
            ageInYears > MAX_AGE -> Result.Error(AgeError.ImplausiblyOld)
            else -> Result.Success(Unit)
        }
    }

    companion object {
        /** D5: minimum age is 13, computed as true age from the full date of birth. */
        const val MIN_AGE = 13

        /** Preserved from the legacy `AgeFragment` upper bound. */
        const val MAX_AGE = 150
    }
}
