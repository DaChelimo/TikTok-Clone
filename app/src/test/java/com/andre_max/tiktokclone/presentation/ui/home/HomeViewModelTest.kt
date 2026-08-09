/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

import com.andre_max.tiktokclone.models.TheResult
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    /**
     * `init` launches into `viewModelScope`, i.e. `Dispatchers.Main`. Without this rule the
     * constructor throws before any assertion runs — which is what the previous version of this
     * suite was actually failing on, not on the repo interaction it meant to check.
     */
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val videosRepo = mock<VideosRepo>()

    @Test
    fun whenViewModel_isInitialized_fetchVideosIsCalled() = runBlockingTest {
        whenever(videosRepo.fetchRandomVideos()).thenReturn(TheResult.Success(emptyList()))

        HomeViewModel(mock(), mock(), videosRepo)

        // verify() takes the mock, not the result of calling it. The previous
        // `verify(videosRepo.fetchRandomVideos())` invoked the method for real and handed its
        // return value to Mockito, which is why this failed with IllegalStateException rather
        // than with a useful message.
        verify(videosRepo).fetchRandomVideos()
    }
}
