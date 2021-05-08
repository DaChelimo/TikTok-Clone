package com.andre_max.tiktokclone.repo.network.user

import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@Suppress("EXPERIMENTAL_API_USAGE")
class UserRepo {
    private val firePath = FirePath()
    private val realFire = Firebase.database

    suspend fun getUserProfile(uid: String?) = safeAccess {
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

    suspend fun addUserToDatabase(
        username: String,
        it: AuthResult,
        googleProfilePicture: String?
    ) =
        safeAccess {
            val user = User(
                username = username,
                followers = 0,
                following = 0,
                totalLikes = 0,
                profilePictureUrl = googleProfilePicture,
                uid = it.user?.uid.toString()
            )

            realFire
                .getReference(firePath.getUserInfo(Firebase.auth.uid ?: ""))
                .setValue(user)
                .await()

            true
        }

    suspend fun isFollowingAuthor(authorUid: String?): Boolean {
        return authorUid?.let {
            val myFollowingRef = realFire.getReference(firePath.getMyFollowingPath())
            myFollowingRef.child(authorUid).get().await().exists()
        } ?: false
    }

    suspend fun changeAuthorInMyFollowing(authorUid: String?, shouldAddAuthor: Boolean) {
        val myUid = Firebase.auth.uid ?: return

        authorUid?.let {
            changeFollowCount(uid = myUid, field = FOLLOWING, shouldIncrease = shouldAddAuthor)
            val myFollowingRef = realFire.getReference(firePath.getMyFollowingPath())
            myFollowingRef.child(authorUid).setValue(if (shouldAddAuthor) authorUid else null)
        }
    }

    suspend fun changeMeInAuthorFollowers(authorUid: String?, shouldAddMe: Boolean) {
        val myUid = Firebase.auth.uid ?: return

        authorUid?.let {
            changeFollowCount(uid = authorUid, field = FOLLOWERS, shouldIncrease = shouldAddMe)
            val authorFollowerRef = realFire.getReference(firePath.getUserFollowerPath(authorUid))
            authorFollowerRef.child(myUid).setValue(if (shouldAddMe) myUid else null)
        }
    }

    /**
     * Increases or decreases either the my following count or the author's follower count. This would have been two
     * different functions but to reduce function count, let's use one.
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