package com.andre_max.tiktokclone.models.reply

data class Reply(
    val authorUid: String,
    var replyText: String,
    var replyLikes: Long,
    val dateCreated: Long,
    val replyKey: String
) {
    constructor() : this("", "", -1, -1, "")
}