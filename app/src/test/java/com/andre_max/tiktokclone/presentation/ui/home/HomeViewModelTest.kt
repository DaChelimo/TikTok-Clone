/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.videos.usecase.GetFeedBatchUseCase
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
 * Pins the migrated feed behaviour: batch fetch + dedupe, near-end pagination (5-from-end
 * threshold, same as the legacy scroll listener), optimistic like with revert, follow
 * visibility, and the comment sheet's live thread.
 */
@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val videosRepo = FakeVideosRepo()
    private val userRepo = FakeUserRepo()
    private val commentsRepo = FakeCommentsRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel() = HomeViewModel(
        getFeedBatch = GetFeedBatchUseCase(videosRepo),
        videosRepo = videosRepo,
        userRepo = userRepo,
        commentsRepo = commentsRepo,
    )

    @Test
    fun `init fetches the first batch and enriches it with author and like state`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a", authorUid = "author-1", likes = 7))
        videosRepo.likedVideoIds += "a"
        userRepo.profiles["author-1"] = makeUser("author-1", username = "andrew")

        val viewModel = buildViewModel()
        advanceUntilIdle()

        val item = viewModel.uiState.value.feed.single()
        assertThat(item.video.videoId).isEqualTo("a")
        assertThat(item.likeCount).isEqualTo(7)
        assertThat(item.isLiked).isTrue()
        assertThat(item.authorUsername).isEqualTo("andrew")
    }

    @Test
    fun `a failed first fetch leaves the feed empty without crashing`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.fetchError = DataError.Network.NO_INTERNET

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.feed).isEmpty()
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `settling near the end fetches again and drops already-shown videos`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val firstBatch = (1..6).map { makeVideo("v$it") }
        videosRepo.randomBatch = firstBatch

        val viewModel = buildViewModel()
        advanceUntilIdle()

        // The backing query window has shifted by one — five of six are repeats.
        videosRepo.randomBatch = firstBatch.drop(1) + makeVideo("v7")
        viewModel.onAction(HomeAction.PageSettled(index = 2))
        advanceUntilIdle()

        val ids = viewModel.uiState.value.feed.map { it.video.videoId }
        assertThat(ids).containsExactly("v1", "v2", "v3", "v4", "v5", "v6", "v7").inOrder()
    }

    @Test
    fun `settling far from the end does not fetch`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = (1..12).map { makeVideo("v$it") }

        val viewModel = buildViewModel()
        advanceUntilIdle()
        val fetchesAfterInit = videosRepo.fetchCount

        viewModel.onAction(HomeAction.PageSettled(index = 2))
        advanceUntilIdle()

        assertThat(videosRepo.fetchCount).isEqualTo(fetchesAfterInit)
    }

    @Test
    fun `like toggles optimistically and reaches the repo`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a", authorUid = "author-1", likes = 7))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(HomeAction.LikeToggled("a"))
        advanceUntilIdle()

        val item = viewModel.uiState.value.feed.single()
        assertThat(item.isLiked).isTrue()
        assertThat(item.likeCount).isEqualTo(8)
        assertThat(videosRepo.likeCalls)
            .containsExactly(FakeVideosRepo.LikeCall("a", "author-1", shouldLike = true))
    }

    @Test
    fun `a failed like reverts the optimistic toggle`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a", likes = 7))
        videosRepo.likeError = DataError.Network.NO_INTERNET

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(HomeAction.LikeToggled("a"))
        advanceUntilIdle()

        val item = viewModel.uiState.value.feed.single()
        assertThat(item.isLiked).isFalse()
        assertThat(item.likeCount).isEqualTo(7)
    }

    @Test
    fun `like is ignored when signed out`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a"))
        userRepo.signedIn = false

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(HomeAction.LikeToggled("a"))
        advanceUntilIdle()

        assertThat(videosRepo.likeCalls).isEmpty()
        assertThat(viewModel.uiState.value.feed.single().isLiked).isFalse()
    }

    @Test
    fun `follow reaches the repo and hides the button on all of that author's videos`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(
            makeVideo("a", authorUid = "author-1"),
            makeVideo("b", authorUid = "author-1"),
        )

        val viewModel = buildViewModel()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.feed.map { it.showFollowButton })
            .containsExactly(true, true)

        viewModel.onAction(HomeAction.FollowClicked("author-1"))
        advanceUntilIdle()

        assertThat(userRepo.followCalls).containsExactly("author-1")
        assertThat(viewModel.uiState.value.feed.map { it.showFollowButton })
            .containsExactly(false, false)
    }

    @Test
    fun `own videos never show the follow button`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a", authorUid = "me-uid"))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.feed.single().showFollowButton).isFalse()
    }

    @Test
    fun `opening comments shows the live thread with author and like state`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a", totalCommentsSize = 1))
        userRepo.profiles["commenter-1"] = makeUser("commenter-1", username = "maria")
        commentsRepo.commentsFlow.value =
            listOf(makeComment("c1", authorUid = "commenter-1", text = "nice"))
        commentsRepo.likedCommentIds += "c1"
        commentsRepo.countFlow.value = 1

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(HomeAction.OpenComments("a"))
        advanceUntilIdle()

        val sheet = viewModel.uiState.value.commentSheet
        assertThat(commentsRepo.observedVideoIds).containsExactly("a")
        assertThat(sheet).isNotNull()
        val comment = sheet!!.comments.single()
        assertThat(comment.comment.commentText).isEqualTo("nice")
        assertThat(comment.authorUsername).isEqualTo("maria")
        assertThat(comment.isLiked).isTrue()
        assertThat(sheet.commentCount).isEqualTo(1)
    }

    @Test
    fun `closing comments clears the sheet`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a"))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(HomeAction.OpenComments("a"))
        advanceUntilIdle()
        viewModel.onAction(HomeAction.CloseComments)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.commentSheet).isNull()
    }

    @Test
    fun `a blank comment is rejected with a message and never sent`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a"))

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onAction(HomeAction.OpenComments("a"))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(HomeAction.CommentDraftChanged("   "))
            viewModel.onAction(HomeAction.SendCommentClicked)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HomeEvent.ShowMessage::class.java)
        }
        assertThat(commentsRepo.sentComments).isEmpty()
    }

    @Test
    fun `sending a comment reaches the repo and clears the draft`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        videosRepo.randomBatch = listOf(makeVideo("a"))

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onAction(HomeAction.OpenComments("a"))
        advanceUntilIdle()

        viewModel.onAction(HomeAction.CommentDraftChanged("first!"))
        viewModel.onAction(HomeAction.SendCommentClicked)
        advanceUntilIdle()

        assertThat(commentsRepo.sentComments)
            .containsExactly(FakeCommentsRepo.SentComment("a", "first!"))
        assertThat(viewModel.uiState.value.commentSheet?.draft).isEmpty()
    }
}
