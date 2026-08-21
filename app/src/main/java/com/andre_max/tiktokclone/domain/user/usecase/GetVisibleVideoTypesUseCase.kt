/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.user.usecase

import com.andre_max.tiktokclone.models.video.VideoType
import javax.inject.Inject

/**
 * Which of a profile's video collections the current viewer may see.
 *
 * The rule (decided 2026-08-09, previously "not yet specified" in docs/SPEC.md §5): private
 * videos are owner-only; the liked collection is visible to others only when the profile
 * owner's `showLikedVideos` flag is on. Public is always visible. Order is the tab order.
 */
class GetVisibleVideoTypesUseCase @Inject constructor() {

    operator fun invoke(isOwnProfile: Boolean, showLikedVideos: Boolean): List<VideoType> =
        when {
            isOwnProfile -> listOf(VideoType.PUBLIC, VideoType.LIKED, VideoType.PRIVATE)
            showLikedVideos -> listOf(VideoType.PUBLIC, VideoType.LIKED)
            else -> listOf(VideoType.PUBLIC)
        }
}
