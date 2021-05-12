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

package com.andre_max.tiktokclone.repo.network.user

import com.andre_max.tiktokclone.models.TheResult
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.google.firebase.auth.AuthResult

interface UserRepo {
    /**
     * Whether there is an account on the user's device
     *
     * @return true if the device has a current user
     */
    fun doesDeviceHaveAnAccount(): Boolean

    /**
     * Retrieves the user's profile based on the user's uid
     *
     * @return the user profile safely wrapped in a try catch statement
     */
    suspend fun getUserProfile(uid: String?): TheResult<User?>

    /**
     * Checks if the current user is following the video author
     *
     * @return true if the current user is following him/her
     */
    suspend fun isFollowingAuthor(authorUid: String?): Boolean

    /**
     * Saves the user to the database after signing up.
     *
     * @param username the user's username
     * @param authResult the authResult obtained after signing up that is useful for getting the uid
     * @param googleProfilePicture the url path to the user's profile picture. This has a value if Google Sign-In was used
     */
    suspend fun addUserToDatabase(
        username: String,
        authResult: AuthResult,
        googleProfilePicture: String?
    ): TheResult<Boolean>

    /**
     * Follow another user based on their uid
     *
     * @param authorUid reference to the user
     */
    suspend fun followAuthor(authorUid: String?)

    /**
     * Unfollow another user based on their uid
     *
     * @param authorUid reference to the user
     */
    suspend fun unFollowAuthor(authorUid: String?)

    /**
     * Get's the videos of a specific user
     *
     * @param uid reference to the user
     * @param videoType type of videos to fetch whether public, private or liked
     */
    suspend fun getUserVideos(uid: String?, videoType: VideoType): TheResult<List<RemoteVideo?>>
}