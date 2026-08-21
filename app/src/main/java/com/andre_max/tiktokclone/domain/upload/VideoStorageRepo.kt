/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.upload

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result

/** Uploads a local video file to remote storage. */
interface VideoStorageRepo {

    /** @return the public download url of the uploaded video. */
    suspend fun uploadVideo(localFilePath: String): Result<String, DataError.Network>
}
