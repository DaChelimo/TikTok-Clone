/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult

/**
 * Writes the profile record that turns an authenticated identity into a usable account.
 *
 * Narrower than the legacy `repo.network.user.UserRepo` on purpose: sign-up only ever creates a
 * profile, and the legacy interface takes a Firebase `AuthResult` — which is exactly the type
 * `domain` may not see. Widen this only when a second caller in `domain` needs more.
 */
interface UserProfileRepo {

    suspend fun createProfile(
        uid: String,
        username: String,
        profilePictureUrl: String?,
    ): EmptyResult<DataError.Network>
}
