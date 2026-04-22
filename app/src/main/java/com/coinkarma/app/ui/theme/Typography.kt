package com.coinkarma.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Mirrors the web prototype's type ramp:
 *   Space Grotesk for display / headings
 *   Inter for body / UI
 *   JetBrains Mono for numeric labels
 *
 * TODO(claude-code): drop TTFs in res/font/ and replace [FontFamily.Default]
 *                    with FontFamily(Font(R.font.space_grotesk_medium), ...)
 */
val DisplayFont = FontFamily.Default // swap → Space Grotesk
val BodyFont = FontFamily.Default    // swap → Inter
val MonoFont = FontFamily.Monospace  // swap → JetBrains Mono

val CoinKarmaTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 40.sp, letterSpacing = (-1.5).sp),
    displayMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 32.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 26.sp, letterSpacing = (-0.8).sp),
    headlineMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 22.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodyMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    bodySmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.sp),
    labelMedium = TextStyle(fontFamily = MonoFont, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 1.5.sp),
    labelSmall = TextStyle(fontFamily = MonoFont, fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 1.4.sp),
)
