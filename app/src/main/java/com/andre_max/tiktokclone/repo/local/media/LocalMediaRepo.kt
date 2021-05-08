package com.andre_max.tiktokclone.repo.local.media

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andre_max.tiktokclone.models.local.LocalImage
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.repo.local.utils.getRealPathFromURI
import com.andre_max.tiktokclone.utils.letAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class LocalMediaRepo {
    private val _listOfLocalImage = MutableLiveData<List<LocalImage>>()
    val listOfLocalImage: LiveData<List<LocalImage>> = _listOfLocalImage

    private val _listOfLocalVideo = MutableLiveData<List<LocalVideo>>()
    val listOfLocalVideo: LiveData<List<LocalVideo>> = _listOfLocalVideo


    // TODO: Remove this
    @SuppressLint("NewApi")
    suspend fun getAllImages(context: Context) = withContext(Dispatchers.IO) {
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
        val imageList = ArrayList<LocalImage>()

        while (cursor.moveToNext()) {
            try {
                val contentUri =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)).toUri()
                Timber.d("contentUri is $contentUri")
                val filePath = getRealPathFromURI(context, contentUri)
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
    suspend fun getAllVideos(context: Context) = withContext(Dispatchers.IO) {
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
                val contentUri =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)).toUri()
                Timber.d("contentUri is $contentUri")
                val filePath = getRealPathFromURI(context, contentUri)
                val dateCreated =
                    cursor.getColumnName(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))

                @Suppress("BlockingMethodInNonBlockingContext")
                val duration = MediaPlayer().letAsync {
                    val uri = filePath?.toUri() ?: return@letAsync 0
                    Timber.d("uri is $uri")
                    setDataSource(context, uri)
                    duration
                }

                val localVideo = LocalVideo(filePath, duration.toLong(), dateCreated)
                Timber.d("url is $filePath")
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
}