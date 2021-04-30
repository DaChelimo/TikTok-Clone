package com.andre_max.tiktokclone.models.user

data class User(
    var username: String,
    var followers: Int,
    var following: Int,
    var totalLikes: Long,
    var profilePictureUrl: String?,
    val uid: String,
    // TODO: Add settings to tweak this to true
    var showLikedVideos: Boolean = false
) {
    constructor() : this("", 0, 0, 0, null, "")
}