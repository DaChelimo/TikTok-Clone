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
    private lateinit var tagRepo: TagRepo

    @Before
    fun setUpRepo() {
        MockitoAnnotations.initMocks(this)
        tagRepo = TagRepo(realFire)
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