/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Screen 5 (spec §4.5).
 *
 * The legacy `BasicPhoneViewModel` had no domain rule at all — it checked
 * `livePhoneNumber.length == 9` on the subscriber part and concatenated whatever dial code the
 * country picker held. These tests pin the E.164 rule that replaces it.
 */
class ValidatePhoneNumberUseCaseTest {

    private val validatePhoneNumber = ValidatePhoneNumberUseCase()

    @Test
    fun `a well-formed E164 number is accepted`() {
        val result = validatePhoneNumber("+254712345678")

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `a blank number returns Empty`() {
        val result = validatePhoneNumber("")

        assertThat(result).isEqualTo(Result.Error(PhoneNumberError.Empty))
    }

    @Test
    fun `a whitespace-only number returns Empty`() {
        val result = validatePhoneNumber("   ")

        assertThat(result).isEqualTo(Result.Error(PhoneNumberError.Empty))
    }

    @Test
    fun `a number without a leading plus returns MissingCountryCode`() {
        val result = validatePhoneNumber("254712345678")

        assertThat(result).isEqualTo(Result.Error(PhoneNumberError.MissingCountryCode))
    }

    @Test
    fun `a number with letters returns NonNumeric`() {
        val result = validatePhoneNumber("+2547CALLME1")

        assertThat(result).isEqualTo(Result.Error(PhoneNumberError.NonNumeric))
    }

    @Test
    fun `a number with embedded punctuation returns NonNumeric`() {
        val result = validatePhoneNumber("+254-712-345-678")

        assertThat(result).isEqualTo(Result.Error(PhoneNumberError.NonNumeric))
    }

    @Test
    fun `surrounding whitespace is trimmed before validating`() {
        val result = validatePhoneNumber("  +254712345678  ")

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `fewer than 4 digits returns TooShort`() {
        val result = validatePhoneNumber("+254")

        assertThat(result).isEqualTo(Result.Error(PhoneNumberError.TooShort))
    }

    @Test
    fun `exactly 4 digits is accepted because the lower bound is inclusive`() {
        val result = validatePhoneNumber("+2541")

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `exactly 15 digits is accepted because that is the E164 maximum`() {
        val result = validatePhoneNumber("+" + "1".repeat(15))

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `more than 15 digits returns TooLong`() {
        val result = validatePhoneNumber("+" + "1".repeat(16))

        assertThat(result).isEqualTo(Result.Error(PhoneNumberError.TooLong))
    }

    @Test
    fun `a lone plus returns TooShort rather than MissingCountryCode`() {
        val result = validatePhoneNumber("+")

        assertThat(result).isEqualTo(Result.Error(PhoneNumberError.TooShort))
    }
}
