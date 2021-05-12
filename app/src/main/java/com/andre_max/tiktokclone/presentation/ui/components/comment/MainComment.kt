/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.presentation.ui.components.comment

import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.R
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
    private val liveUserComment: MutableLiveData<String>,
    private val onCommentVisibilityChanged: (Boolean) -> Unit
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
        setUpClickListeners()

        // If we have not add the observer, add it.
        if (!commentsMap.hasObservers())
            commentsMap.observeForever(commentsMapObserver)
        if (!commentsSize.hasObservers())
            commentsSize.observeForever(commentSizeObserver)
    }

    fun showCommentSection() {
        (binding.root as MotionLayout).transitionToEnd()
        onCommentVisibilityChanged(true)
    }

    fun hideCommentSection() {
        (binding.root as MotionLayout).transitionToStart()
        onCommentVisibilityChanged(false)
    }

    private val commentSizeObserver: (Int) -> Unit = { commentSize ->
        val context = binding.root.context
        binding.totalComments.text = context.getString(R.string.comment_size, NumbersUtils.formatCount(commentSize))
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

            if (userRepo.doesDeviceHaveAnAccount()) {
                sendComment(message)
            } else {
            // TODO: Once the button is clicked, let's show a small pop-up layout that tells him/her to sign up or login
            }
        }
    }

    private fun sendComment(message: String) {
        if (message.isBlank())
            ResUtils.showSnackBar(binding.root, R.string.empty_comment_error)
        else
            commentRepo.sendComment(message, remoteVideo.videoId)
    }

    fun destroy() {
        commentsMap.removeObserver(commentsMapObserver)
        commentsSize.removeObserver(commentSizeObserver)
    }
}