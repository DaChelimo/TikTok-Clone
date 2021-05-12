/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.repo.local.record


import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.andre_max.tiktokclone.models.local.LocalRecordLocation
import com.andre_max.tiktokclone.repo.local.utils.getRealPathFromURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultRecordVideoRepo : RecordVideoRepo {
    private var localRecordLocation: LocalRecordLocation? = null

    override suspend fun initVideo(context: Context, timeCreated: Long): LocalRecordLocation? =
        withContext(Dispatchers.IO) {
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
                put(MediaStore.Video.Media.DISPLAY_NAME, "$timeCreated.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, timeCreated)
                put(MediaStore.Video.Media.DATE_MODIFIED, timeCreated)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
            }

            val contentUri =
                resolver.insert(videoCollection, videoDetails) ?: return@withContext null
            val filePath =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getRealPathFromURI(context, contentUri) ?: ""
                } else contentUri.path ?: ""

            @Suppress("BlockingMethodInNonBlockingContext")
            val fileDescriptor = resolver
                .openFileDescriptor(contentUri, "rw")?.fileDescriptor ?: return@withContext null

            localRecordLocation = LocalRecordLocation(contentUri, filePath, fileDescriptor)
            return@withContext localRecordLocation
        }

    override suspend fun stopVideo(context: Context) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(
                localRecordLocation?.contentUri ?: return@withContext,
                contentValues,
                null,
                null
            )
        }
    }
}
