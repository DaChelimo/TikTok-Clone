/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.comments

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.comment.Comment
import kotlinx.coroutines.flow.Flow

/**
 * A video's comment thread.
 *
 * The observe functions are cold flows that stay live while collected (the legacy repo used a
 * custom observable map plus LiveData for the same purpose); collecting them is how the sheet
 * sees new comments arrive in real time. There is deliberately no pagination — the thread has
 * never had a cap (docs/SPEC.md §5) and adding one is a product decision, not a migration step.
 */
interface CommentsRepo {

    fun observeComments(videoId: String): Flow<List<Comment>>

    /** Live total comment count of the video, as shown in the sheet header. */
    fun observeCommentCount(videoId: String): Flow<Int>

    suspend fun sendComment(videoId: String, text: String): EmptyResult<DataError.Network>

    suspend fun isCommentLiked(
        videoId: String,
        commentId: String,
    ): Result<Boolean, DataError.Network>

    /** Toggles my like on the comment and adjusts its like count to match. */
    suspend fun likeOrUnlikeComment(
        videoId: String,
        commentId: String,
    ): EmptyResult<DataError.Network>
}
