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

package com.andre_max.tiktokclone.repo.network.tag

import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.andre_max.tiktokclone.utils.runAsync
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class DefaultTagRepo(private val realFire: FirebaseDatabase = Firebase.database) : TagRepo {
    private val firePath = FirePath()

    override suspend fun fetchPopularTags() = safeAccess {
        val tagList = realFire
            .getReference(firePath.getTagsPath())
            .orderByChild("count")
            .limitToFirst(12)
            .get().await().getValue<List<Tag>>()

        return@safeAccess tagList ?: listOf()
    }

    override suspend fun fetchTagVideos(tagName: String) = safeAccess {
        realFire
            .getReference(firePath.getTagVideos(tagName))
            .limitToFirst(12)
            .get()
            .await()
            .getValue<List<String>>() ?: listOf()
    }

    override fun saveTagsInVideo(tags: Collection<String>, videoId: String) {
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
}