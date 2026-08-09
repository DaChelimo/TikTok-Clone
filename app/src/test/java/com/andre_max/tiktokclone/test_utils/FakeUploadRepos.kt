/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.upload.VideoCompressor
import com.andre_max.tiktokclone.domain.upload.VideoStorageRepo

/** Hand-written [VideoCompressor] double. */
class FakeVideoCompressor : VideoCompressor {

    val compressedPaths = mutableListOf<String>()
    var compressError: DataError.Local? = null

    /** The compressed path is derived so tests can assert the pipeline used it. */
    override suspend fun compress(originalFilePath: String): Result<String, DataError.Local> {
        compressError?.let { return Result.Error(it) }
        compressedPaths += originalFilePath
        return Result.Success("$originalFilePath.compressed")
    }
}

/** Hand-written [VideoStorageRepo] double. */
class FakeVideoStorageRepo : VideoStorageRepo {

    val uploadedPaths = mutableListOf<String>()
    var uploadError: DataError.Network? = null
    var downloadUrl = "https://storage.example/uploaded"

    override suspend fun uploadVideo(localFilePath: String): Result<String, DataError.Network> {
        uploadError?.let { return Result.Error(it) }
        uploadedPaths += localFilePath
        return Result.Success(downloadUrl)
    }
}
