package com.andre_max.tiktokclone.repo.local.media

import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andre_max.tiktokclone.models.local.LocalImage
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.utils.letAsync
import com.andre_max.tiktokclone.utils.runAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class LocalMediaRepo {
    private val _listOfLocalImage = MutableLiveData<List<LocalImage>>()
    val listOfLocalImage: LiveData<List<LocalImage>> = _listOfLocalImage

    private val _listOfLocalVideo = MutableLiveData<List<LocalVideo>>()
    val listOfLocalVideo: LiveData<List<LocalVideo>> = _listOfLocalVideo


    fun getAllImages(context: Context) {
        val contentResolver = context.contentResolver
        val mediaStoreUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        // I'm not sure if the cursor does it's query asynchrounously  
        runAsync {
            val cursor = contentResolver?.query(mediaStoreUri, null, null, null, null)
            if (cursor == null) {
                Timber.d("Cursor is null")
                return@runAsync
            }
            val imageList = ArrayList<LocalImage>()

            while (cursor.moveToNext()) {
                val url = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                val dateCreated =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                val userImage = LocalImage(url, dateCreated)
                Timber.d("url is $url")
                imageList.add(userImage)
            }

            withContext(Dispatchers.Main) {
                _listOfLocalImage.value = imageList
            }

            cursor.close()
        }
    }


    fun getAllVideos(context: Context) {
        val contentResolver = context.contentResolver
        val mediaStoreUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        runAsync {
            val cursor = contentResolver?.query(mediaStoreUri, null, null, null, null)
            if (cursor == null) {
                Timber.d("Cursor is null")
                return@runAsync
            }

            val videoList = ArrayList<LocalVideo>()
            while (cursor.moveToNext()) {
                val url = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                val dateCreated =
                    cursor.getColumnName(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))

                val duration = MediaPlayer().letAsync {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    setDataSource(context, url.toUri())
                    duration
                }

                val localVideo = LocalVideo(url, duration.toLong(), dateCreated)
                Timber.d("url is $url")
                videoList.add(localVideo)
            }
            withContext(Dispatchers.Main) {
                _listOfLocalVideo.value = videoList
            }
            cursor.close()
        }
    }

}