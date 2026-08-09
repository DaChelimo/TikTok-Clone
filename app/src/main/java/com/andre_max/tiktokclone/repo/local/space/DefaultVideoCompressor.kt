/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.local.space

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.upload.VideoCompressor
import com.vincent.videocompressor.VideoCompress
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * [VideoCompressor] on the VideoCompress library, medium quality — the legacy
 * `DefaultLocalSpaceRepo.compressVideo` with its failure signal turned into a typed error
 * instead of a cancelled coroutine (which callers couldn't distinguish from leaving the
 * screen).
 */
class DefaultVideoCompressor : VideoCompressor {

    override suspend fun compress(originalFilePath: String): Result<String, DataError.Local> =
        withContext(Dispatchers.IO) {
            val videoDirectory = File(originalFilePath).parent
            val destPath = File(videoDirectory, generateFileName()).path

            if (!compressMedium(originalFilePath, destPath)) {
                return@withContext Result.Error(DataError.Local.COMPRESSION_FAILED)
            }

            deleteOriginalFile(originalFilePath)
            Result.Success(destPath)
        }

    /** Deleting the original is best-effort — the compressed copy is what gets uploaded. */
    private fun deleteOriginalFile(originalFilePath: String) {
        try {
            File(originalFilePath).delete()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun generateFileName() =
        "VIDEO_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.mp4"

    private suspend fun compressMedium(srcPath: String, destPath: String): Boolean =
        suspendCancellableCoroutine { cont ->
            VideoCompress.compressVideoMedium(
                srcPath,
                destPath,
                object : VideoCompress.CompressListener {
                    override fun onStart() = Unit

                    override fun onSuccess() {
                        cont.resume(true)
                    }

                    override fun onFail() {
                        cont.resume(false)
                    }

                    override fun onProgress(percent: Float) = Unit
                },
            )
        }
}
