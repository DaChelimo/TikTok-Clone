package com.andre_max.tiktokclone.repo.local.utils

import android.os.Build
import android.os.Environment
import android.os.StatFs
import timber.log.Timber
import java.io.File


class LocalSpaceRepo {
    fun hasEnoughSpace(): Boolean {
        val rootDir: File = Environment.getRootDirectory()
        val stat = StatFs(rootDir.path)
        val sizeInBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            blockSize * availableBlocks
        } else {
            val blockSize = stat.blockSize.toLong()
            val availableBlocks = stat.availableBlocks.toLong()
            blockSize * availableBlocks
        }
        val sizeInMB = sizeInBytes / 1024
        Timber.d("Size in MB is $sizeInMB")
        return sizeInMB > (30 * 1024)
    }
}