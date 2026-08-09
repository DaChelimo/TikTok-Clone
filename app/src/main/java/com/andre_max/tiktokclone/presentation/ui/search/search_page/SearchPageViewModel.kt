/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.search.search_page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.domain.core.fold
import com.andre_max.tiktokclone.domain.search.SuggestionsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Debounced username search. Same contract as the legacy screen: 1250ms after the last
 * keystroke, lowercased query, a blank query fetches nothing (and keeps the last results).
 */
@HiltViewModel
class SearchPageViewModel @Inject constructor(
    private val suggestionsRepo: SuggestionsRepo,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchPageUiState())
    val uiState: StateFlow<SearchPageUiState> = _uiState.asStateFlow()

    private val _events = Channel<SearchPageEvent>(Channel.BUFFERED)
    val events: Flow<SearchPageEvent> = _events.receiveAsFlow()

    private var searchJob: Job? = null

    fun onAction(action: SearchPageAction) {
        when (action) {
            is SearchPageAction.QueryChanged -> onQueryChanged(action.query)
            is SearchPageAction.InsertSuggestion -> onQueryChanged(action.username)
            is SearchPageAction.SuggestionClicked ->
                _events.trySend(SearchPageEvent.NavigateToProfile(action.uid))
        }
    }

    private fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }

        val normalized = query.lowercase(Locale.ENGLISH)
        if (normalized.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            val suggestions = suggestionsRepo.fetchSuggestions(normalized)
                .fold({ it }, { emptyList() })
            _uiState.update { it.copy(suggestions = suggestions.toImmutableList()) }
        }
    }

    companion object {
        /** The legacy screen's debounce, kept as-is. */
        const val DEBOUNCE_MILLIS = 1250L
    }
}
