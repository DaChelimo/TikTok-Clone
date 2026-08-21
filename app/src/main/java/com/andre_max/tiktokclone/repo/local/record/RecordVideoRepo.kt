/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.local.record

import com.andre_max.tiktokclone.models.local.LocalRecordLocation

/** Manages the MediaStore entry a camera recording writes into. */
interface RecordVideoRepo {

    /** Creates a pending MediaStore entry and opens its file descriptor for the camera. */
    suspend fun initVideo(timeCreated: Long): LocalRecordLocation?

    /** Publishes the pending entry (clears IS_PENDING) once recording ends. */
    suspend fun stopVideo()
}
