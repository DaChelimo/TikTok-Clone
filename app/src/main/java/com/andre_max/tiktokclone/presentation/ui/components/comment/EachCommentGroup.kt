package com.andre_max.tiktokclone.presentation.ui.components.comment

import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.EachCommentItemBinding
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.utils.ImageUtils
import com.xwray.groupie.viewbinding.BindableItem

class EachCommentGroup(
    private val comment: Comment,
    private var isLiked: Boolean = false,
    private val commentAuthor: User?,
    private val likeOrUnlikeComment: () -> Unit
) : BindableItem<EachCommentItemBinding>() {
    var likesCount = comment.commentLikes

    override fun bind(binding: EachCommentItemBinding, position: Int) {
        binding.personComment.text = comment.commentText
        binding.likesCount.text = likesCount.toString()

        binding.personUsername.text = commentAuthor?.username
        ImageUtils.loadGlideImage(binding.personImage, commentAuthor?.profilePictureUrl)

        binding.likeIconImage.setOnClickListener {
            likeOrUnlikeComment()
            isLiked = !isLiked
        }
    }


    override fun initializeViewBinding(view: View) =
        EachCommentItemBinding.bind(view)

    override fun getLayout(): Int = R.layout.each_comment_item

}