package com.andre_max.tiktokclone.presentation.ui.components.comment

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.comment.CommentRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.utils.NumbersUtils
import com.andre_max.tiktokclone.utils.ResUtils
import com.andre_max.tiktokclone.utils.map.SmartAction
import com.andre_max.tiktokclone.utils.map.SmartMap
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainComment(
    private val binding: LargeVideoLayoutBinding,
    private val commentRepo: CommentRepo,
    private val remoteVideo: RemoteVideo,
    private val userRepo: UserRepo,
    private val scope: CoroutineScope,
    private val liveUserComment: MutableLiveData<String>
) {
    private val commentsGroupAdapter = GroupAdapter<GroupieViewHolder>()

    val commentsSize by lazy { commentRepo.getTotalCommentsSize(remoteVideo.videoId) }
    val commentsMap by lazy { commentRepo.fetchComments(remoteVideo.videoId) }

    init {
        binding.commentRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentsGroupAdapter
        }
    }

    fun init() {
        // Reset any previous user comment
        liveUserComment.value = ""

        // If we have not add the observer, add it.
        if (!commentsMap.hasObservers())
            commentsMap.observeForever(commentsMapObserver)
        if (!commentsSize.hasObservers())
            commentsSize.observeForever(commentSizeObserver)
    }

    fun showCommentSection() {
        binding.commentLayout.visibility = View.VISIBLE
    }

    fun hideCommentSection() {
        binding.commentLayout.visibility = View.GONE
    }

    private val commentSizeObserver: (Int) -> Unit = { commentSize ->
        binding.totalComments.text = NumbersUtils.formatCount(commentSize)
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
            else -> {
            }
        }
    }

    private fun addGroupToAdapter(comment: Comment) {
        val videoId = remoteVideo.videoId
        val commentId = comment.commentId
        scope.launch {
            val eachCommentGroup = EachCommentGroup(
                comment = comment,
                isLiked = commentRepo.isCommentLiked(videoId, commentId),
                commentAuthor = userRepo.getUserProfile(comment.authorUid).tryData(),
                likeOrUnlikeComment = {
                    scope.launch {
                        commentRepo.changeCommentInMyLikedComments(videoId, commentId)
                        commentRepo.changeCommentLikesCount(videoId, commentId)
                    }
                }
            )

            commentsGroupAdapter.add(eachCommentGroup)
        }
    }

    private fun removeGroup(position: Int) {
        commentsGroupAdapter.removeGroupAtAdapterPosition(position)
    }

    fun setUpClickListeners() {
        binding.sendCommentBtn.setOnClickListener {
            val message = liveUserComment.value ?: ""

            if (message.isBlank())
                ResUtils.showSnackBar(binding.root, message)
            else
                commentRepo.sendComment(message, remoteVideo.videoId)
        }
    }

    fun destroy() {
        commentsMap.removeObserver(commentsMapObserver)
        commentsSize.removeObserver(commentSizeObserver)
    }
}