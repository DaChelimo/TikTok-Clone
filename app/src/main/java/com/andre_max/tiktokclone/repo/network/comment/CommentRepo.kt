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

package com.andre_max.tiktokclone.repo.network.comment

import androidx.lifecycle.LiveData
import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.utils.map.SmartMap

interface CommentRepo {
    /**
     * Adds a comment to the video
     *
     * @param commentText message in the comment
     * @param videoId reference to the video
     */
    fun sendComment(commentText: String, videoId: String)
    fun deleteComment(videoId: String)

    /**
     * Fetches comments for the specified video. The @link [ChildEventListener] will ensure that our map of Comments is always up to date.
     *
     * @param videoId a reference to the video
     * @return an observable array map containing the video's comments
     */
    fun fetchComments(videoId: String): SmartMap<String, Comment>

    /**
     * Emits the total comment size of the video. Given the fact that we are using @link [ValueEventListener], any changes to the comment size
     * is reflected to the onDataChange function.
     *
     * @param videoId a reference to the video
     * @return a liveData instance containing the total number of comments in that video
     */
    fun getTotalCommentsSize(videoId: String): LiveData<Int>

    /**
     * Checks if the comment is in the user's liked comments
     *
     * @param videoId A reference to the video
     * @param commentId A reference to the comment
     * @return Whether the comment has been liked by the user
     */
    suspend fun isCommentLiked(videoId: String, commentId: String): Boolean

    /**
     * Either likes or unlikes the comment
     *
     * @param videoId A reference to the video
     * @param commentId A reference to the comment
     */
    suspend fun likeOrUnlikeComment(videoId: String, commentId: String)
}