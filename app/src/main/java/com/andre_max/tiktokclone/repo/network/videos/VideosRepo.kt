package com.andre_max.tiktokclone.repo.network.videos

import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.repo.network.tag.TagRepo
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeAccess
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*

class VideosRepo {
    private val realFire = Firebase.database
    private val tagRepo = TagRepo()
    private val firePath = FirePath()

    /**
     * Fetches videos from Firebase database but limits it to 12 to avoid use of too much network resources
     * @return A custom result containing a list of {@link [com.andre_max.tiktokclone.models.video.RemoteVideo]}
     */
    suspend fun fetchRandomVideos() = safeAccess {
        // TODO: Change the keepSynced in order to store the freshest data within a gap of 5 MB.
        realFire.getReference("videos").also { it.keepSynced(true) }
            .limitToFirst(12)
            .get()
            .await()
            .getValue<Map<String, RemoteVideo>>()
            ?.values
            ?.toList() ?: listOf()
    }

    suspend fun fetchVideo(videoId: String) = safeAccess {
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
    suspend fun isVideoLiked(videoId: String) = safeAccess {
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
    suspend fun likeOrUnlikeVideo(videoId: String, authorId: String, shouldLike: Boolean) {
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

    /**
     * Get's the videos of a specific user
     *
     * @param uid reference to the user
     * @param videoType type of videos to fetch whether public, private or liked
     */
    suspend fun getUserVideos(uid: String?, videoType: VideoType) = safeAccess {
        val listOfUserVideoId = realFire
            .getReference(firePath.getUserVideos(uid ?: "", videoType))
            .get()
            .await()
            .getValue<Map<String, String>>()
            ?.values
            ?.toList() ?: listOf()

        val allVideos = realFire.getReference(firePath.getAllVideosPath())
        listOfUserVideoId.map { videoId ->
            allVideos.child(videoId).get().await().getValue<RemoteVideo>()
        }
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


    suspend fun saveVideoToFireDB(
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