/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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