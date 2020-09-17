package com.andre_max.tiktokclone.tag

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.firebaseDatabase
import com.andre_max.tiktokclone.getAllVideosPath
import com.andre_max.tiktokclone.getTagsPath
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.each_hashtag_image.view.*
import timber.log.Timber

class MainTag {
    val adapter = GroupAdapter<GroupieViewHolder>()
    val videoTagMap = HashMap<String, EachVideoPreviewGroup>()

    fun formatHashTagCount(count: Int): String {
        val stringCount = count.toString()
        Timber.d("count is $count")
        return when {
            count > 1_000_000_000 -> "${stringCount[0]}.${stringCount[1]}B"
            count > 100_000_000 -> "${stringCount.take(3)}.${stringCount[3]}M"
            count > 10_000_000 -> "${stringCount.take(2)}.${stringCount[2]}M"
            count > 1_000_000 -> "${stringCount[0]}.${stringCount[1]}M"
            count > 100_000 -> "${stringCount.take(3)}.${stringCount[3]}K"
            count > 10_000 -> "${stringCount.take(2)}.${stringCount[2]}K"
            count > 1_000 -> "${stringCount[0]}.${stringCount[1]}K"
            else -> stringCount
        }
    }

    fun getVideosWithTag(tagName: String) {
        val videoTagRef = firebaseDatabase.getReference("${getTagsPath()}/$tagName/tag-videos")
            .limitToFirst(30)

        videoTagRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val arrayOfVideoKeys = ArrayList<String>()
                snapshot.children.forEach {
                    val videoKey = it.getValue(String::class.java) ?: return@forEach
                    arrayOfVideoKeys.add(videoKey)
                }
                Timber.d("arrayOfVideoKeys.size is ${arrayOfVideoKeys.size}")

                arrayOfVideoKeys.forEach {
                    getVideoFromKey(it)
                    Timber.d("After loop")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })
    }

    private fun getVideoFromKey(key: String): Bitmap? {
        val videoRef = firebaseDatabase.getReference("${getAllVideosPath()}/$key").child("url")
        var bitmap: Bitmap? = null

        videoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.getValue(String::class.java) ?: return

                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(url, mapOf())
                bitmap = mediaMetadataRetriever.frameAtTime
                mediaMetadataRetriever.release()

                val eachVideoPreviewGroup = EachVideoPreviewGroup(bitmap, key)
                videoTagMap[key] = eachVideoPreviewGroup
                adapter.add(eachVideoPreviewGroup)

                Timber.d("Finished onDataChange of getVideoFromKey with returnedBitmap as $bitmap")
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })

        Timber.d("End of getVideoKey() with returnedBitmap as $bitmap")
        return bitmap
    }

    class EachVideoPreviewGroup(private val bitmap: Bitmap?, private val videoKey: String) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val layout = viewHolder.itemView

            Glide.with(layout.context)
                .load(bitmap)
                .into(layout.each_hashtag_image)

            layout.setOnClickListener {
                Timber.d("Layout clickListener called")
            }
        }

        override fun getLayout(): Int = R.layout.each_hashtag_image
    }

}