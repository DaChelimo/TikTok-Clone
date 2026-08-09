/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.upload

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result

/** Compresses a video before upload, deleting the original on success. */
interface VideoCompressor {

    /** @return the path of the compressed file. */
    suspend fun compress(originalFilePath: String): Result<String, DataError.Local>
}
