package com.andre_max.tiktokclone.presentation.ui.components.comment

import android.view.View
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.comment.CommentRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.utils.ResUtils
import com.andre_max.tiktokclone.utils.map.SmartAction
import com.andre_max.tiktokclone.utils.map.SmartMap
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.FieldPosition

class MainComment(
    private val binding: LargeVideoLayoutBinding,
    private var remoteVideo: RemoteVideo,
    private val commentRepo: CommentRepo,
    private val userRepo: UserRepo
) {
    private val commentsGroupAdapter = GroupAdapter<GroupieViewHolder>()

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var commentsSize: LiveData<Int>
    private lateinit var commentsMap: SmartMap<String, Comment>

    fun setUpCommentSection(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentsGroupAdapter
        }

        binding.sendCommentBtn.setOnClickListener {
            val message = binding.commentText.text.toString()

            if (message.isEmpty())
                ResUtils.showSnackBar(binding.root, message)
            else
                commentRepo.sendComment(message, remoteVideo.videoId)
        }

        // If the commentSize and commentsMap have not been initialized, get those comments
        if (!this::commentsSize.isInitialized && !this::commentsMap.isInitialized)
            refreshComments(remoteVideo, false)
    }

    fun showCommentSection() {
        binding.commentLayout.visibility = View.VISIBLE
    }

    fun hideCommentSection() {
        binding.commentLayout.visibility = View.GONE
    }

    /**
     * This function allows us to re-use this class by changing the current remoteVideo to a different one
     * and fetching the corresponding comments
     *
     * @param newRemoteVideo the remoteVideo to replace the current one with
     * @param forceRefresh a flag indicating whether to force a refresh or not. Useful when initializing the videos in setUpCommentSection()
     */
    fun refreshComments(newRemoteVideo: RemoteVideo, forceRefresh: Boolean = false) {
        if (newRemoteVideo.videoId != remoteVideo.videoId || forceRefresh) {
            remoteVideo = newRemoteVideo
            commentsSize = commentRepo.getTotalCommentsSize(remoteVideo.videoId)
            commentsMap = commentRepo.fetchComments(remoteVideo.videoId)

            // If we have not add the observer, add it.
            if (!commentsMap.hasObservers())
                commentsMap.observeForever(commentsMapObserver)
        }
    }

    private val commentsMapObserver: (SmartMap<String, Comment>) -> Unit = { smartMap ->
        when (smartMap.action) {
            SmartAction.Add -> {
                smartMap.actionValue?.let { addGroupToAdapter(it) }
            }
            SmartAction.AddAll -> {
                smartMap.actionMap?.values?.forEach { addGroupToAdapter(it) }
            }
            SmartAction.Remove -> {
                smartMap.actionKey?.let { removeGroup(smartMap.indexOf(it)) }
            }
            else -> {}
        }
    }

    private fun addGroupToAdapter(comment: Comment) {
        val videoId =  remoteVideo.videoId
        val commentId = comment.commentId
        coroutineScope.launch {
            val eachCommentGroup = EachCommentGroup(
                comment = comment,
                isLiked = commentRepo.isCommentLiked(videoId, commentId),
                commentAuthor = userRepo.getUserProfile(comment.authorUid).getData(),
                likeOrUnlikeComment = {
                    coroutineScope.launch {
                        commentRepo.changeCommentInMyLikedComments(videoId, commentId)
                        commentRepo.changeCommentLikesCount(videoId, commentId)
                    }
                }
            )

            commentsGroupAdapter.add(eachCommentGroup)
        }
    }

    private fun removeGroup(position: Int) { commentsGroupAdapter.removeGroupAtAdapterPosition(position) }

    /**
     * A function that cleans up our coroutine scope and is invoked in the fragment's onDestroy function.
     */
    fun onDestroy() {
        job.cancel()
    }
}