/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.local.media

import android.net.Uri
import com.andre_max.tiktokclone.models.local.LocalImage
import com.andre_max.tiktokclone.models.local.LocalVideo

/** The device's MediaStore gallery, read-only. */
interface LocalMediaRepo {

    suspend fun getAllImages(): List<LocalImage>

    suspend fun getAllVideos(): List<LocalVideo>

    suspend fun getVideoDuration(fileUri: Uri?): Long
}
