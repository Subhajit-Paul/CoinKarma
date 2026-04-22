package com.coinkarma.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/**
 * Design tokens lifted directly from the web prototype (theme.jsx).
 * Extend by adding palette variants (cobalt/sunset/mint/plum) to [CkPalette].
 */
data class CkPalette(
    val bg: Color,
    val surface: Color,
    val surfaceStrong: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textMuted: Color,
    val textSoft: Color,
    val textDim: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val warning: Color,
    val danger: Color,
    val onPrimary: Color,
    val sheet: Color,
    val keypad: Color,
    val divider: Color,
    val surfaceOverlay: Color,
    val isDark: Boolean,
)

// Forest / dark — matches CoinKarma.html's default palette
val ForestDark = CkPalette(
    bg = Color(0xFF07070A),
    surface = Color(0xFF121216),
    surfaceStrong = Color(0xFF1A1A20),
    border = Color(0x1FFFFFFF),
    borderStrong = Color(0x33FFFFFF),
    text = Color(0xFFEAEAEA),
    textMuted = Color(0xFF8A8A92),
    textSoft = Color(0xFF5F5F68),
    textDim = Color(0xFF3F3F48),
    primary = Color(0xFF22C55E),
    secondary = Color(0xFF06B6D4),
    accent = Color(0xFFA855F7),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFEF4444),
    onPrimary = Color(0xFF07070A),
    sheet = Color(0xFF0F0F13),
    keypad = Color(0xFF15151A),
    divider = Color(0x14FFFFFF),
    surfaceOverlay = Color(0x14FFFFFF),
    isDark = true,
)

// Light fallback — port the rest of the palettes from theme.jsx as you need.
val ForestLight = CkPalette(
    bg = Color(0xFFFAFAF9),
    surface = Color(0xFFFFFFFF),
    surfaceStrong = Color(0xFFF0F0EE),
    border = Color(0x14000000),
    borderStrong = Color(0x26000000),
    text = Color(0xFF14141A),
    textMuted = Color(0xFF6E6E78),
    textSoft = Color(0xFF9A9AA4),
    textDim = Color(0xFFC7C7CF),
    primary = Color(0xFF15803D),
    secondary = Color(0xFF0891B2),
    accent = Color(0xFF7E22CE),
    warning = Color(0xFFD97706),
    danger = Color(0xFFDC2626),
    onPrimary = Color(0xFFFFFFFF),
    sheet = Color(0xFFFFFFFF),
    keypad = Color(0xFFF4F4F2),
    divider = Color(0x0F000000),
    surfaceOverlay = Color(0xCCFFFFFF),
    isDark = false,
)

val LocalCkPalette = staticCompositionLocalOf { ForestDark }

@Composable
fun CoinKarmaTheme(
    dark: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = if (dark) ForestDark else ForestLight
    val scheme = if (dark) {
        darkColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            background = palette.bg,
            surface = palette.surface,
            onBackground = palette.text,
            onSurface = palette.text,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            background = palette.bg,
            surface = palette.surface,
            onBackground = palette.text,
            onSurface = palette.text,
        )
    }
    CompositionLocalProvider(LocalCkPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = CoinKarmaTypography,
            content = content,
        )
    }
}
