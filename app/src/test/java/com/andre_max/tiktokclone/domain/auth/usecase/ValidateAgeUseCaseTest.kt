/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * Screen 1 (spec §4.1).
 *
 * `today` is always passed explicitly, so none of these depend on the wall clock.
 *
 * The legacy `AgeFragment` compared **years only** (`date.year > year - 9`), which is both the
 * wrong minimum age and the wrong arithmetic. D5 replaces it with a true age computed from the
 * full date; the year-boundary cases below are the ones that separate the two.
 */
class ValidateAgeUseCaseTest {

    private val validateAge = ValidateAgeUseCase()

    @Test
    fun `turning 13 exactly today is accepted because the boundary is inclusive`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(2013, 6, 15),
            today = LocalDate.of(2026, 6, 15),
        )

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `13th birthday one day away returns TooYoung`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(2013, 6, 15),
            today = LocalDate.of(2026, 6, 14),
        )

        assertThat(result).isEqualTo(Result.Error(AgeError.TooYoung))
    }

    @Test
    fun `13th birthday one day past is accepted`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(2013, 6, 15),
            today = LocalDate.of(2026, 6, 16),
        )

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `born Dec 31 and evaluated on Jan 1 is still 12 and returns TooYoung`() {
        // The D5 bug in one case: a year-only comparison sees 2026 - 2013 == 13 and lets this
        // 12-year-old through. True age is 12 — the 13th birthday is 2026-12-31.
        val result = validateAge(
            dateOfBirth = LocalDate.of(2013, 12, 31),
            today = LocalDate.of(2026, 1, 1),
        )

        assertThat(result).isEqualTo(Result.Error(AgeError.TooYoung))
    }

    @Test
    fun `born Jan 1 and evaluated on Dec 31 of the 13th year is accepted`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(2013, 1, 1),
            today = LocalDate.of(2026, 12, 31),
        )

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `leap day birthday is not yet 13 on Feb 28 of a non-leap year`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(2012, 2, 29),
            today = LocalDate.of(2025, 2, 28),
        )

        assertThat(result).isEqualTo(Result.Error(AgeError.TooYoung))
    }

    @Test
    fun `leap day birthday is 13 on Mar 1 of a non-leap year`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(2012, 2, 29),
            today = LocalDate.of(2025, 3, 1),
        )

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `born today returns TooYoung`() {
        val today = LocalDate.of(2026, 7, 30)

        val result = validateAge(dateOfBirth = today, today = today)

        assertThat(result).isEqualTo(Result.Error(AgeError.TooYoung))
    }

    @Test
    fun `tomorrow returns DateInFuture`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(2026, 7, 31),
            today = LocalDate.of(2026, 7, 30),
        )

        assertThat(result).isEqualTo(Result.Error(AgeError.DateInFuture))
    }

    @Test
    fun `a far future date returns DateInFuture and not TooYoung`() {
        // A future date is also "under 13"; DateInFuture is the more specific truth, and this
        // rule used to be enforced only by the DatePicker's maxDate.
        val result = validateAge(
            dateOfBirth = LocalDate.of(2050, 1, 1),
            today = LocalDate.of(2026, 7, 30),
        )

        assertThat(result).isEqualTo(Result.Error(AgeError.DateInFuture))
    }

    @Test
    fun `exactly 150 years old is accepted because the upper bound is inclusive`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(1876, 7, 30),
            today = LocalDate.of(2026, 7, 30),
        )

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `151 years ago returns ImplausiblyOld`() {
        val result = validateAge(
            dateOfBirth = LocalDate.of(1875, 7, 30),
            today = LocalDate.of(2026, 7, 30),
        )

        assertThat(result).isEqualTo(Result.Error(AgeError.ImplausiblyOld))
    }
}
