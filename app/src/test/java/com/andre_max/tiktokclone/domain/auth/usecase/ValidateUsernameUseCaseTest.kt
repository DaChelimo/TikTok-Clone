/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Screen 4, shape rules only (spec §4.4).
 *
 * These are the rules `DefaultNameRepo.getErrorFromUsername` enforces **today**, lifted out of
 * the data layer verbatim rather than replaced with a textbook rule set:
 *
 * ```
 * username.length < 4   -> R.string.short_username
 * username.contains(" ") -> R.string.name_contains_spaces
 * username.length >= 25 -> R.string.long_username
 * doesNameExist(username) -> R.string.name_unavailable
 * ```
 *
 * Two things about that legacy list are recorded rather than silently "fixed" — see the report
 * and spec §4.4: there is **no general allowed-character rule** (only spaces are rejected), and
 * the length bounds are asymmetric (`< 4` exclusive lower, `>= 25` exclusive upper, so 24 is
 * the longest accepted name).
 */
class ValidateUsernameUseCaseTest {

    private val validateUsername = ValidateUsernameUseCase()

    @Test
    fun `a blank username reports Empty`() {
        val errors = validateUsername("")

        assertThat(errors).contains(UsernameError.Empty)
    }

    @Test
    fun `a whitespace-only username reports Empty`() {
        val errors = validateUsername("   ")

        assertThat(errors).contains(UsernameError.Empty)
    }

    @Test
    fun `3 characters reports TooShort`() {
        val errors = validateUsername("and")

        assertThat(errors).containsExactly(UsernameError.TooShort)
    }

    @Test
    fun `exactly 4 characters is accepted because the lower bound is inclusive`() {
        val errors = validateUsername("andr")

        assertThat(errors).isEmpty()
    }

    @Test
    fun `24 characters is accepted because the legacy upper bound is exclusive`() {
        val username = "a".repeat(24)

        val errors = validateUsername(username)

        assertThat(errors).isEmpty()
    }

    @Test
    fun `exactly 25 characters reports TooLong because the legacy rule is length greater or equal 25`() {
        val username = "a".repeat(25)

        val errors = validateUsername(username)

        assertThat(errors).containsExactly(UsernameError.TooLong)
    }

    @Test
    fun `a username containing a space reports ContainsSpaces`() {
        val errors = validateUsername("andrew max")

        assertThat(errors).containsExactly(UsernameError.ContainsSpaces)
    }

    @Test
    fun `leading and trailing whitespace is rejected rather than silently trimmed`() {
        // The legacy rule is a plain `contains(" ")`, so " andrew" is invalid, not trimmed.
        val errors = validateUsername(" andrew ")

        assertThat(errors).contains(UsernameError.ContainsSpaces)
    }

    @Test
    fun `a short username with a space reports both rules`() {
        val errors = validateUsername("a b")

        assertThat(errors).containsExactly(
            UsernameError.TooShort,
            UsernameError.ContainsSpaces,
        ).inOrder()
    }

    @Test
    fun `punctuation and symbols are accepted because the legacy rules never restricted them`() {
        // Recorded, not endorsed: flagged in the Pass 1 report as a rule that looks wrong.
        val errors = validateUsername("an!@#\$rew")

        assertThat(errors).isEmpty()
    }

    @Test
    fun `a valid username returns an empty list`() {
        val errors = validateUsername("andrew_max")

        assertThat(errors).isEmpty()
    }
}
