package com.andre_max.tiktokclone.repo.local.record

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.andre_max.tiktokclone.models.local.LocalRecordLocation

class RecordVideoRepo {

    fun getLocalRecordLocation(context: Context, timeCreated: Long): LocalRecordLocation? {
        // Add a media item that other apps shouldn't see until the item is
        // fully written to the media store.
        val resolver = context.contentResolver

        // Find all video files on the primary external storage device.
        val videoCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        val videoDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$timeCreated.mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val videoUri = resolver.insert(videoCollection, videoDetails) ?: return null
        val fileDescriptor = resolver
            .openFileDescriptor(videoUri, "rw")?.fileDescriptor ?: return null

        return LocalRecordLocation(videoUri, fileDescriptor)
    }

    fun stopVideo(context: Context, fileUri: Uri?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(fileUri ?: return, contentValues, null, null)
        }
    }

}