/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.username

import com.andre_max.tiktokclone.domain.auth.UsernameRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Firebase-backed [UsernameRepo]. Every call goes through `safeCall`, so nothing above this
 * layer ever sees a `FirebaseException`.
 *
 * Coexists with the legacy `DefaultNameRepo` (strangler-fig, non-negotiable #2) — that one
 * returns raw `R.string` ids from the data layer, which is exactly what this replaces.
 */
class DefaultUsernameRepo(
    private val realFire: FirebaseDatabase = Firebase.database,
) : UsernameRepo {

    override suspend fun isUsernameAvailable(
        username: String,
    ): Result<Boolean, DataError.Network> = TODO("Pass 2")

    override suspend fun claimUsername(username: String): EmptyResult<DataError.Network> =
        TODO("Pass 2")
}
