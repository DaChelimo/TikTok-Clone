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
    var url: String,
    var description: String?,
    var tags: Map<String, String>,
    var duration: Long,
    var videoId: String,
    var dateCreated: Long,
    var likes: Long = 0,
    var views: Long = 0,
    var authorUid: String,
    var totalCommentsSize: Long = 0
) : Parcelable {
    constructor() : this("", null, mapOf(), -1, "", -1, -1, -1, "", -1)
}