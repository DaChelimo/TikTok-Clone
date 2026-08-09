/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.local.space

import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.andre_max.tiktokclone.repo.local.utils.toMB
import java.io.File

class DefaultLocalSpaceRepo : LocalSpaceRepo {

    @Suppress("DEPRECATION")
    override fun hasEnoughSpace(): Boolean {
        val rootDir: File = Environment.getExternalStorageDirectory()
        val stat = StatFs(rootDir.path)

        val sizeInBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            stat.blockSizeLong * stat.availableBlocksLong
        } else {
            stat.blockSize.toLong() * stat.availableBlocks.toLong()
        }

        return sizeInBytes.toMB() > MINIMUM_DEVICE_STORAGE_IN_MB
    }

    companion object {
        const val MINIMUM_DEVICE_STORAGE_IN_MB = 100
    }
}
