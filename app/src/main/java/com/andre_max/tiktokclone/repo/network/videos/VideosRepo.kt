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

package com.andre_max.tiktokclone.repo.network.videos

import com.andre_max.tiktokclone.models.TheResult
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType

interface VideosRepo {
    /**
     * Fetches videos from Firebase database but limits it to 12 to avoid use of too much network resources
     * @return A custom result containing a list of {@link [com.andre_max.tiktokclone.models.video.RemoteVideo]}
     */
    suspend fun fetchRandomVideos(): TheResult<List<RemoteVideo>>

    /**
     * Fetches a specific video based on the video id.
     *
     * @param videoId a reference to the video
     */
    suspend fun fetchVideo(videoId: String): TheResult<RemoteVideo?>

    /**
     * This function checks if the user likes the video. One might suggest that we use a
     * ValueEventListener and return a liveData but that would be really intensive
     * in a scenario where we have 15 videos. However, that's my opinion, what do you think(Drop a PR).
     *
     * @param videoId a reference to the video
     */
    suspend fun isVideoLiked(videoId: String): TheResult<Boolean>

    /**
     * This function does 3 things (Uncle Bob is about to get mad since I'm breaking the Clean Code Conduct).
     * First, it adds or removes the video to my liked videos.
     * Second, it changes the video's like count
     * Third, it changes the author's total likes count
     *
     * @param videoId A reference to the video
     * @param authorId A reference to the video's author
     * @return Whether the video has been liked by the user
     */
    suspend fun likeOrUnlikeVideo(videoId: String, authorId: String, shouldLike: Boolean)

    /**
     * This function saves the video to the database. It uploads the actual video to the
     * main videos/ path then it uploads the video uid to the users/$userUid/videos path for
     * two way storage
     *
     * @param isPrivate whether to make the video private or not
     * @param videoUrl the url pointing to the video location in Firebase Storage
     * @param descriptionText the video description
     */
    suspend fun saveVideoToFireDB(
        isPrivate: Boolean,
        videoUrl: String,
        descriptionText: String,
        tags: Map<String, String>,
        duration: Long?,
        onComplete: (Boolean) -> Unit
    )
}