/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.search.search_page

import app.cash.turbine.test
import com.andre_max.tiktokclone.domain.search.SuggestionsRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.test_utils.makeUser
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Test

/** The debounced search contract: 1250ms after the last keystroke, lowercased, blank = no-op. */
@ExperimentalCoroutinesApi
class SearchPageViewModelTest {

    private class FakeSuggestionsRepo : SuggestionsRepo {
        var users: List<User> = emptyList()
        val queries = mutableListOf<String>()
        var error: DataError.Network? = null

        override suspend fun fetchSuggestions(query: String): Result<List<User>, DataError.Network> {
            queries += query
            error?.let { return Result.Error(it) }
            return Result.Success(users.filter { it.username.startsWith(query, ignoreCase = true) })
        }
    }

    private val suggestionsRepo = FakeSuggestionsRepo()

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun buildViewModel() = SearchPageViewModel(suggestionsRepo)

    @Test
    fun `searches only after the debounce and lowercases the query`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        suggestionsRepo.users = listOf(makeUser("u1", username = "andrew"))
        val viewModel = buildViewModel()

        viewModel.onAction(SearchPageAction.QueryChanged("An"))
        advanceTimeBy(SearchPageViewModel.DEBOUNCE_MILLIS - 1)
        assertThat(suggestionsRepo.queries).isEmpty()

        advanceUntilIdle()
        assertThat(suggestionsRepo.queries).containsExactly("an")
        assertThat(viewModel.uiState.value.suggestions.single().username).isEqualTo("andrew")
    }

    @Test
    fun `rapid typing collapses into one search for the final query`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel()

        viewModel.onAction(SearchPageAction.QueryChanged("a"))
        advanceTimeBy(300)
        viewModel.onAction(SearchPageAction.QueryChanged("an"))
        advanceTimeBy(300)
        viewModel.onAction(SearchPageAction.QueryChanged("and"))
        advanceUntilIdle()

        assertThat(suggestionsRepo.queries).containsExactly("and")
    }

    @Test
    fun `a blank query fetches nothing and keeps the last results`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        suggestionsRepo.users = listOf(makeUser("u1", username = "andrew"))
        val viewModel = buildViewModel()

        viewModel.onAction(SearchPageAction.QueryChanged("an"))
        advanceUntilIdle()
        viewModel.onAction(SearchPageAction.QueryChanged("   "))
        advanceUntilIdle()

        assertThat(suggestionsRepo.queries).containsExactly("an")
        assertThat(viewModel.uiState.value.suggestions).hasSize(1)
    }

    @Test
    fun `inserting a suggestion re-runs the search for that name`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        suggestionsRepo.users = listOf(makeUser("u1", username = "andrew"))
        val viewModel = buildViewModel()

        viewModel.onAction(SearchPageAction.InsertSuggestion("andrew"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEqualTo("andrew")
        assertThat(suggestionsRepo.queries).containsExactly("andrew")
    }

    @Test
    fun `tapping a suggestion emits profile navigation`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = buildViewModel()

        viewModel.events.test {
            viewModel.onAction(SearchPageAction.SuggestionClicked("u1"))
            assertThat(awaitItem()).isEqualTo(SearchPageEvent.NavigateToProfile("u1"))
        }
    }
}
