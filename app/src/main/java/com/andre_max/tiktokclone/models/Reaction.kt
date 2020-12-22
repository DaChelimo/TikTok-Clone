package com.andre_max.tiktokclone.models

import android.widget.ImageView
import com.andre_max.tiktokclone.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber

class Reaction(private val remoteUserVideo: RemoteUserVideo, private val likedIcon: ImageView) {
    var wasLiked: Boolean = false

    fun checkIfVideoWasLiked() {
        val myLikedVideos = firebaseDatabase.getReference(getUserLikedVideosPath()).child(remoteUserVideo.firestoreId)

        myLikedVideos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                wasLiked = snapshot.exists()
                Timber.d("internal wasLiked is $wasLiked")
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })

        Timber.d("returning wasLiked is $wasLiked")
    }

    private fun initIcon() {
        checkIfVideoWasLiked()
        if (wasLiked) likedIcon.setImageResource(R.drawable.heart_red) else likedIcon.setImageResource(R.drawable.heart_white)
    }

    init {
        initIcon()
    }

    fun likeVideo() {
        val myLikedVideos = firebaseDatabase.getReference(getUserLikedVideosPath()).child(remoteUserVideo.firestoreId)
        val actualVideoRef = firebaseDatabase.getReference("${getAllVideosPath()}/${remoteUserVideo.firestoreId}").child("likes")
        val creatorVideoRef = firebaseDatabase.getReference(getUserPublicVideosPath(remoteUserVideo.creatorUid)).child("${remoteUserVideo.firestoreId}/likes")

        actualVideoRef.addListenerForSingleValueEvent(likeVideoValueEventFun(actualVideoRef))
        creatorVideoRef.addListenerForSingleValueEvent(likeVideoValueEventFun(creatorVideoRef))
        myLikedVideos.setValue(remoteUserVideo.firestoreId)
            .addOnSuccessListener {
                likedIcon.setImageResource(R.drawable.heart_red)
                wasLiked = true
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }



    fun removeLike() {
        val myLikedVideos = firebaseDatabase.getReference(getUserLikedVideosPath()).child(remoteUserVideo.firestoreId)
        val actualVideoRef = firebaseDatabase.getReference("${getAllVideosPath()}/${remoteUserVideo.firestoreId}").child("likes")
        val creatorVideoRef = firebaseDatabase.getReference(getUserPublicVideosPath(remoteUserVideo.creatorUid)).child("${remoteUserVideo.firestoreId}/likes")

        actualVideoRef.addListenerForSingleValueEvent(removeLikeVideoValueEventFun(actualVideoRef))
        creatorVideoRef.addListenerForSingleValueEvent(removeLikeVideoValueEventFun(creatorVideoRef))
        myLikedVideos.removeValue()
            .addOnSuccessListener {
                Timber.d("removed liked video")
                likedIcon.setImageResource(R.drawable.heart_white)
                wasLiked = false
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

}