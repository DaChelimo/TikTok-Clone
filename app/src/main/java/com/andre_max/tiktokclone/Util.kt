package com.andre_max.tiktokclone

import android.os.Parcelable
import android.text.format.DateFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

val firebaseAuth = FirebaseAuth.getInstance()
val firebaseDatabase = FirebaseDatabase.getInstance()
val firebaseStorage = FirebaseStorage.getInstance()

var userVerificationId : String? = null
var userToken: PhoneAuthProvider.ForceResendingToken? = null

const val GOOGLE_RC_SIGN_IN = 8223
const val CLIENT_ID_TYPE_3 = "1031105970956-mmutkrvn9fqo92mq5j2a32lh8nsu9qkf.apps.googleusercontent.com"

val NAME_UNAVAILABLE = "This username isn't available. Try a suggested username, or enter a new one."
val SHORT_USERNAME = "Include at least 2 characters in your username"
val LONG_USERNAME = "Username must be less than 25 characters"
val CONTAINS_SPACES = "Username must not contain any spaces"

val POSITION = "POSITION"

data class UserVideo(val url: String?, val duration: String?, val dateCreated: String?)
data class UserImage(val url: String?, val dateCreated: String?)

fun convertTimeToDisplayTime(timeInMillis: String): String {
    Timber.d("timeInMillis is $timeInMillis")
    var time = ""
    val seconds = timeInMillis.toLong() % 60
    val minutes = timeInMillis.toLong() / 60
    time = "${if (minutes.toString().length == 2)minutes else "0$minutes"}:${if (seconds.toString().length == 2)seconds else "0$seconds"}"
    Timber.d("TimeInMillis is $timeInMillis and time is $time")
    return time
}

data class User(var username: String, var followers: Long, var following: Long, var totalLikes: Long, var profilePictureUrl: String?, val uid: String) {
    constructor(): this("", -1, -1, -1, null, "")
}

@Parcelize
data class EmailBody(val email: String, val password: String): Parcelable