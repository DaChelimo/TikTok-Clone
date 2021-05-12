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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.suggestions.DefaultSuggestionsRepo
import com.andre_max.tiktokclone.repo.network.suggestions.SuggestionsRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.*
import java.util.*

class SearchPageViewModel(
    private val suggestionsRepo: SuggestionsRepo = DefaultSuggestionsRepo()
) : BaseViewModel() {
    private var job: Job = Job()

    val liveQuery = MutableLiveData("")

    private val _liveSuggestions = MutableLiveData<List<User>>()
    val liveSuggestions: LiveData<List<User>> = _liveSuggestions

    private val onQueryChanged: (String?) -> Unit = {
        fetchSuggestions()
    }

    init {
        liveQuery.observeForever(onQueryChanged)
    }

    private fun fetchSuggestions() {
        val query = liveQuery.value?.toLowerCase(Locale.ENGLISH)
        if (query.isNullOrBlank()) return

        job.cancel()
        job = viewModelScope.launch {
            println("Before delay: Doing job")
            delay(1250)
            println("After delay: Doing job")
            _liveSuggestions.value = suggestionsRepo.fetchSuggestions(query).tryData() ?: listOf()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        liveQuery.removeObserver(onQueryChanged)
    }
}