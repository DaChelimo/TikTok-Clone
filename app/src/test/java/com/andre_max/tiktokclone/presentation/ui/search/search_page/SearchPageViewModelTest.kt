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
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.suggestions.DefaultSuggestionsRepo
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.andre_max.tiktokclone.test_utils.filterUserList
import com.andre_max.tiktokclone.test_utils.testQueryWithNoActualUser
import com.andre_max.tiktokclone.test_utils.testUserList
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun setup() = runBlockingTest {
        suggestionsRepo = spy(DefaultSuggestionsRepo(mock()))
        viewModel = SearchPageViewModel(suggestionsRepo)

        whenever(suggestionsRepo.dummy(anyString())).thenAnswer { i: InvocationOnMock ->
            i.arguments[0]
        }
        whenever(suggestionsRepo.fetchSuggestions(anyString())).thenAnswer { i: InvocationOnMock ->
            val query = i.arguments[0] as String
            testUserList.filter { it.username.startsWith(query)  }
        }
    }

    @Test
    fun whenDummy() {
        val output = suggestionsRepo.dummy("joy")
        assertThat(output).isEqualTo("joy")
    }

    @Test
    fun whenQueryName_withMultipleUsers_returnsSeveralUsers() {
        // GIVEN a general query
        val query = "an"

        // WHEN the liveQuery is set
        viewModel.liveQuery.value = query

        println("viewModel.liveQuery is ${viewModel.liveQuery.value}")
        // THEN the liveSuggestions has a value
        assertThat(viewModel.liveSuggestions.value).isEqualTo(filterUserList)
    }

    @Test
    fun whenQueryName_withNoUsers_returnsSeveralUsers() = runBlockingTest {
        // GIVEN a general query

        // WHEN the liveQuery is set
        viewModel.liveQuery.value = testQueryWithNoActualUser

        // THEN the liveSuggestions has a value
        assertThat(viewModel.liveSuggestions.value).isEmpty()
    }
}
