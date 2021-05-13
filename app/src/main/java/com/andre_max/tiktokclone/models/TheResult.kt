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

package com.andre_max.tiktokclone.models

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class TheResult<out R> {

    data class Success<out T>(val data: T) : TheResult<T>()
    data class Error(val exception: Exception) : TheResult<Nothing>()
    object Loading : TheResult<Nothing>()

    fun tryData() = (this as? Success)?.data
    fun forceData() = (this as Success).data

    fun error() = (this as? Error)?.exception

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"
        }
    }

    companion object {
        fun <T> theSuccess(data: T) = Success(data)
        fun theError(exception: Exception) = Error(exception)
    }
}

/**
 * `true` if [TheResult] is of type [Success] & holds non-null [Success.data].
 */
val TheResult<*>.succeeded
    get() = this is TheResult.Success && data != null