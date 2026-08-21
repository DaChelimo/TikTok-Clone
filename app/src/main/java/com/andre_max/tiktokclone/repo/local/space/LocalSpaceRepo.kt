/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.local.space

/**
 * Device storage checks. Compression moved to
 * [com.andre_max.tiktokclone.domain.upload.VideoCompressor] when the upload slice migrated.
 */
interface LocalSpaceRepo {

    /** Whether the device has enough free storage to record a new video. */
    fun hasEnoughSpace(): Boolean
}
