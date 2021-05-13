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

package com.andre_max.tiktokclone.repo.local.media

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andre_max.tiktokclone.models.local.LocalImage
import com.andre_max.tiktokclone.models.local.LocalVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class DefaultLocalMediaRepo : LocalMediaRepo {
    private val _listOfLocalImage = MutableLiveData<List<LocalImage>>()
    val listOfLocalImage: LiveData<List<LocalImage>> = _listOfLocalImage

    private val _listOfLocalVideo = MutableLiveData<List<LocalVideo>>()
    val listOfLocalVideo: LiveData<List<LocalVideo>> = _listOfLocalVideo


    // TODO: Remove this
    @SuppressLint("NewApi")
    override suspend fun getAllImages(context: Context) =
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val mediaStoreUri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                else
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val cursor = contentResolver?.query(mediaStoreUri, null, null, null, null)
            if (cursor == null) {
                Timber.d("Cursor is null")
                return@withContext
            }
            val imageList = ArrayList<LocalImage>()

            while (cursor.moveToNext()) {
                try {
                    val contentUri =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                            .toUri()
                    Timber.d("contentUri is $contentUri")
//                val filePath = getRealPathFromURI(context, contentUri)
                    val filePath = getFilePathFromCursor(cursor)
                    val dateCreated =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                    val userImage = LocalImage(filePath, dateCreated)
                    Timber.d("filePath is $filePath")
                    imageList.add(userImage)
                } catch (e: Exception) {
                    Timber.e(e, "Caught exception!!!")
                }
            }

            withContext(Dispatchers.Main) {
                _listOfLocalImage.value = imageList
            }

            cursor.close()
        }


    // TODO: Remove this
    @SuppressLint("NewApi")
    override suspend fun getAllVideos(context: Context) =
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val mediaStoreUri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                else
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI

            val cursor = contentResolver?.query(mediaStoreUri, null, null, null, null)
            if (cursor == null) {
                Timber.d("Cursor is null")
                return@withContext
            }

            val videoList = ArrayList<LocalVideo>()

            while (cursor.moveToNext()) {
                try {
                    val fileUri = getFilePathFromCursor(cursor)
                    val videoDuration = getVideoDuration(context, fileUri?.toUri())
                    val dateCreated =
                        cursor.getColumnName(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))

                    val localVideo = LocalVideo(fileUri, videoDuration, dateCreated)
                    Timber.d("url is $fileUri")
                    videoList.add(localVideo)
                } catch (e: Exception) {
                    Timber.e(e, "Caught exception!!!")
                }
            }

            withContext(Dispatchers.Main) {
                _listOfLocalVideo.value = videoList
            }
            cursor.close()
        }

    @Suppress("DEPRECATION")
    override fun getFilePathFromCursor(cursor: Cursor): String? =
        cursor.getString(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
            } else {
                cursor.getColumnIndex(MediaStore.Video.Media.DATA)
            }
        )

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun getVideoDuration(context: Context, fileUri: Uri?) = withContext(Dispatchers.IO) {
        if (fileUri == null) return@withContext 0

       val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(context, fileUri)

        val duration = mediaPlayer.duration
        mediaPlayer.release()

        return@withContext duration.toLong()
    }
}