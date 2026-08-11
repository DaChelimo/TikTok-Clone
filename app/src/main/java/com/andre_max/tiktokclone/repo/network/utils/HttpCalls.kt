/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.utils

import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

/**
 * A non-2xx http response.
 *
 * Extends [IOException] so an unmapped one still reads as a network failure rather than an
 * unrelated crash, but carries [code] so [toNetworkError] can be specific about which failure.
 */
class HttpException(val code: Int, message: String) : IOException(message)

/**
 * Suspending [Call.execute] that aborts the in-flight request when the coroutine is cancelled.
 *
 * Built on [Call.enqueue] rather than a blocking execute on `Dispatchers.IO` precisely so that
 * cancellation propagates into the socket: a user leaving the upload screen mid-upload should
 * stop pushing bytes, not run the transfer to completion on a detached thread.
 */
suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }

    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }

        // The body is a live connection, so it must be closed on the path where the coroutine was
        // already cancelled — nothing downstream will ever get the response to close it itself.
        override fun onResponse(call: Call, response: Response) {
            if (continuation.isActive) continuation.resume(response) { response.close() }
            else response.close()
        }
    })
}
