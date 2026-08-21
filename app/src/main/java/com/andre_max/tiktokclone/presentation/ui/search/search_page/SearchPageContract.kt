/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.search.search_page

import com.andre_max.tiktokclone.models.user.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SearchPageUiState(
    val query: String = "",
    val suggestions: ImmutableList<User> = persistentListOf(),
)

sealed interface SearchPageAction {
    data class QueryChanged(val query: String) : SearchPageAction

    /** The arrow icon: copies the suggestion into the box (which re-triggers the search). */
    data class InsertSuggestion(val username: String) : SearchPageAction

    /** The row itself: opens the suggested user's profile. */
    data class SuggestionClicked(val uid: String) : SearchPageAction
}

sealed interface SearchPageEvent {
    data class NavigateToProfile(val uid: String) : SearchPageEvent
}

/** Test/preview tags for the nodes this screen must expose. */
object SearchPageTags {
    const val INPUT = "search_page_input"
    const val SUGGESTION_LIST = "search_page_suggestions"
}
