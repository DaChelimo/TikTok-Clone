/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result

/**
 * Username uniqueness, as the domain sees it. The Firebase implementation lives in
 * `repo/network/username` — nothing here mentions Firebase or Android.
 */
interface UsernameRepo {
    /** @return `Success(true)` when the username is free to claim. */
    suspend fun isUsernameAvailable(username: String): Result<Boolean, DataError.Network>

    suspend fun claimUsername(username: String): EmptyResult<DataError.Network>

    /**
     * Gives a claimed username back.
     *
     * Exists for sign-up rollback, not as a rename feature: `CompleteSignUpUseCase` claims the
     * name before writing the profile, so a failed profile write has to return it or the name is
     * reserved to an account that was itself rolled back.
     */
    suspend fun releaseUsername(username: String): EmptyResult<DataError.Network>
}
