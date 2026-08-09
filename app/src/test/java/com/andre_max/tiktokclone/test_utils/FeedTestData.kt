/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.test_utils

import com.andre_max.tiktokclone.models.comment.Comment
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo

/** Builders for the feed/comments verticals — only the fields a test cares about vary. */

fun makeVideo(
    id: String,
    authorUid: String = "author-$id",
    likes: Long = 0,
    totalCommentsSize: Long = 0,
) = RemoteVideo(
    url = "https://videos.example/$id",
    description = "desc-$id",
    tags = mapOf(),
    duration = 10_000,
    videoId = id,
    dateCreated = 0,
    likes = likes,
    views = 0,
    authorUid = authorUid,
    totalCommentsSize = totalCommentsSize,
)

fun makeUser(
    uid: String,
    username: String = "user_$uid",
    profilePictureUrl: String? = null,
) = User(
    username = username,
    followers = 0,
    following = 0,
    totalLikes = 0,
    profilePictureUrl = profilePictureUrl,
    uid = uid,
)

fun makeComment(
    id: String,
    authorUid: String = "commenter-$id",
    text: String = "text-$id",
    likes: Long = 0,
) = Comment(
    authorUid = authorUid,
    commentText = text,
    commentLikes = likes,
    replies = arrayListOf(),
    dateCreated = 0,
    commentId = id,
)
