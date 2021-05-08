package com.andre_max.tiktokclone.repo.network.comment

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.andre_max.tiktokclone.utils.map.SmartMap
import com.andre_max.tiktokclone.utils.runAsync
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class CommentRepo {
    private val firePath = FirePath()
    private val realFire = Firebase.database
    private val fireAuth = Firebase.auth

    fun sendComment(commentText: String, videoId: String) {
        val newCommentRef = realFire
            .getReference(firePath.getCommentsPath(videoId))
            .push()
        val commentId = newCommentRef.key.toString()

        val comment = Comment(
            authorUid = fireAuth.uid.toString(),
            commentText = commentText,
            commentLikes = 0,
            replies = arrayListOf(),
            dateCreated = System.currentTimeMillis(),
            commentId = commentId
        )

        runAsync {
            newCommentRef.setValue(comment)
            changeTotalCommentSize(videoId, shouldIncrease = true)
        }
    }


    fun deleteComment(videoId: String) {
        val newCommentRef = realFire
            .getReference(firePath.getCommentsPath(videoId))
            .push()

        runAsync {
            newCommentRef.setValue(null)
            changeTotalCommentSize(videoId, shouldIncrease = false)
        }
    }

    /**
     * Fetches comments for the specified video. The @link [ChildEventListener] will ensure that our map of Comments is always up to date.
     *
     * @param videoId a reference to the video
     * @return an observable array map containing the video's comments
     */
    fun fetchComments(videoId: String): SmartMap<String, Comment> {
        val commentsMap = SmartMap<String, Comment>()
        val videoCommentsRef = realFire.getReference(firePath.getCommentsPath(videoId))
        videoCommentsRef.addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val comment = snapshot.getValue<Comment>() ?: return
                    Timber.d("comment.commentText is ${comment.commentText}")
                    commentsMap[comment.commentId] = comment
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val comment = snapshot.getValue<Comment>() ?: return
                    Timber.d("comment.commentText is ${comment.commentText}")
                    commentsMap[comment.commentId] = comment
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val comment = snapshot.getValue<Comment>() ?: return
                    commentsMap.remove(comment.commentId)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), error.message)
                }
            }
        )

        return commentsMap
    }

    /**
     * Emits the total comment size of the video. Given the fact that we are using @link [ValueEventListener], any changes to the comment size
     * is reflected to the onDataChange function.
     *
     * @param videoId a reference to the video
     * @return a liveData instance containing the total number of comments in that video
     */
    fun getTotalCommentsSize(videoId: String): LiveData<Int> {
        val totalCommentSize = MutableLiveData<Int>()
        val totalCommentSizeRef = realFire
            .getReference(firePath.getAllVideosPath())
            .child(videoId)
            .child("totalCommentsSize")

        totalCommentSizeRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    totalCommentSize.value = snapshot.getValue<Int>() ?: 0
                    Timber.d("totalCommentSize.value changed and is now ${totalCommentSize.value}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), error.message)
                }
            }
        )

        return totalCommentSize
    }

    private suspend fun changeTotalCommentSize(videoId: String, shouldIncrease: Boolean) {
        val totalCommentsSizeRef = realFire.getReference(firePath.getAllVideosPath())
            .child(videoId)
            .child("totalCommentsSize")

        var count = totalCommentsSizeRef.get().await().getValue<Int>() ?: 0
        if (shouldIncrease) count++ else count--
        totalCommentsSizeRef.setValue(count)
    }

    /**
     * Adds or removes this comment from the user's liked comments.
     * @param videoId A reference to the video
     * @param commentId A reference to the comment
     */
    suspend fun changeCommentInMyLikedComments(videoId: String, commentId: String) {
        // Whether we should add the comment to the user's liked comments or to remove it
        val shouldAdd = !isCommentLiked(videoId, commentId)
        val myLikedCommentsRef = realFire.getReference(firePath.getMyLikedComments())

        myLikedCommentsRef
            .child(videoId)
            .child(commentId)
            .setValue(if (shouldAdd) commentId else null)
    }

    /**
     * Increases or reduces the like count of the comment
     * @param videoId A reference to the video
     * @param commentId A reference to the comment
     */
    suspend fun changeCommentLikesCount(
        videoId: String,
        commentId: String
    ) {
        val shouldIncrease = isCommentLiked(videoId, commentId)
        val commentRef = realFire
            .getReference(firePath.getCommentsPath(videoId))
            .child(commentId)

        commentRef.child("commentLikes").runAsync {
            var likesCount = get().await().getValue<Int>() ?: 0
            if (shouldIncrease) likesCount++ else likesCount--

            setValue(likesCount)
        }
    }

    /**
     * Checks if the comment is in the user's liked comments
     *
     * @param videoId A reference to the video
     * @param commentId A reference to the comment
     * @return Whether the comment has been liked by the user
     */
    suspend fun isCommentLiked(videoId: String, commentId: String): Boolean {
        val myLikedVideos = realFire.getReference(firePath.getMyLikedComments())
        val isLiked = myLikedVideos.child(videoId).child(commentId).get().await().exists()
        Timber.d("isLiked is $isLiked")
        return isLiked
    }

}