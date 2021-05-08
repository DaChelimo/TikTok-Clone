package com.andre_max.tiktokclone.utils

import com.andre_max.tiktokclone.utils.TimeUtils.convertTimeToDisplayTime
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimeUtilsTest {
    private val minute = 60 * 60

    private val testSeconds45 = 45 * 60
    private val resultSeconds45 = "00:45"

    private val testMinutes30 = 30 * minute
    private val resultMinutes30 = "30:00"

    private val testMinutes75 = 75 * minute
    private val resultMinutes75 = "01:15:00"

    private val testHours2 = 120 * minute
    private val resultHours2 = "02:00:00"

    @Test
    fun `test convertTimeToDisplayTime`() {
        assertThat(convertTimeToDisplayTime(testSeconds45.toLong()))
            .isEqualTo(resultSeconds45)
        assertThat(convertTimeToDisplayTime(testMinutes30.toLong()))
            .isEqualTo(resultMinutes30)
        assertThat(convertTimeToDisplayTime(testMinutes75.toLong()))
            .isEqualTo(resultMinutes75)
        assertThat(convertTimeToDisplayTime(testHours2.toLong()))
            .isEqualTo(resultHours2)
    }

}