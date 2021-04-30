package com.andre_max.tiktokclone.repo.network.tag

import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.andre_max.tiktokclone.utils.runAsync
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class TagRepo {
    private val realFire = Firebase.database
    private val firePath = FirePath()

    fun saveTagsInVideo(tags: List<String>, videoId: String) {
        tags.forEach {
            val tagName = it.replace("#", "", true)

            // Increment tag count
            realFire
                .getReference(firePath.getTagInfo(tagName))
                .runAsync {
                    // Get an existing tag or create a new one.
                    val tag = get().await().getValue<Tag>() ?: Tag(tagName, 0)
                    tag.count++

                    setValue(tag)
                }

            realFire
                .getReference(firePath.getTagVideos(tagName))
                .child(videoId)
                .setValue(videoId)
        }
    }

    suspend fun fetchPopularTags() = safeAccess {
        val tagList = realFire
            .getReference(firePath.getTagsPath())
            .orderByChild("count")
            .limitToFirst(12)
            .get().await().getValue<List<Tag>>()

        return@safeAccess tagList ?: listOf()
    }

    suspend fun fetchTagVideos(tagName: String) = safeAccess {
        realFire
            .getReference(firePath.getTagVideos(tagName))
            .limitToFirst(12)
            .get()
            .await()
            .getValue<List<String>>() ?: listOf()
    }
}