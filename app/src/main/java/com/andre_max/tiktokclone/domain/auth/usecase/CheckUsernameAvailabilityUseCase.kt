/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.auth.UsernameRepo
import com.andre_max.tiktokclone.domain.core.Result
import javax.inject.Inject

/**
 * Combines shape validation with the remote uniqueness check, so a malformed username never
 * reaches the network.
 */
class CheckUsernameAvailabilityUseCase @Inject constructor(
    private val validate: ValidateUsernameUseCase,
    private val repo: UsernameRepo,
) {
    /**
     * @return `Success(true)` when available, `Success(false)` when taken. Validation failures
     * short-circuit before any repo call and come back as the first [UsernameError]; a failed
     * lookup comes back as [UsernameError.Network].
     */
    suspend operator fun invoke(username: String): Result<Boolean, UsernameError> {
        validate(username).firstOrNull()?.let { return Result.Error(it) }

        return when (val result = repo.isUsernameAvailable(username)) {
            is Result.Success -> Result.Success(result.data)
            is Result.Error -> Result.Error(UsernameError.Network(result.error))
        }
    }
}
