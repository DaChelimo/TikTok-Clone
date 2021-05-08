package com.andre_max.tiktokclone.repo.network.utils

import com.andre_max.tiktokclone.models.TheResult.Companion.theError
import com.andre_max.tiktokclone.models.TheResult.Companion.theSuccess
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.getValue
import timber.log.Timber

/**
 * A super duper epic wrapper that logs and handles network calls to Firebase
 *
 * @param firebaseLambda A suspend lambda that makes a network call and returns T
 * @return A custom result containing T
 */
suspend fun <T> safeAccess(firebaseLambda: suspend () -> T) = try {
    theSuccess(firebaseLambda())
} catch (e: Exception) {
    Timber.e(e)
    theError(e)
}

//@Suppress("UNCHECKED_CAST")
//fun <T> DataSnapshot?.getValue() = this?.value as? T
inline fun <reified T> DataSnapshot?.forceValue() = this?.getValue<T>()!!

@Suppress("UNCHECKED_CAST")
fun <T> DataSnapshot.getListValue() =
    getValue<Map<String, T>>()?.values?.toList() ?: listOf()

/*


 */