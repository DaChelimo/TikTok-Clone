/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Screen 3 (spec §4.3).
 *
 * **Rewritten, not extended.** The previous suite asserted that only the *first* violated rule
 * came back; D6 deliberately removes that behavior in favour of returning every violated rule,
 * because the UI renders a live checklist rather than one error line. Keeping the old
 * `returns the FIRST rule violated` test would pin the exact behavior this migration exists to
 * change.
 */
class ValidatePasswordUseCaseTest {

    private val validatePassword = ValidatePasswordUseCase()

    // region individual rules

    @Test
    fun `password under 8 characters reports TooShort`() {
        val errors = validatePassword("Pw0rd!1")

        assertThat(errors).containsExactly(PasswordError.TooShort)
    }

    @Test
    fun `password over 20 characters reports TooLong`() {
        val errors = validatePassword("Passw0rd!Passw0rd!abc")

        assertThat(errors).containsExactly(PasswordError.TooLong)
    }

    @Test
    fun `password with no digit reports NoDigit`() {
        val errors = validatePassword("Password!")

        assertThat(errors).containsExactly(PasswordError.NoDigit)
    }

    @Test
    fun `password with no special character reports NoSpecialCharacter`() {
        val errors = validatePassword("Password1")

        assertThat(errors).containsExactly(PasswordError.NoSpecialCharacter)
    }

    @Test
    fun `password with no uppercase letter reports NoUppercase`() {
        val errors = validatePassword("password1!")

        assertThat(errors).containsExactly(PasswordError.NoUppercase)
    }

    @Test
    fun `password with no lowercase letter reports NoLowercase`() {
        val errors = validatePassword("PASSWORD1!")

        assertThat(errors).containsExactly(PasswordError.NoLowercase)
    }

    // endregion

    // region every failure, in order

    @Test
    fun `a password failing every rule reports all of them in declaration order`() {
        // "ab" — too short, no digit, no special char, no uppercase, and contains the identity.
        val errors = validatePassword(password = "ab", identity = "abc")

        assertThat(errors).containsExactly(
            PasswordError.TooShort,
            PasswordError.NoDigit,
            PasswordError.NoSpecialCharacter,
            PasswordError.NoUppercase,
        ).inOrder()
    }

    @Test
    fun `a short password missing a digit reports both rules rather than only the first`() {
        // The case the pre-D6 implementation got wrong: it stopped at TooShort.
        val errors = validatePassword("Pw!")

        assertThat(errors).containsAtLeast(PasswordError.TooShort, PasswordError.NoDigit)
    }

    // endregion

    // region length boundaries

    @Test
    fun `exactly 8 characters is accepted`() {
        val errors = validatePassword("Passw0r!")

        assertThat(errors).isEmpty()
    }

    @Test
    fun `exactly 20 characters is accepted`() {
        val password = "Passw0rd!Passw0rd!ab"
        check(password.length == 20)

        val errors = validatePassword(password)

        assertThat(errors).isEmpty()
    }

    @Test
    fun `exactly 21 characters reports TooLong`() {
        val password = "Passw0rd!Passw0rd!abc"
        check(password.length == 21)

        val errors = validatePassword(password)

        assertThat(errors).contains(PasswordError.TooLong)
    }

    // endregion

    // region identity containment (D6)

    @Test
    fun `password equal to the identity reports ContainsIdentity`() {
        val errors = validatePassword(password = "Andrew12!", identity = "Andrew12!")

        assertThat(errors).contains(PasswordError.ContainsIdentity)
    }

    @Test
    fun `password containing the identity reports ContainsIdentity`() {
        val errors = validatePassword(password = "xandrewx1!A", identity = "andrew")

        assertThat(errors).contains(PasswordError.ContainsIdentity)
    }

    @Test
    fun `identity containment is case-insensitive`() {
        val errors = validatePassword(password = "xANDREWx1!", identity = "andrew")

        assertThat(errors).contains(PasswordError.ContainsIdentity)
    }

    @Test
    fun `a null identity skips the containment rule`() {
        val errors = validatePassword(password = "Passw0rd!", identity = null)

        assertThat(errors).isEmpty()
    }

    @Test
    fun `an identity shorter than 3 characters is ignored`() {
        // "ab" appears in the password, but a 2-character name would reject almost everything.
        val errors = validatePassword(password = "abPassw0rd!", identity = "ab")

        assertThat(errors).isEmpty()
    }

    @Test
    fun `an identity of exactly 3 characters is still applied`() {
        val errors = validatePassword(password = "abcPassw0rd!", identity = "abc")

        assertThat(errors).contains(PasswordError.ContainsIdentity)
    }

    @Test
    fun `a password that merely shares letters with the identity is accepted`() {
        val errors = validatePassword(password = "Nwerda12!", identity = "andrew")

        assertThat(errors).isEmpty()
    }

    // endregion

    @Test
    fun `a valid password returns an empty list`() {
        val errors = validatePassword(password = "Passw0rd!", identity = "chelimo")

        assertThat(errors).isEmpty()
    }
}
