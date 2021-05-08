package com.andre_max.tiktokclone.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NumbersUtilsTest {

    val test120 = NumbersUtils.formatCount(120)
    val test1230 = NumbersUtils.formatCount(1230)
    val test12340 = NumbersUtils.formatCount(12_340)
    val test123_450 = NumbersUtils.formatCount(123_450)
    val test1_234_560 = NumbersUtils.formatCount(1_234_560)
    val test12_345_670 = NumbersUtils.formatCount(12_345_670)

    @Test
    fun formatCount() {
        assertThat(test120).isEqualTo("120")
        assertThat(test1230).isEqualTo("1.2K")
        assertThat(test12340).isEqualTo("12.3K")
        assertThat(test123_450).isEqualTo("123.4K")
        assertThat(test1_234_560).isEqualTo("1.2M")
        assertThat(test12_345_670).isEqualTo("12.3M")
    }
}