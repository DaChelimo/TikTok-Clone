package com.andre_max.tiktokclone

import android.os.Parcelable
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.parcelize.Parcelize
import timber.log.Timber

var userVerificationId: String? = null
var userToken: PhoneAuthProvider.ForceResendingToken? = null

const val GOOGLE_RC_SIGN_IN = 8223
const val CLIENT_ID_TYPE_3 =
    "1031105970956-mmutkrvn9fqo92mq5j2a32lh8nsu9qkf.apps.googleusercontent.com"