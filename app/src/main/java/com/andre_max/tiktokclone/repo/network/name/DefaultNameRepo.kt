/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.repo.network.name

import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class DefaultNameRepo(private val realFire: FirebaseDatabase = Firebase.database) : NameRepo {
    private val firePath = FirePath()

    override suspend fun getErrorFromUsername(username: String) = when {
        username.length < 4 -> R.string.short_username
        username.contains(" ") -> R.string.name_contains_spaces
        username.length >= 25 -> R.string.long_username
        doesNameExist(username) -> R.string.name_unavailable
        else -> null
    }

    override suspend fun doesNameExist(username: String) =
        realFire
            .getReference(firePath.getTakenUsernames(username))
            .get()
            .await()
            ?.exists() == true

    override suspend fun registerUserName(username: String) = safeAccess {
        realFire
            .getReference(firePath.getTakenUsernames(username))
            .setValue(username)
            .await()

        true
    }

    override suspend fun generateRandomName(): String {
        var randomUserName = ""
        val initialNameArray = listOf("user", "account", "person")

        while (doesNameExist(randomUserName) || randomUserName == "") {
            val randomNumber = Random.nextInt(100_000_000).toString()
            randomUserName = initialNameArray.random() + randomNumber
        }

        return randomUserName
    }

    override suspend fun getUsernameFromGoogleUsername(googleName: String): String {
        var userName = googleName

        while (doesNameExist(userName)) {
            val randomNumber = Random.nextInt(100_000_000).toString()
            userName = googleName + randomNumber
        }

        return userName
    }

}