package com.andre_max.tiktokclone.models.sign_up

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmailBody(val email: String, val password: String): Parcelable