/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.videos

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.repo.network.utils.FirePath
import com.andre_max.tiktokclone.repo.network.utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.UUID
import kotlinx.coroutines.tasks.await

/**
 * [com.andre_max.tiktokclone.domain.videos.VideosRepo] against Firebase Realtime Database.
 *
 * Replaces the query/like half of the legacy `DefaultVideosRepo` (which stays alive for its
 * remaining legacy callers until the upload/profile slices migrate off it — strangler-fig).
 */
class FirebaseVideosRepo(
    private val realFire: FirebaseDatabase = Firebase.database,
    private val fireAuth: FirebaseAuth = Firebase.auth,
    private val firePath: FirePath = FirePath(),
) : com.andre_max.tiktokclone.domain.videos.VideosRepo {

    override suspend fun fetchRandomVideos(): Result<List<RemoteVideo>, DataError.Network> =
        safeCall {
            realFire.getReference(firePath.getAllVideosPath()).also { it.keepSynced(true) }
                .limitToFirst(BATCH_SIZE)
                .get()
                .await()
                .getValue<Map<String, RemoteVideo>>()
                ?.values
                ?.toList() ?: listOf()
        }

    override suspend fun fetchVideo(videoId: String): Result<RemoteVideo, DataError.Network> {
        val result = safeCall {
            realFire.getReference(firePath.getAllVideosPath())
                .child(videoId)
                .get()
                .await()
                .getValue<RemoteVideo>()
        }
        return when (result) {
            is Result.Success ->
                result.data?.let { Result.Success(it) }
                    ?: Result.Error(DataError.Network.NOT_FOUND)

            is Result.Error -> result
        }
    }

    override suspend fun isVideoLiked(videoId: String): Result<Boolean, DataError.Network> =
        safeCall {
            realFire.getReference(firePath.getMyLikedVideos())
                .child(videoId)
                .get()
                .await()
                .exists()
        }

    override suspend fun likeOrUnlikeVideo(
        videoId: String,
        authorId: String,
        shouldLike: Boolean,
    ): EmptyResult<DataError.Network> = safeCall {
        realFire.getReference(firePath.getMyLikedVideos())
            .child(videoId)
            .setValue(if (shouldLike) videoId else null)
            .await()

        adjustCount(
            realFire.getReference(firePath.getAllVideosPath()).child(videoId).child("likes"),
            increase = shouldLike,
        )
        adjustCount(
            realFire.getReference(firePath.getUserInfo(authorId)).child("totalLikes"),
            increase = shouldLike,
        )
    }

    override suspend fun saveVideo(
        isPrivate: Boolean,
        videoUrl: String,
        description: String,
        tags: Map<String, String>,
        duration: Long,
    ): EmptyResult<DataError.Network> = safeCall {
        val uploaderUid = fireAuth.uid ?: throw IllegalStateException("Not signed in")
        val remoteVideo = RemoteVideo(
            url = videoUrl,
            description = description,
            tags = tags,
            duration = duration,
            videoId = UUID.randomUUID().toString(),
            dateCreated = System.currentTimeMillis(),
            likes = 0,
            views = 0,
            authorUid = uploaderUid,
        )

        if (!isPrivate) {
            realFire.getReference(firePath.getAllVideosPath())
                .child(remoteVideo.videoId)
                .setValue(remoteVideo)
                .await()
        }

        val videoType = if (isPrivate) VideoType.PRIVATE else VideoType.PUBLIC
        realFire.getReference(firePath.getUserVideos(uploaderUid, videoType))
            .child(remoteVideo.videoId)
            .setValue(remoteVideo.videoId)
            .await()

        saveTagsForVideo(tags.values, remoteVideo.videoId)
    }

    /**
     * Same tag writes as the legacy `DefaultTagRepo.saveTagsInVideo`, awaited. Lives here (not
     * a tags repo) until the discover slice migrates and gives tags their own domain surface.
     */
    private suspend fun saveTagsForVideo(tags: Collection<String>, videoId: String) {
        tags.forEach { rawTag ->
            val tagName = rawTag.replace("#", "", true)

            val tagInfoRef = realFire.getReference(firePath.getTagInfo(tagName))
            val tag = tagInfoRef.get().await().getValue<Tag>() ?: Tag(tagName, 0)
            tag.count++
            tagInfoRef.setValue(tag).await()

            realFire.getReference(firePath.getTagVideos(tagName))
                .child(videoId)
                .setValue(videoId)
                .await()
        }
    }

    /**
     * Read-increment-write, as the legacy repo did. Racy under concurrent likes; moving to
     * `runTransaction` is a deliberate follow-up, not a drive-by (docs/SPEC.md §5).
     */
    private suspend fun adjustCount(
        ref: com.google.firebase.database.DatabaseReference,
        increase: Boolean,
    ) {
        val count = ref.get().await().getValue<Long>() ?: 0
        ref.setValue(if (increase) count + 1 else count - 1).await()
    }

    companion object {
        /** Caps network use per fetch; the value the legacy feed always used. */
        private const val BATCH_SIZE = 12
    }
}
