/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.search

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.user.User

/** Username-prefix search over user profiles. */
interface SuggestionsRepo {

    suspend fun fetchSuggestions(query: String): Result<List<User>, DataError.Network>
}
