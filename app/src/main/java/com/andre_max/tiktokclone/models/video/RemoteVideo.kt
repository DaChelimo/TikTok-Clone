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