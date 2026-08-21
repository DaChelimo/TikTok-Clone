/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.upload.usecase

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.test_utils.FakeVideoCompressor
import com.andre_max.tiktokclone.test_utils.FakeVideoStorageRepo
import com.andre_max.tiktokclone.test_utils.FakeVideosRepo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/** The posting pipeline: compress → upload → persist, stopping at the first failure. */
class PostVideoUseCaseTest {

    private val compressor = FakeVideoCompressor()
    private val storageRepo = FakeVideoStorageRepo()
    private val videosRepo = FakeVideosRepo()

    private val postVideo = PostVideoUseCase(compressor, storageRepo, videosRepo)

    @Test
    fun `posts the compressed file and persists the record with extracted tags`() = runTest {
        val result = postVideo(
            localFilePath = "/videos/raw.mp4",
            duration = 9_000,
            description = "my trip #travel #fun",
            isPrivate = false,
        )

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(storageRepo.uploadedPaths).containsExactly("/videos/raw.mp4.compressed")
        val save = videosRepo.saveCalls.single()
        assertThat(save.videoUrl).isEqualTo("https://storage.example/uploaded")
        assertThat(save.description).isEqualTo("my trip #travel #fun")
        assertThat(save.tags).containsExactly("travel", "#travel", "fun", "#fun")
        assertThat(save.duration).isEqualTo(9_000)
        assertThat(save.isPrivate).isFalse()
    }

    @Test
    fun `a description without hashtags produces no tags`() = runTest {
        postVideo("/v.mp4", 1_000, "no tags here", isPrivate = false)

        assertThat(videosRepo.saveCalls.single().tags).isEmpty()
    }

    @Test
    fun `compression failure stops the pipeline before any upload`() = runTest {
        compressor.compressError = DataError.Local.COMPRESSION_FAILED

        val result = postVideo("/v.mp4", 1_000, "d", isPrivate = false)

        assertThat(result).isEqualTo(Result.Error(DataError.Local.COMPRESSION_FAILED))
        assertThat(storageRepo.uploadedPaths).isEmpty()
        assertThat(videosRepo.saveCalls).isEmpty()
    }

    @Test
    fun `upload failure stops the pipeline before the record write`() = runTest {
        storageRepo.uploadError = DataError.Network.UPLOAD_FAILED

        val result = postVideo("/v.mp4", 1_000, "d", isPrivate = false)

        assertThat(result).isEqualTo(Result.Error(DataError.Network.UPLOAD_FAILED))
        assertThat(videosRepo.saveCalls).isEmpty()
    }

    @Test
    fun `a failed record write surfaces as the pipeline result`() = runTest {
        videosRepo.saveError = DataError.Network.SERVER

        val result = postVideo("/v.mp4", 1_000, "d", isPrivate = false)

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVER))
    }
}
