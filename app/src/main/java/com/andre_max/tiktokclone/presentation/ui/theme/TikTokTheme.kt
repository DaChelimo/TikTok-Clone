/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Brand tokens lifted from the reference screens in docs/designs/screens (Mobbin captures of
 * the real app). Named after their role there, not their hue, so screens read as the design.
 */
object TikTokColors {
    /** Primary CTA + "Follow" red. */
    val Red = Color(0xFFFE2C55)

    /** The logo's second accent; only used for small flourishes (record ring, splash dots). */
    val Cyan = Color(0xFF25F4EE)

    /** Near-black used for all primary text on white. */
    val TextPrimary = Color(0xFF161823)

    /** 50%-alpha ink for secondary text/labels on white. */
    val TextSecondary = Color(0x80161823)

    /** Fill for secondary buttons, search fields and chips. */
    val LightGray = Color(0xFFF1F1F2)

    /** Hairline dividers / field underlines. */
    val Divider = Color(0xFFE3E3E4)

    /** Disabled state of the red CTA (screen 28's pink "Next"). */
    val RedDisabled = Color(0xFFFBB8C5)

    /** Video surfaces (feed, camera) stay black regardless of theme. */
    val VideoBlack = Color(0xFF000000)
}

/**
 * Light-first scheme: every non-video reference screen is white with near-black ink, so the
 * app does not follow system dark mode — dark surfaces (feed, camera) opt in with explicit
 * colors instead.
 */
private val LightColors = lightColorScheme(
    primary = TikTokColors.Red,
    onPrimary = Color.White,
    secondary = TikTokColors.TextPrimary,
    onSecondary = Color.White,
    background = Color.White,
    onBackground = TikTokColors.TextPrimary,
    surface = Color.White,
    onSurface = TikTokColors.TextPrimary,
    surfaceVariant = TikTokColors.LightGray,
    onSurfaceVariant = TikTokColors.TextSecondary,
    secondaryContainer = TikTokColors.LightGray,
    onSecondaryContainer = TikTokColors.TextPrimary,
    outline = TikTokColors.Divider,
    outlineVariant = TikTokColors.Divider,
    error = Color(0xFFB3261E),
)

@Composable
fun TikTokTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = TikTokTypography,
        content = content,
    )
}
