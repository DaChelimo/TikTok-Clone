/*
 * MIT License
 *
 * Copyright (c) 2021 Andrew Chelimo
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

package com.andre_max.tiktokclone.domain.core

interface AppError

sealed interface Result<out D, out E : AppError> {
    data class Success<D>(val data: D) : Result<D, Nothing>
    data class Error<E : AppError>(val error: E) : Result<Nothing, E>
}

// region DataError

sealed interface DataError : AppError {
    enum class Network : DataError {
        NO_INTERNET,
        SERVER,
        TIMEOUT,
        UNAUTHORIZED,
        NOT_FOUND,
        UPLOAD_FAILED,
        UNKNOWN
    }

    enum class Local : DataError {
        INSUFFICIENT_STORAGE,
        FILE_NOT_FOUND,
        COMPRESSION_FAILED,
        PERMISSION_DENIED,
        UNKNOWN
    }
}

// endregion

// region Result extensions

/** Shorthand for an operation that returns no data on success, only an error on failure. */
typealias EmptyResult<E> = Result<Unit, E>

/** Runs [action] only if this is a Success, then returns the same result for chaining. */
inline fun <D, E : AppError> Result<D, E>.onSuccess(action: (D) -> Unit): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

/** Runs [action] only if this is an Error, then returns the same result for chaining. */
inline fun <D, E : AppError> Result<D, E>.onError(action: (E) -> Unit): Result<D, E> {
    if (this is Result.Error) action(error)
    return this
}

/** Transforms the success data, leaving an error untouched. */
inline fun <D, E : AppError, R> Result<D, E>.map(transform: (D) -> R): Result<R, E> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
    }

/** Collapses both branches into a single value. */
inline fun <D, E : AppError, R> Result<D, E>.fold(
    onSuccess: (D) -> R,
    onError: (E) -> R,
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error -> onError(error)
}

// endregion