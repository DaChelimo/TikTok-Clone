package com.andre_max.tiktokclone.models.video

/**
 * This enum represents the three tabs in the [com.andre_max.tiktokclone.presentation.ui.profile.with_account.ProfileWithAccountFragment] fragment
 * allowing us to re-use one fragment instead of three
 */
enum class VideoType {
    LIKED {
        override fun toString(): String = "liked-videos"
    },
    PRIVATE {
        override fun toString(): String = "private-videos"
    },
    PUBLIC {
        override fun toString(): String = "public-videos"
    }
}