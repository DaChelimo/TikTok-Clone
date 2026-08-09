/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.large_video

import androidx.lifecycle.SavedStateHandle
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.presentation.ui.components.video.CommentSheetAction
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoPageAction
import com.andre_max.tiktokclone.test_utils.FakeCommentsRepo
import com.andre_max.tiktokclone.test_utils.FakeUserRepo
import com.andre_max.tiktokclone.test_utils.FakeVideosRepo
import com.andre_max.tiktokclone.test_utils.makeComment
import com.andre_max.tiktokclone.test_utils.makeUser
import com.andre_max.tiktokclone.test_utils.makeVideo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

/**
 * The single-video detail screen. Same interaction contract as a feed page, sourced from the
 * nav argument instead of a batch fetch.
 */
@ExperimentalCoroutinesApi
class LargeVideoViewModelTest {

    private val videosRepo = FakeVideosRepo()
    private val userRepo = FakeUserRepo()
    private val commentsRepo = FakeCommentsRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel(handle: SavedStateHandle) = LargeVideoViewModel(
        videosRepo = videosRepo,
        userRepo = userRepo,
        commentsRepo = commentsRepo,
        savedStateHandle = handle,
    )

    private fun handleWith(video: com.andre_max.tiktokclone.models.video.RemoteVideo) =
        SavedStateHandle(mapOf("remoteVideo" to video))

    @Test
    fun `init builds the page from the nav argument and enriches it`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val video = makeVideo("a", authorUid = "author-1", likes = 3)
        videosRepo.likedVideoIds += "a"
        userRepo.profiles["author-1"] = makeUser("author-1", username = "andrew")

        val viewModel = buildViewModel(handleWith(video))
        advanceUntilIdle()

        val item = viewModel.uiState.value.item
        assertThat(item).isNotNull()
        assertThat(item!!.video.videoId).isEqualTo("a")
        assertThat(item.likeCount).isEqualTo(3)
        assertThat(item.isLiked).isTrue()
        assertThat(item.authorUsername).isEqualTo("andrew")
        assertThat(item.showFollowButton).isTrue()
    }

    @Test
    fun `a missing nav argument leaves the state empty instead of crashing`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))

        val viewModel = buildViewModel(SavedStateHandle())
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.item).isNull()
    }

    @Test
    fun `like toggles optimistically and reverts when the repo fails`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val video = makeVideo("a", likes = 5)
        videosRepo.likeError = DataError.Network.NO_INTERNET

        val viewModel = buildViewModel(handleWith(video))
        advanceUntilIdle()

        viewModel.onPageAction(VideoPageAction.LikeToggled("a"))
        advanceUntilIdle()

        val item = viewModel.uiState.value.item!!
        assertThat(item.isLiked).isFalse()
        assertThat(item.likeCount).isEqualTo(5)
        assertThat(videosRepo.likeCalls).hasSize(1)
    }

    @Test
    fun `follow reaches the repo and hides the button`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val video = makeVideo("a", authorUid = "author-1")

        val viewModel = buildViewModel(handleWith(video))
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.item!!.showFollowButton).isTrue()

        viewModel.onPageAction(VideoPageAction.FollowClicked("author-1"))
        advanceUntilIdle()

        assertThat(userRepo.followCalls).containsExactly("author-1")
        assertThat(viewModel.uiState.value.item!!.showFollowButton).isFalse()
    }

    @Test
    fun `opening comments shows the live thread`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val video = makeVideo("a", totalCommentsSize = 1)
        userRepo.profiles["commenter-1"] = makeUser("commenter-1", username = "maria")
        commentsRepo.commentsFlow.value =
            listOf(makeComment("c1", authorUid = "commenter-1", text = "nice"))
        commentsRepo.countFlow.value = 1

        val viewModel = buildViewModel(handleWith(video))
        advanceUntilIdle()

        viewModel.onPageAction(VideoPageAction.OpenComments("a"))
        advanceUntilIdle()

        val sheet = viewModel.uiState.value.commentSheet
        assertThat(commentsRepo.observedVideoIds).containsExactly("a")
        assertThat(sheet).isNotNull()
        assertThat(sheet!!.comments.single().comment.commentText).isEqualTo("nice")
        assertThat(sheet.comments.single().authorUsername).isEqualTo("maria")
        assertThat(sheet.commentCount).isEqualTo(1)
    }

    @Test
    fun `sending a comment reaches the repo and clears the draft`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val video = makeVideo("a")

        val viewModel = buildViewModel(handleWith(video))
        advanceUntilIdle()
        viewModel.onPageAction(VideoPageAction.OpenComments("a"))
        advanceUntilIdle()

        viewModel.onSheetAction(CommentSheetAction.DraftChanged("first!"))
        viewModel.onSheetAction(CommentSheetAction.Send)
        advanceUntilIdle()

        assertThat(commentsRepo.sentComments)
            .containsExactly(FakeCommentsRepo.SentComment("a", "first!"))
        assertThat(viewModel.uiState.value.commentSheet?.draft).isEmpty()
    }
}
