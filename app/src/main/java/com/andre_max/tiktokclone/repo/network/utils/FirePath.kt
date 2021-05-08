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