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

package com.andre_max.tiktokclone.presentation.ui.search.search_page

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.andre_max.tiktokclone.models.TheResult
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.suggestions.DefaultSuggestionsRepo
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.andre_max.tiktokclone.test_utils.filterUserList
import com.andre_max.tiktokclone.test_utils.testQueryWithNoActualUser
import com.andre_max.tiktokclone.test_utils.testUserList
import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer


@ExperimentalCoroutinesApi
class SearchPageViewModelTest {
    private lateinit var viewModel: SearchPageViewModel

    private lateinit var suggestionsRepo: DefaultSuggestionsRepo

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Before
    fun setup() = coroutineTestRule.testCoroutineDispatcher.runBlockingTest {
        suggestionsRepo = spy(DefaultSuggestionsRepo(mock()))
        viewModel = SearchPageViewModel(suggestionsRepo)

        // doAnswer().whenever(spy) rather than whenever(spy.method()): the latter *calls* the
        // real method while stubbing, which on a spy over a Firebase-backed repo is how these
        // stubs were silently failing to register at all.
        doAnswer { i: InvocationOnMock -> i.arguments[0] }
            .whenever(suggestionsRepo).dummy(anyString())
        // fetchSuggestions returns TheResult<List<User>>, not a bare list. The stub used to
        // answer with the list itself, so the ViewModel's .tryData() call blew up on a type
        // that has no such method.
        // ignoreCase because SearchPageViewModel lowercases the query before it gets here while
        // the fixtures are capitalised. Whether the real Firebase range query is case-insensitive
        // too is a separate, open question — see the search-casing follow-up.
        doAnswer { i: InvocationOnMock ->
            val query = i.arguments[0] as String
            TheResult.Success(
                testUserList.filter { it.username.startsWith(query, ignoreCase = true) },
            )
        }.whenever(suggestionsRepo).fetchSuggestions(anyString())
    }

    @Test
    fun whenDummy() {
        val output = suggestionsRepo.dummy("joy")
        assertThat(output).isEqualTo("joy")
    }

    @Test
    fun whenQueryName_withMultipleUsers_returnsSeveralUsers() =
        coroutineTestRule.testCoroutineDispatcher.runBlockingTest {
        // GIVEN a general query
        val query = "an"

        // WHEN the liveQuery is set
        viewModel.liveQuery.value = query

        // The ViewModel debounces by 1250ms before it queries, so the virtual clock has to be
        // moved past that or the assertion races the coroutine that has not started yet.
        advanceUntilIdle()

        // THEN the liveSuggestions has a value
        assertThat(viewModel.liveSuggestions.value).isEqualTo(filterUserList)
    }

    @Test
    fun whenQueryName_withNoUsers_returnsSeveralUsers() =
        coroutineTestRule.testCoroutineDispatcher.runBlockingTest {
        // GIVEN a general query

        // WHEN the liveQuery is set
        viewModel.liveQuery.value = testQueryWithNoActualUser
        advanceUntilIdle()

        // THEN the liveSuggestions has a value
        assertThat(viewModel.liveSuggestions.value).isEmpty()
    }
}
