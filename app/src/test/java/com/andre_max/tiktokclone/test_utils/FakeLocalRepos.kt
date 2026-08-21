/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import android.net.Uri
import com.andre_max.tiktokclone.models.local.LocalImage
import com.andre_max.tiktokclone.models.local.LocalRecordLocation
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.repo.local.media.LocalMediaRepo
import com.andre_max.tiktokclone.repo.local.record.RecordVideoRepo
import com.andre_max.tiktokclone.repo.local.space.LocalSpaceRepo

/** Hand-written doubles for the device-local upload repos. */

class FakeLocalSpaceRepo : LocalSpaceRepo {
    var enoughSpace = true
    override fun hasEnoughSpace(): Boolean = enoughSpace
}

class FakeRecordVideoRepo : RecordVideoRepo {
    /** Null simulates MediaStore refusing the pending entry. */
    var nextLocation: LocalRecordLocation? = null
    val initCalls = mutableListOf<Long>()
    var stopCalls = 0

    override suspend fun initVideo(timeCreated: Long): LocalRecordLocation? {
        initCalls += timeCreated
        return nextLocation
    }

    override suspend fun stopVideo() {
        stopCalls++
    }
}

class FakeLocalMediaRepo : LocalMediaRepo {
    var images: List<LocalImage> = emptyList()
    var videos: List<LocalVideo> = emptyList()
    var videoDuration = 5_000L

    override suspend fun getAllImages(): List<LocalImage> = images

    override suspend fun getAllVideos(): List<LocalVideo> = videos

    override suspend fun getVideoDuration(fileUri: Uri?): Long = videoDuration
}
