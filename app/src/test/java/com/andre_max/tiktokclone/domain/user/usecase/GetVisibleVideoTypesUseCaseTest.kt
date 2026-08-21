/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.user.usecase

import com.andre_max.tiktokclone.models.video.VideoType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Pins the profile visibility rule decided 2026-08-09 (docs/SPEC.md §5). */
class GetVisibleVideoTypesUseCaseTest {

    private val getVisibleVideoTypes = GetVisibleVideoTypesUseCase()

    @Test
    fun `the owner sees all three collections, private last`() {
        assertThat(getVisibleVideoTypes(isOwnProfile = true, showLikedVideos = false))
            .containsExactly(VideoType.PUBLIC, VideoType.LIKED, VideoType.PRIVATE)
            .inOrder()
    }

    @Test
    fun `visitors see liked only when the owner opted in — never private`() {
        assertThat(getVisibleVideoTypes(isOwnProfile = false, showLikedVideos = true))
            .containsExactly(VideoType.PUBLIC, VideoType.LIKED)
            .inOrder()
    }

    @Test
    fun `visitors see only public by default`() {
        assertThat(getVisibleVideoTypes(isOwnProfile = false, showLikedVideos = false))
            .containsExactly(VideoType.PUBLIC)
    }
}
