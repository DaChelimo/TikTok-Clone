package com.andre_max.tiktokclone.presentation.ui.suggestions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.suggestions.SuggestionsRepo
import com.andre_max.tiktokclone.utils.viewModel.BaseViewModel
import kotlinx.coroutines.*

class SuggestionsViewModel : BaseViewModel() {
    private val suggestionsRepo = SuggestionsRepo()

    private var job: Job = Job()

    val liveQuery = MutableLiveData("")

    private val _liveSuggestions = MutableLiveData<List<User>>()
    val liveSuggestions: LiveData<List<User>> = _liveSuggestions

    private val onQueryChanged = { query: String? ->
        fetchSuggestions()
    }

    init {
        liveQuery.observeForever(onQueryChanged)
    }

    private fun fetchSuggestions() {
        val query = liveQuery.value
        if (query.isNullOrBlank()) return

        job.cancel()
        job = viewModelScope.launch {
            ensureActive()
            delay(1250)
            _liveSuggestions.value = suggestionsRepo.fetchSuggestions(query).getData()
        }
    }


    override fun onCleared() {
        super.onCleared()
        job.cancel()
        liveQuery.removeObserver(onQueryChanged)
    }
}