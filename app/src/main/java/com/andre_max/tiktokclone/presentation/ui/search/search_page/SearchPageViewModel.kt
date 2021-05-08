package com.andre_max.tiktokclone.presentation.ui.search.search_page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.suggestions.SuggestionsRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.*
import java.util.*

class SearchPageViewModel : BaseViewModel() {
    private val suggestionsRepo = SuggestionsRepo()

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
            delay(1250)
            _liveSuggestions.value = suggestionsRepo.fetchSuggestions(query).tryData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        liveQuery.removeObserver(onQueryChanged)
    }
}