/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.domain.comments.CommentsRepo
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.comment.Comment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Hand-written [CommentsRepo] double — push comments in by setting [commentsFlow]. */
class FakeCommentsRepo : CommentsRepo {

    val commentsFlow = MutableStateFlow<List<Comment>>(emptyList())
    val countFlow = MutableStateFlow(0)

    val likedCommentIds = mutableSetOf<String>()

    data class SentComment(val videoId: String, val text: String)

    val sentComments = mutableListOf<SentComment>()
    var sendError: DataError.Network? = null

    /** Which video ids have been observed, so a test can pin that the right thread is opened. */
    val observedVideoIds = mutableListOf<String>()

    override fun observeComments(videoId: String): Flow<List<Comment>> {
        observedVideoIds += videoId
        return commentsFlow
    }

    override fun observeCommentCount(videoId: String): Flow<Int> = countFlow

    override suspend fun sendComment(videoId: String, text: String): EmptyResult<DataError.Network> {
        sendError?.let { return Result.Error(it) }
        sentComments += SentComment(videoId, text)
        return Result.Success(Unit)
    }

    override suspend fun isCommentLiked(
        videoId: String,
        commentId: String,
    ): Result<Boolean, DataError.Network> = Result.Success(commentId in likedCommentIds)

    override suspend fun likeOrUnlikeComment(
        videoId: String,
        commentId: String,
    ): EmptyResult<DataError.Network> {
        if (commentId in likedCommentIds) likedCommentIds -= commentId
        else likedCommentIds += commentId
        return Result.Success(Unit)
    }
}
