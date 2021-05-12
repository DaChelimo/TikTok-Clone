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

import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@Suppress("EXPERIMENTAL_API_USAGE")
class DefaultUserRepo(
    private val fireAuth: FirebaseAuth = Firebase.auth,
    private val firePath: FirePath = FirePath(),
    private val realFire: FirebaseDatabase = Firebase.database
) : UserRepo {

    override fun doesDeviceHaveAnAccount() = fireAuth.currentUser != null

    override suspend fun getUserProfile(uid: String?) = safeAccess {
        Timber.d("uid is $uid")
        val userProfile = realFire
            .getReference(firePath.getUserInfo(uid ?: ""))
            .get()
            .addOnCompleteListener {
                val user = it.result.getValue<User>()
                Timber.d("user is $user")
            }
            .await()
            .getValue<User>()
        Timber.d("userProfile is $userProfile")
        userProfile
    }

    override suspend fun addUserToDatabase(
        username: String,
        authResult: AuthResult,
        googleProfilePicture: String?
    ) =
        safeAccess {
            val user = User(
                username = username,
                followers = 0,
                following = 0,
                totalLikes = 0,
                profilePictureUrl = googleProfilePicture,
                uid = authResult.user?.uid.toString()
            )

            realFire
                .getReference(firePath.getUserInfo(Firebase.auth.uid ?: ""))
                .setValue(user)
                .await()

            true
        }

    override suspend fun getUserVideos(uid: String?, videoType: VideoType) = safeAccess {
        val listOfUserVideoId = realFire
            .getReference(firePath.getUserVideos(uid ?: "", videoType))
            .get()
            .await()
            .getValue<Map<String, String>>()
            ?.values
            ?.toList() ?: listOf()

        val allVideos = realFire.getReference(firePath.getAllVideosPath())
        listOfUserVideoId.map { videoId ->
            allVideos.child(videoId).get().await().getValue<RemoteVideo>()
        }
    }

    override suspend fun isFollowingAuthor(authorUid: String?): Boolean {
        return authorUid?.let {
            val myFollowingRef = realFire.getReference(firePath.getMyFollowingPath())
            myFollowingRef.child(authorUid).get().await().exists()
        } ?: false
    }

    override suspend fun followAuthor(authorUid: String?) {
        changeAuthorInMyFollowing(authorUid = authorUid, shouldAddAuthor = true)
        changeMeInAuthorFollowers(authorUid = authorUid, shouldAddMe = true)
    }

    override suspend fun unFollowAuthor(authorUid: String?) {
        changeAuthorInMyFollowing(authorUid = authorUid, shouldAddAuthor = false)
        changeMeInAuthorFollowers(authorUid = authorUid, shouldAddMe = false)
    }

    private suspend fun changeAuthorInMyFollowing(authorUid: String?, shouldAddAuthor: Boolean) {
        val myUid = Firebase.auth.uid ?: return

        authorUid?.let {
            changeFollowCount(uid = myUid, field = FOLLOWING, shouldIncrease = shouldAddAuthor)
            val myFollowingRef = realFire.getReference(firePath.getMyFollowingPath())
            myFollowingRef.child(authorUid).setValue(if (shouldAddAuthor) authorUid else null)
        }
    }

    private suspend fun changeMeInAuthorFollowers(authorUid: String?, shouldAddMe: Boolean) {
        val myUid = Firebase.auth.uid ?: return

        authorUid?.let {
            changeFollowCount(uid = authorUid, field = FOLLOWERS, shouldIncrease = shouldAddMe)
            val authorFollowerRef = realFire.getReference(firePath.getUserFollowerPath(authorUid))
            authorFollowerRef.child(myUid).setValue(if (shouldAddMe) myUid else null)
        }
    }

    /**
     * Increases or decreases either the my following count or the author's follower count.
     * This would have been two different functions but to reduce function count, let's use one.
     *
     * @param uid a uid referencing the account data to change
     * @param field indicates whether to change the followers module or the following module
     */
    private suspend fun changeFollowCount(uid: String, field: String, shouldIncrease: Boolean) {
        val databaseReference = realFire
            .getReference("users/$uid")
            .child(field)

        var count = databaseReference.get().await().getValue<Int>() ?: 0
        if (shouldIncrease) count++ else count--

        databaseReference.setValue(count)
    }

    companion object {
        const val FOLLOWERS = "followers"
        const val FOLLOWING = "following"
    }
}