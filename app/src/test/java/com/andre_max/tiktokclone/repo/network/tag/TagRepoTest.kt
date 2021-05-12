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
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class TagRepoTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Mock
    lateinit var realFire: FirebaseDatabase

    private val firePath = FirePath()
    private lateinit var tagRepo: DefaultTagRepo

    @Before
    fun setUpRepo() {
        MockitoAnnotations.initMocks(this)
        tagRepo = DefaultTagRepo(realFire)
    }

    @Test
    fun saveTagsInVideo() = runBlockingTest {
        val initialCount = 1

        // GIVEN a tag that has a count of 1
        Mockito.`when`(
            realFire
                .getReference(firePath.getTagInfo(testTagName)).get().await().getValue<Tag>()
        ).thenReturn(Tag("happy", initialCount))

        // WHEN a tag is saved
        tagRepo.saveTagsInVideo(testTagsList, testVideoId)

        // THEN setValue is called with the new count as 2
        Mockito.verify(realFire).getReference(firePath.getTagInfo(testTagName)).setValue(2)
        Mockito.verify(realFire).getReference(firePath.getTagVideos(testTagName)).child(testVideoId).setValue(testVideoId)
    }
}