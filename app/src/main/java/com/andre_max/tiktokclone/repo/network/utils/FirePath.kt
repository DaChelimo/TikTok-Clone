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

package com.andre_max.tiktokclone.repo.network.utils

import com.andre_max.tiktokclone.models.video.VideoType
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class FirePath {
    private val myUid: String
        get() = Firebase.auth.uid ?: ""

    fun getTagsPath() = "tags"
    fun getTagInfo(tag: String) = "tags/$tag/info"
    fun getTagVideos(tag: String) = "tags/$tag/tag-videos"

    fun getUserInfo(uid: String = myUid): String =
        "users/$uid/basic-data"

    fun getMyFollowersPath(): String = getUserFollowerPath(myUid)
    fun getMyFollowingPath(): String = getUserFollowingPath(myUid)
    
    fun getUserFollowerPath(otherUid: String): String = "followers/$otherUid"
    fun getUserFollowingPath(otherUid: String): String = "following/$otherUid"

    fun getAllVideosPath(): String = "videos"
    fun getCommentsPath(videoId: String): String = "comments/$videoId"
//     "comments/$videoId/actual-comments"
//fun getCommentsSizePath(remoteUserVideoKey: String): String = "comments/$remoteUserVideoKey/comments-size"

    fun getMyLikedVideos(): String = "users/$myUid/liked-videos"
    fun getMyLikedComments(): String = "users/$myUid/liked-comments"
    fun getMyLikedReplies(): String = "users/$myUid/liked-replies"

    fun getUserVideos(uid: String, videoType: VideoType) = "users/$uid/$videoType"

    fun getTakenUsernames(userName: String) = "taken-usernames/$userName"
}