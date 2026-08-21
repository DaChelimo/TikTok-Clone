/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.comment

import com.andre_max.tiktokclone.domain.comments.CommentsRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * [CommentsRepo] against Firebase Realtime Database.
 *
 * The child-listener-into-map shape mirrors the legacy `DefaultCommentRepo.fetchComments`,
 * with the custom observable `SmartMap` replaced by a `callbackFlow` so collectors get plain
 * lists and cancellation detaches the Firebase listener automatically.
 */
class FirebaseCommentsRepo(
    private val fireAuth: FirebaseAuth = Firebase.auth,
    private val realFire: FirebaseDatabase = Firebase.database,
    private val firePath: FirePath = FirePath(),
) : CommentsRepo {

    override fun observeComments(videoId: String): Flow<List<Comment>> = callbackFlow {
        // Insertion-ordered so the thread renders oldest-first, matching Firebase's push-key
        // ordering and the legacy adapter's append order.
        val comments = LinkedHashMap<String, Comment>()
        val ref = realFire.getReference(firePath.getCommentsPath(videoId))

        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val comment = snapshot.getValue<Comment>() ?: return
                comments[comment.commentId] = comment
                trySend(comments.values.toList())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val comment = snapshot.getValue<Comment>() ?: return
                comments[comment.commentId] = comment
                trySend(comments.values.toList())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val comment = snapshot.getValue<Comment>() ?: return
                comments.remove(comment.commentId)
                trySend(comments.values.toList())
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), error.message)
                close(error.toException())
            }
        }

        ref.addChildEventListener(listener)
        trySend(emptyList())
        awaitClose { ref.removeEventListener(listener) }
    }.conflate()

    override fun observeCommentCount(videoId: String): Flow<Int> = callbackFlow {
        val ref = realFire.getReference(firePath.getAllVideosPath())
            .child(videoId)
            .child(TOTAL_COMMENTS_SIZE)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue<Int>() ?: 0)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), error.message)
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }.conflate()

    override suspend fun sendComment(
        videoId: String,
        text: String,
    ): EmptyResult<DataError.Network> = safeCall {
        val newCommentRef = realFire
            .getReference(firePath.getCommentsPath(videoId))
            .push()

        val comment = Comment(
            authorUid = fireAuth.uid.toString(),
            commentText = text,
            commentLikes = 0,
            replies = arrayListOf(),
            dateCreated = System.currentTimeMillis(),
            commentId = newCommentRef.key.toString(),
        )

        newCommentRef.setValue(comment).await()
        adjustTotalCommentsSize(videoId, increase = true)
    }

    override suspend fun isCommentLiked(
        videoId: String,
        commentId: String,
    ): Result<Boolean, DataError.Network> = safeCall {
        realFire.getReference(firePath.getMyLikedComments())
            .child(videoId)
            .child(commentId)
            .get()
            .await()
            .exists()
    }

    override suspend fun likeOrUnlikeComment(
        videoId: String,
        commentId: String,
    ): EmptyResult<DataError.Network> = safeCall {
        // One pre-state read decides both writes — the legacy repo re-read between them, which
        // made the count direction depend on its own first write having landed.
        val shouldLike = !realFire.getReference(firePath.getMyLikedComments())
            .child(videoId)
            .child(commentId)
            .get()
            .await()
            .exists()

        realFire.getReference(firePath.getMyLikedComments())
            .child(videoId)
            .child(commentId)
            .setValue(if (shouldLike) commentId else null)
            .await()

        val likesRef = realFire.getReference(firePath.getCommentsPath(videoId))
            .child(commentId)
            .child("commentLikes")
        val count = likesRef.get().await().getValue<Long>() ?: 0
        likesRef.setValue(if (shouldLike) count + 1 else count - 1).await()
    }

    private suspend fun adjustTotalCommentsSize(videoId: String, increase: Boolean) {
        val ref = realFire.getReference(firePath.getAllVideosPath())
            .child(videoId)
            .child(TOTAL_COMMENTS_SIZE)

        val count = ref.get().await().getValue<Long>() ?: 0
        ref.setValue(if (increase) count + 1 else count - 1).await()
    }

    companion object {
        private const val TOTAL_COMMENTS_SIZE = "totalCommentsSize"
    }
}
