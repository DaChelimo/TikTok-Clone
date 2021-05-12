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

import com.andre_max.tiktokclone.models.TheResult

interface NameRepo {
    /**
     * Checks for errors in the username and returns a string resource referencing the error.
     *
     * @return a string resource id that represents the error. Null means no error and the username is valid.
     */
    suspend fun getErrorFromUsername(username: String): Int?

    /**
     * Checks whether the name exists in the database
     *
     * @return true if the name has already been taken
     */
    suspend fun doesNameExist(username: String): Boolean

    /**
     * Registers the name to the Database
     */
    suspend fun registerUserName(username: String): TheResult<Boolean>

    /**
     * Generates a name from the user's google account name
     * @return A name derived from the user's google account name
     */
    suspend fun getUsernameFromGoogleUsername(googleName: String): String

    /**
     * Generates a truly random name
     */
    suspend fun generateRandomName(): String
}