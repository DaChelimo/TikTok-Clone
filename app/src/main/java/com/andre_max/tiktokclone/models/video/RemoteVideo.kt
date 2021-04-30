package com.andre_max.tiktokclone.models.video

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A class that contains details regarding the video. This class is stored remotely
 * in Firebase hence we need a no-argument constructor to allow Firebase to deserialize the Json to a POJO
 * according to this post on <a href="https://stackoverflow.com/a/48406247/13834895">StackOverflow</a>.
 */
@Parcelize
data class RemoteVideo(
    val url: String,
    val description: String?,
    val tags: List<String>,
    val duration: Long,
    val videoId: String,
    val dateCreated: Long,
    var likes: Long = 0,
    var views: Long = 0,
    val authorUid: String,
    var totalCommentsSize: Long = 0
) : Parcelable {
    constructor() : this("", null, listOf(), -1, "", -1, -1, -1, "", -1)
}