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

object NumbersUtils {

    fun formatCount(count: Int): String {
        val stringCount = count.toString()
        Timber.d("count is $count")
        return when {
            count > 1_000_000_000 -> "${stringCount[0]}.${stringCount[1]}B"
            count > 100_000_000 -> "${stringCount.take(3)}.${stringCount[3]}M"
            count > 10_000_000 -> "${stringCount.take(2)}.${stringCount[2]}M"
            count > 1_000_000 -> "${stringCount[0]}.${stringCount[1]}M"
            count > 100_000 -> "${stringCount.take(3)}.${stringCount[3]}K"
            count > 10_000 -> "${stringCount.take(2)}.${stringCount[2]}K"
            count > 1_000 -> "${stringCount[0]}.${stringCount[1]}K"
            else -> stringCount
        }
    }

}