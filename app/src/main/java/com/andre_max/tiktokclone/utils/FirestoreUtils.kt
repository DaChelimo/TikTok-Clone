package com.andre_max.tiktokclone.utils

import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.utils.FirebaseUtils.firestore
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("EXPERIMENTAL_API_USAGE")
object FirestoreUtils {

    fun getAllVideos() = channelFlow {
        firestore.collection("videos")
            .limit(12)
            .get()
            .addOnSuccessListener {
                launch {
                    send(it.toObjects<RemoteUserVideo>())
                    close()
                }
            }

        awaitClose()
    }

    fun getMyUserProfile() = getUserProfile(FirebaseUtils.firebaseAuth.uid)

    fun getUserProfile(uid: String?) = channelFlow {
        firestore.collection("users")
            .document(uid ?: return@channelFlow)
            .get()
            .addOnSuccessListener {
                launch {
                    send(it.toObject<User>())
                    close()
                }
            }
            .addOnFailureListener {
                Timber.e(it)
            }

        awaitClose()
    }

    fun doesNameExist(username: String) =
        firestore.collection("taken-usernames")
            .document(username)
            .get()
            .result
            ?.exists() == true

    fun createAccountInDatabase(
        username: String,
        it: AuthResult,
        googleProfilePicture: String?
    ) = channelFlow {
        val user = User(
            username = username,
            followers = arrayListOf(),
            following = arrayListOf(),
            totalLikes = 0,
            profilePictureUrl = googleProfilePicture,
            uid = it.user?.uid.toString()
        )

        firestore.collection("users")
            .document(FirebaseUtils.firebaseAuth.uid ?: return@channelFlow)
            .set(user)
            .addOnSuccessListener {
                Timber.d("Success adding user to database")
                launch {
                    send(Result.success(user))
                }
//                (activity as MainActivity).navView.visibility = View.VISIBLE
//                registerUserName(username)
//                findNavController().navigate(CreateUsernameFragmentDirections.actionCreateUsernameFragmentToHomeFragment())
            }
            .addOnFailureListener { exception ->
                Timber.e(exception)
//                Toast.makeText(
//                    this.requireContext(),
//                    "Error occurred creating account in database. Try again later.",
//                    Toast.LENGTH_SHORT
//                ).show()
                launch {
                    send(Result.failure<User>(exception))
                }
            }
    }

    fun registerUserName(username: String) {
        firestore.collection("taken-usernames")
            .document(username)
            .set(username)
            .addOnSuccessListener {
                Timber.d("Success. Registered name: $username")
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

}