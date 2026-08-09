/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.local.media

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.andre_max.tiktokclone.models.local.LocalImage
import com.andre_max.tiktokclone.models.local.LocalVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * The legacy cursor walks, returning plain lists instead of exposing LiveData. Rows that fail
 * to read are skipped (media stores routinely hold entries whose files are gone).
 */
class DefaultLocalMediaRepo(private val context: Context) : LocalMediaRepo {

    @SuppressLint("Range")
    override suspend fun getAllImages(): List<LocalImage> = withContext(Dispatchers.IO) {
        val mediaStoreUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val cursor = context.contentResolver?.query(mediaStoreUri, null, null, null, null)
            ?: return@withContext emptyList()

        val imageList = ArrayList<LocalImage>()
        cursor.use {
            while (it.moveToNext()) {
                try {
                    val filePath = getFilePathFromCursor(it)
                    val dateCreated =
                        it.getString(it.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                    imageList.add(LocalImage(filePath, dateCreated))
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
        imageList
    }

    @SuppressLint("Range")
    override suspend fun getAllVideos(): List<LocalVideo> = withContext(Dispatchers.IO) {
        val mediaStoreUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            else
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val cursor = context.contentResolver?.query(mediaStoreUri, null, null, null, null)
            ?: return@withContext emptyList()

        val videoList = ArrayList<LocalVideo>()
        cursor.use {
            while (it.moveToNext()) {
                try {
                    val filePath = getFilePathFromCursor(it)
                    val videoDuration = getVideoDuration(filePath?.toUri())
                    val dateCreated =
                        it.getString(it.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))
                    videoList.add(LocalVideo(filePath, videoDuration, dateCreated))
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
        videoList
    }

    override suspend fun getVideoDuration(fileUri: Uri?): Long = withContext(Dispatchers.IO) {
        if (fileUri == null) return@withContext 0

        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(context, fileUri)
            mediaPlayer.duration.toLong()
        } finally {
            mediaPlayer.release()
        }
    }

    @SuppressLint("Range")
    @Suppress("DEPRECATION")
    private fun getFilePathFromCursor(cursor: Cursor): String? =
        cursor.getString(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
            } else {
                cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            },
        )
}
