/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.suggestions

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.search.SuggestionsRepo
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * [SuggestionsRepo] as a Firebase range query on `basic-data/username`.
 *
 * Deliberate fix over `DefaultSuggestionsRepo`, which ordered by follower count while range-
 * bounding on the query text (a category error Firebase resolves to nothing) and then read
 * the `users` node as a plain list. This is the prefix query its commented-out code intended:
 * order by username, bound to `[query, query + U+F8FF]`, cap 12.
 */
class FirebaseSuggestionsRepo(
    private val realFire: FirebaseDatabase = Firebase.database,
) : SuggestionsRepo {

    override suspend fun fetchSuggestions(query: String): Result<List<User>, DataError.Network> =
        safeCall {
            realFire.getReference("users")
                .orderByChild("basic-data/username")
                .startAt(query)
                .endAt(query + PREFIX_UPPER_BOUND)
                .limitToFirst(SUGGESTION_CAP)
                .get()
                .await()
                .children
                .mapNotNull { it.child("basic-data").getValue<User>() }
        }

    companion object {
        /** The conventional Firebase prefix-query terminator: sorts after every real string. */
        private const val PREFIX_UPPER_BOUND = "\uf8ff"
        private const val SUGGESTION_CAP = 12
    }
}
