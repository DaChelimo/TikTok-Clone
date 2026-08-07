/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.Result
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ValidateEmailUseCaseTest {

    private val validateEmail = ValidateEmailUseCase()

    @Test
    fun `well-formed email returns Success`() {
        val result = validateEmail("andrew@example.com")

        assertThat(result).isInstanceOf(Result.Success::class.java)
    }

    @Test
    fun `surrounding whitespace is trimmed before validating`() {
        val result = validateEmail("  andrew@example.com  ")

        assertThat(result).isInstanceOf(Result.Success::class.java)
    }

    @Test
    fun `blank email returns Empty`() {
        val result = validateEmail("   ")

        assertThat(result).isEqualTo(Result.Error(EmailError.Empty))
    }

    @Test
    fun `email with no at sign returns InvalidFormat`() {
        val result = validateEmail("andrew.example.com")

        assertThat(result).isEqualTo(Result.Error(EmailError.InvalidFormat))
    }

    @Test
    fun `email with no domain dot returns InvalidFormat`() {
        val result = validateEmail("andrew@example")

        assertThat(result).isEqualTo(Result.Error(EmailError.InvalidFormat))
    }

    @Test
    fun `email with internal whitespace returns InvalidFormat`() {
        val result = validateEmail("and rew@example.com")

        assertThat(result).isEqualTo(Result.Error(EmailError.InvalidFormat))
    }

    @Test
    fun `emptiness is checked before format`() {
        // an empty string fails both rules — Empty is checked first, so it wins
        val result = validateEmail("")

        assertThat(result).isEqualTo(Result.Error(EmailError.Empty))
    }
}
