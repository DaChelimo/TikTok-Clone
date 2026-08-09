/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.upload.usecase

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.EmptyResult
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.domain.upload.VideoCompressor
import com.andre_max.tiktokclone.domain.upload.VideoStorageRepo
import com.andre_max.tiktokclone.domain.videos.VideosRepo
import javax.inject.Inject

/**
 * The whole posting pipeline: compress → upload to storage → persist the video record.
 *
 * Tags are the `#`-prefixed words of the description (keyed without the `#`, valued with it —
 * the shape the tag tree has always stored). Failure at any stage stops the pipeline; an
 * already-uploaded file whose record write fails is left behind, the same accepted risk the
 * legacy flow had.
 */
class PostVideoUseCase @Inject constructor(
    private val videoCompressor: VideoCompressor,
    private val videoStorageRepo: VideoStorageRepo,
    private val videosRepo: VideosRepo,
) {

    suspend operator fun invoke(
        localFilePath: String,
        duration: Long,
        description: String,
        isPrivate: Boolean,
    ): EmptyResult<DataError> {
        val compressedPath = when (val result = videoCompressor.compress(localFilePath)) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }

        val downloadUrl = when (val result = videoStorageRepo.uploadVideo(compressedPath)) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }

        return videosRepo.saveVideo(
            isPrivate = isPrivate,
            videoUrl = downloadUrl,
            description = description,
            tags = extractTags(description),
            duration = duration,
        )
    }

    private fun extractTags(description: String): Map<String, String> =
        description.split(" ")
            .filter { it.startsWith("#") }
            .associateBy { it.replace("#", "") }
}
