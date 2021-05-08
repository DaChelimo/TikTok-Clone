package com.andre_max.tiktokclone.fakes.repo.network.user

import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.andre_max.tiktokclone.repo.network.user.INameRepo

class FakeNameRepo(val alreadyExistingNames: MutableList<String>): INameRepo {
    override suspend fun doesNameExist(username: String) = alreadyExistingNames.contains(username)

    override suspend fun registerUserName(username: String) = safeAccess {
        alreadyExistingNames.add(username)
    }
}