package com.andre_max.tiktokclone.models.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A class that contains details of the video selected from the user's local storage.
 * This class is required since some of the parameters in {@link [com.andre_max.tiktokclone.models.video.RemoteVideo]}
 * are either unnecessary or impractical.
 *
 * @param filePath a url giving the path to the video file
 * @param duration a string representing the duration of the video
 * @param dateCreated a string representing when the video file was created to facilitate ordering based on data created
 */
@Parcelize
data class LocalVideo(
    var filePath: String?,
    val duration: Long?,
    val dateCreated: String?
): Parcelable