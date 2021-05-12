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

import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.EachCommentItemBinding
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.user.User
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