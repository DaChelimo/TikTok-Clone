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

import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {

    /**
     * Formats a duration as `mm:ss`, or `h:mm:ss` once it reaches an hour.
     *
     * Both components are zero-padded: without it `String.format("%d:%d", ...)` renders 45
     * seconds as "0:45" and 1 minute 5 seconds as "1:5", which reads as one minute and fifty
     * seconds. The hour component is dropped below an hour rather than shown as "00:", matching
     * how every video player labels a short clip.
     */
    fun convertTimeToDisplayTime(timeInMillis: Long): String {
        val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis)
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE

        val time = if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }

        Timber.d("TimeInMillis is $timeInMillis and time is $time")
        return time
    }

    private const val SECONDS_PER_MINUTE = 60
    private const val SECONDS_PER_HOUR = 60 * 60

}