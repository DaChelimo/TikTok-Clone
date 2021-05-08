package com.andre_max.tiktokclone.repo.network.suggestions

import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class SuggestionsRepo {
    private val realFire = Firebase.database

    suspend fun fetchSuggestions(query: String) = safeAccess {
        realFire
            .getReference("user")
            .orderByChild("basic-data/username")
            .startAt(query)
            .endBefore((query.first() + 1).toString())
            // TODO: Confirm if this works
            .orderByChild("basic-data/followers")
            .limitToFirst(12)
            .get()
            .await()
            .getValue<List<User>>()
    }

}