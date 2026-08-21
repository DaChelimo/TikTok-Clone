/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.andre_max.tiktokclone.R

/**
 * TikTok Sans — the actual open-source typeface TikTok ships (SIL OFL, distributed via the
 * Google Fonts provider), not an approximation. Resolved at runtime by Play Services; if that
 * download ever fails, Compose falls back to the platform default automatically.
 */
val TikTokSans = FontFamily(
    Font(R.font.tiktok_sans_regular, FontWeight.Normal),
    Font(R.font.tiktok_sans_medium, FontWeight.Medium),
    Font(R.font.tiktok_sans_semibold, FontWeight.SemiBold),
    Font(R.font.tiktok_sans_bold, FontWeight.Bold),
)

val TikTokTypography = Typography().let { defaults ->
    Typography(
        displayLarge = defaults.displayLarge.copy(fontFamily = TikTokSans),
        displayMedium = defaults.displayMedium.copy(fontFamily = TikTokSans),
        displaySmall = defaults.displaySmall.copy(fontFamily = TikTokSans),
        headlineLarge = defaults.headlineLarge.copy(fontFamily = TikTokSans),
        headlineMedium = defaults.headlineMedium.copy(fontFamily = TikTokSans),
        headlineSmall = defaults.headlineSmall.copy(fontFamily = TikTokSans),
        titleLarge = defaults.titleLarge.copy(fontFamily = TikTokSans),
        titleMedium = defaults.titleMedium.copy(fontFamily = TikTokSans),
        titleSmall = defaults.titleSmall.copy(fontFamily = TikTokSans),
        bodyLarge = defaults.bodyLarge.copy(fontFamily = TikTokSans),
        bodyMedium = defaults.bodyMedium.copy(fontFamily = TikTokSans),
        bodySmall = defaults.bodySmall.copy(fontFamily = TikTokSans),
        labelLarge = defaults.labelLarge.copy(fontFamily = TikTokSans),
        labelMedium = defaults.labelMedium.copy(fontFamily = TikTokSans),
        labelSmall = defaults.labelSmall.copy(fontFamily = TikTokSans),
    )
}
