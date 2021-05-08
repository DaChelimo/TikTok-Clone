package com.andre_max.tiktokclone.repo.network.user

import com.andre_max.tiktokclone.models.TheResult

interface INameRepo {
    suspend fun doesNameExist(username: String): Boolean

    suspend fun registerUserName(username: String): TheResult<Boolean>
}