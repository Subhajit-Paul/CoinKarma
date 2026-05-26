package com.coinkarma.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.coinkarma.app.R

val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_regular,  FontWeight.Normal),
    Font(R.font.space_grotesk_medium,   FontWeight.Medium),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium,  FontWeight.Medium),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium,  FontWeight.Medium),
)

val CoinKarmaTypography = Typography(
    // Space Grotesk — display/headings
    displayLarge  = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 40.sp, letterSpacing = (-1.5).sp),
    displayMedium = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 32.sp, letterSpacing = (-1).sp),
    headlineLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 26.sp, letterSpacing = (-0.8).sp),
    headlineMedium= TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 22.sp, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 18.sp, letterSpacing = (-0.4).sp),
    titleLarge    = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 18.sp, letterSpacing = (-0.3).sp),
    titleMedium   = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 15.sp),
    // Inter — body/UI
    bodyLarge     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodyMedium    = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    bodySmall     = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    // JetBrains Mono — numbers/labels
    labelLarge    = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 1.sp),
    labelMedium   = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 1.5.sp),
    labelSmall    = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Normal, fontSize = 10.sp, letterSpacing = 1.4.sp),
)
