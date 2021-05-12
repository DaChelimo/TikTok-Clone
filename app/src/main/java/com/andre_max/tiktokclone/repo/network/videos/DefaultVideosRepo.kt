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

import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.repo.network.tag.DefaultTagRepo
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*

class DefaultVideosRepo : VideosRepo {
    private val realFire = Firebase.database
    private val tagRepo = DefaultTagRepo()
    private val firePath = FirePath()

    /**
     * Fetches videos from Firebase database but limits it to 12 to avoid use of too much network resources
     * @return A custom result containing a list of {@link [com.andre_max.tiktokclone.models.video.RemoteVideo]}
     */
    override suspend fun fetchRandomVideos() = safeAccess {
        // TODO: Change the keepSynced in order to store the freshest data within a gap of 5 MB.
        realFire.getReference("videos").also { it.keepSynced(true) }
            .limitToFirst(12)
            .get()
            .await()
            .getValue<Map<String, RemoteVideo>>()
            ?.values
            ?.toList() ?: listOf()
    }

    override suspend fun fetchVideo(videoId: String) = safeAccess {
        realFire.getReference("videos")
            .child(videoId)
            .get()
            .await()
            .getValue<RemoteVideo>()
    }

    /**
     * This function checks if the user likes the video. One might suggest that we use a
     * ValueEventListener and return a liveData but that would be really intensive
     * in a scenario where we have 15 videos. However, that's my opinion, what do you think(Drop a PR).
     */
    override suspend fun isVideoLiked(videoId: String) = safeAccess {
        realFire
            .getReference(firePath.getMyLikedVideos())
            .child(videoId)
            .get()
            .await()
            .exists()
    }

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
    override suspend fun likeOrUnlikeVideo(videoId: String, authorId: String, shouldLike: Boolean) {
        val myLikedVideos = realFire
            .getReference(firePath.getMyLikedVideos())
            .child(videoId)

        val videoRef = realFire
            .getReference(firePath.getAllVideosPath())
            .child(videoId)
            .child("likes")

        val authorTotalLikesCountRef = realFire
            .getReference(firePath.getUserInfo(authorId))
            .child("totalLikes")


        // Adds or removes the video to my liked videos
        myLikedVideos.setValue(if (shouldLike) videoId else null)

        // Change the video's like count
        var videoLikeCount = videoRef.get().await().getValue<Int>() ?: 0
        if (shouldLike) videoLikeCount++ else videoLikeCount--
        videoRef.setValue(videoLikeCount)

        // Change the author's total likes count
        var totalLikeCount = authorTotalLikesCountRef.get().await().getValue<Int>() ?: 0
        if (shouldLike) totalLikeCount++ else totalLikeCount--
        authorTotalLikesCountRef.setValue(totalLikeCount)
    }

    private fun getRemoteVideoFromLocalVideo(
        videoUrl: String,
        descriptionText: String,
        tags: Map<String, String>,
        duration: Long?
    ) =
        RemoteVideo(
            url = videoUrl,
            description = descriptionText,
            tags = tags,
            duration = duration ?: 0,
            videoId = UUID.randomUUID().toString(),
            dateCreated = System.currentTimeMillis(),
            likes = 0,
            views = 0,
            authorUid = Firebase.auth.uid ?: ""
        )


    override suspend fun saveVideoToFireDB(
        isPrivate: Boolean,
        videoUrl: String,
        descriptionText: String,
        tags: Map<String, String>,
        duration: Long?,
        onComplete: (Boolean) -> Unit
    ) {
        try {
            val videoType = if (isPrivate) VideoType.PRIVATE else VideoType.PUBLIC
            val remoteVideo =
                getRemoteVideoFromLocalVideo(videoUrl, descriptionText, tags, duration)

            if (!isPrivate) {
                makeVideoPublic(remoteVideo)
            }
            saveVideoToMyAccount(videoType, remoteVideo)
            tagRepo.saveTagsInVideo(tags.values, remoteVideo.videoId)
            onComplete(true)
        } catch (e: Exception) {
            Timber.e(e)
            onComplete(false)
        }
    }

    // Saves the video to my profile
    private suspend fun saveVideoToMyAccount(
        videoType: VideoType,
        remoteVideo: RemoteVideo
    ) {
        // Since Firebase.auth.uid!! throws when null.
        realFire
            .getReference(firePath.getUserVideos(Firebase.auth.uid!!, videoType))
            .child(remoteVideo.videoId).setValue(remoteVideo.videoId)
            .await()
    }

    private suspend fun makeVideoPublic(
        remoteVideo: RemoteVideo
    ) {
        realFire
            .getReference(firePath.getAllVideosPath())
            .child(remoteVideo.videoId).setValue(remoteVideo)
            .await()
    }

}