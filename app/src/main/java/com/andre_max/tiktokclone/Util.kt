package com.andre_max.tiktokclone

import android.os.Parcelable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import timber.log.Timber
import kotlin.collections.ArrayList

val firebaseAuth = FirebaseAuth.getInstance()
val firebaseDatabase = FirebaseDatabase.getInstance()
val firebaseStorage = FirebaseStorage.getInstance()

var userVerificationId : String? = null
var userToken: PhoneAuthProvider.ForceResendingToken? = null

const val GOOGLE_RC_SIGN_IN = 8223
const val CLIENT_ID_TYPE_3 = "1031105970956-mmutkrvn9fqo92mq5j2a32lh8nsu9qkf.apps.googleusercontent.com"

const val NAME_UNAVAILABLE = "This username isn't available. Try a suggested username, or enter a new one."
const val SHORT_USERNAME = "Include at least 2 characters in your username"
const val LONG_USERNAME = "Username must be less than 25 characters"
const val CONTAINS_SPACES = "Username must not contain any spaces"

const val POSITION = "POSITION"

fun getUserBasicDataPath(uid: String = firebaseAuth.currentUser?.uid.toString()): String = "users/$uid/basic-data"
fun getUserPublicVideosPath(uid: String = firebaseAuth.currentUser?.uid.toString()): String = "users/$uid/public-videos"
fun getUserPrivateVideosPath(): String = "users/${firebaseAuth.currentUser?.uid}/private-videos"
fun getUserLikedVideosPath(): String = "users/${firebaseAuth.currentUser?.uid}/liked-videos"

fun getMyFollowersPath(): String = "users/${firebaseAuth.currentUser?.uid}/followers"
fun getMyFollowingPath(): String = "users/${firebaseAuth.currentUser?.uid}/following"

fun getOtherFollowerPath(otherUid: String): String = "users/$otherUid/followers"
fun getOthersFollowingPath(otherUid: String): String = "users/$otherUid/following"

fun getAllVideosPath(): String = "videos"
fun getCommentsPath(remoteUserVideoKey: String): String = "comments/$remoteUserVideoKey/actual-comments"
//fun getCommentsSizePath(remoteUserVideoKey: String): String = "comments/$remoteUserVideoKey/comments-size"

fun getMyLikedComments(): String = "users/${firebaseAuth.currentUser?.uid}/liked-comments"
fun getMyLikedReplies(): String = "users/${firebaseAuth.currentUser?.uid}/liked-replies"


@Parcelize
data class LocalUserVideo(val url: String?, val duration: String?, val dateCreated: String?): Parcelable {
    constructor(): this(null, null, null)
}
data class LocalUserImage(val url: String?, val dateCreated: String?)

@Parcelize
data class RemoteUserVideo(val url: String, val description: String?, val tags: List<String>, val duration: Long, val key: String, val dateCreated: Long, var likes: Long = 0, var views: Long = 0, val creatorUid: String,  var totalCommentsSize: Long = 0): Parcelable {
    constructor(): this("", null, listOf(), -1, "", -1, -1, -1, "",  -1)
}


data class Comment(val authorUid: String, var commentText: String, var commentLikes: Long, val replies: ArrayList<Reply>, val dateCreated: Long, val commentKey: String) {
    constructor(): this("", "", -1, arrayListOf(), -1, "")
}
data class Reply(val authorUid: String, var replyText: String, var replyLikes: Long, val dateCreated: Long, val replyKey: String) {
    constructor(): this("", "", -1, -1, "")
}

fun convertTimeToDisplayTime(timeInMillis: String): String {
    Timber.d("timeInMillis is $timeInMillis")
    var time = ""
    val seconds = timeInMillis.toLong() % 1000
    val minutes = timeInMillis.toLong() / 1000
    time = "${if (minutes.toString().length == 2)minutes else "0$minutes"}:${if (seconds.toString().length == 2)seconds else "0$seconds"}"
    Timber.d("TimeInMillis is $timeInMillis and time is $time")
    return time
}

fun getCurrentUser(): User? {
    val ref = firebaseDatabase.getReference(getUserBasicDataPath())
    var currentUser: User? = null
    ref.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val user = snapshot.getValue(User::class.java) ?: return
            Timber.d("user is $user")
            currentUser = user
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.e(error.message)
        }
    })

    Timber.d("currentUser is $currentUser")
    return currentUser
}

data class User(var username: String, var followers: Long, var following: Long, var totalLikes: Long, var profilePictureUrl: String?, val uid: String) {
    constructor(): this("", -1, -1, -1, null, "")
}

@Parcelize
data class EmailBody(val email: String, val password: String): Parcelable



fun likeVideoValueEventFun(ref: DatabaseReference): ValueEventListener {
    return object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var likes = snapshot.getValue(Long::class.java)


            Timber.d("likes is $likes")

            if (likes == null) {
                return
            }

            likes++
            ref.setValue(likes)
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.e(error.toException())
        }
    }
}

fun removeLikeVideoValueEventFun(ref: DatabaseReference): ValueEventListener {
    return object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var likes = snapshot.getValue(Long::class.java)

            if (likes == null) {
                Timber.d("likes is null")
                return
            }

            likes--
            ref.setValue(likes)
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.e(error.toException())
        }
    }
}

