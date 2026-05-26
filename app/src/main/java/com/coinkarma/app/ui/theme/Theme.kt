package com.coinkarma.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Palette accent colours (matches web prototype PALETTES) ──────────────────

data class CkAccents(
    val name: String,
    val primary: Color,
    val secondary: Color,
    val warning: Color,
    val danger: Color,
    val overrun: Color,
    val accent: Color,
)

val PaletteForest = CkAccents(
    name = "Forest",
    primary   = Color(0xFF22C55E),
    secondary = Color(0xFF06B6D4),
    warning   = Color(0xFFF59E0B),
    danger    = Color(0xFFEF4444),
    overrun   = Color(0xFF991B1B),
    accent    = Color(0xFFFCD34D),
)
val PaletteCobalt = CkAccents(
    name = "Cobalt",
    primary   = Color(0xFF3B82F6),
    secondary = Color(0xFF8B5CF6),
    warning   = Color(0xFFF59E0B),
    danger    = Color(0xFFEC4899),
    overrun   = Color(0xFF9F1239),
    accent    = Color(0xFF38BDF8),
)
val PaletteSunset = CkAccents(
    name = "Sunset",
    primary   = Color(0xFFF97316),
    secondary = Color(0xFFEC4899),
    warning   = Color(0xFFFBBF24),
    danger    = Color(0xFFDC2626),
    overrun   = Color(0xFF7C2D12),
    accent    = Color(0xFFFB923C),
)
val PaletteMint = CkAccents(
    name = "Mint",
    primary   = Color(0xFF14B8A6),
    secondary = Color(0xFFA3E635),
    warning   = Color(0xFFEAB308),
    danger    = Color(0xFFF43F5E),
    overrun   = Color(0xFF881337),
    accent    = Color(0xFF5EEAD4),
)
val PalettePlum = CkAccents(
    name = "Plum",
    primary   = Color(0xFFA855F7),
    secondary = Color(0xFFEC4899),
    warning   = Color(0xFFF59E0B),
    danger    = Color(0xFFEF4444),
    overrun   = Color(0xFF701A75),
    accent    = Color(0xFFC084FC),
)

val ALL_PALETTES = mapOf(
    "forest" to PaletteForest,
    "cobalt" to PaletteCobalt,
    "sunset" to PaletteSunset,
    "mint"   to PaletteMint,
    "plum"   to PalettePlum,
)

// ── Surface tokens (matches web prototype SURFACES) ──────────────────────────

data class CkPalette(
    // Surface
    val bg: Color,
    val bgGlow: Color,
    val surface: Color,
    val surfaceStrong: Color,
    val surfaceOverlay: Color,
    val border: Color,
    val borderStrong: Color,
    val divider: Color,
    val sheet: Color,
    val keypad: Color,
    // Text
    val text: Color,
    val textMuted: Color,
    val textSoft: Color,
    val textDim: Color,
    val onPrimary: Color,
    // Palette accents (injected from CkAccents)
    val primary: Color,
    val secondary: Color,
    val warning: Color,
    val danger: Color,
    val overrun: Color,
    val accent: Color,
    // Meta
    val isDark: Boolean,
    val paletteName: String,
)

private fun darkSurfaces(accents: CkAccents) = CkPalette(
    bg             = Color(0xFF0B0B0C),
    bgGlow         = Color(0x0AFFFFFF),
    surface        = Color(0x09FFFFFF),
    surfaceStrong  = Color(0x0FFFFFFF),
    surfaceOverlay = Color(0xC6121214),
    border         = Color(0x14FFFFFF),
    borderStrong   = Color(0x1FFFFFFF),
    divider        = Color(0x0DFFFFFF),
    sheet          = Color(0xFF141414),
    keypad         = Color(0x0AFFFFFF),
    text           = Color(0xFFFFFFFF),
    textMuted      = Color(0x99FFFFFF),
    textSoft       = Color(0x73FFFFFF),
    textDim        = Color(0x4DFFFFFF),
    onPrimary      = Color(0xFF0B0B0B),
    primary        = accents.primary,
    secondary      = accents.secondary,
    warning        = accents.warning,
    danger         = accents.danger,
    overrun        = accents.overrun,
    accent         = accents.accent,
    isDark         = true,
    paletteName    = accents.name,
)

private fun lightSurfaces(accents: CkAccents) = CkPalette(
    bg             = Color(0xFFF6F6F3),
    bgGlow         = Color(0x05000000),
    surface        = Color(0x08000000),
    surfaceStrong  = Color(0x0D000000),
    surfaceOverlay = Color(0xE0FFFFFF),
    border         = Color(0x14000000),
    borderStrong   = Color(0x24000000),
    divider        = Color(0x0F000000),
    sheet          = Color(0xFFFFFFFF),
    keypad         = Color(0x0A000000),
    text           = Color(0xFF18181B),
    textMuted      = Color(0xA618181B),
    textSoft       = Color(0x7318181B),
    textDim        = Color(0x4D18181B),
    onPrimary      = Color(0xFFFFFFFF),
    primary        = accents.primary,
    secondary      = accents.secondary,
    warning        = accents.warning,
    danger         = accents.danger,
    overrun        = accents.overrun,
    accent         = accents.accent,
    isDark         = false,
    paletteName    = accents.name,
)

fun buildCkPalette(isDark: Boolean, paletteKey: String): CkPalette {
    val accents = ALL_PALETTES[paletteKey] ?: PaletteForest
    return if (isDark) darkSurfaces(accents) else lightSurfaces(accents)
}

val LocalCkPalette = staticCompositionLocalOf { darkSurfaces(PaletteForest) }

// ── Aura state helpers (matches web prototype auraColorsForState) ─────────────

enum class AuraState { PRISTINE, HEALTHY, WARNING, DANGER, OVERRUN, SAVED }

data class AuraColors(val core: Color, val glow: Color, val ring: Color, val speed: Float)

fun auraStateFor(spent: Double, budget: Double, savedMode: Boolean = false): AuraState {
    if (savedMode && spent <= 0) return AuraState.SAVED
    if (spent <= 0) return AuraState.PRISTINE
    val r = spent / budget
    return when {
        r < 0.6  -> AuraState.HEALTHY
        r < 0.85 -> AuraState.WARNING
        r <= 1.0 -> AuraState.DANGER
        else     -> AuraState.OVERRUN
    }
}

fun auraLabel(state: AuraState) = when (state) {
    AuraState.PRISTINE -> "Pristine"
    AuraState.HEALTHY  -> "Healthy"
    AuraState.WARNING  -> "Warning"
    AuraState.DANGER   -> "Danger"
    AuraState.OVERRUN  -> "Overrun"
    AuraState.SAVED    -> "Saved Mode"
}

fun auraSub(state: AuraState) = when (state) {
    AuraState.PRISTINE -> "A fresh day. Spend with intention."
    AuraState.HEALTHY  -> "Well under budget. Keep it up."
    AuraState.WARNING  -> "Slow down — you're past 60%."
    AuraState.DANGER   -> "Budget nearly gone. Breathe."
    AuraState.OVERRUN  -> "Over budget. Karma hit incoming."
    AuraState.SAVED    -> "Zero-spend day. Cyan streak active."
}

fun auraColors(state: AuraState, ck: CkPalette): AuraColors {
    fun lighten(c: Color, amt: Float): Color {
        val r = c.red   + (1f - c.red)   * amt
        val g = c.green + (1f - c.green) * amt
        val b = c.blue  + (1f - c.blue)  * amt
        return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
    }
    return when (state) {
        AuraState.PRISTINE -> AuraColors(lighten(ck.primary, 0.50f),   ck.primary,   ck.primary.copy(alpha = 0.40f),   6f)
        AuraState.HEALTHY  -> AuraColors(lighten(ck.primary, 0.35f),   ck.primary,   ck.primary.copy(alpha = 0.40f),   5f)
        AuraState.WARNING  -> AuraColors(lighten(ck.warning, 0.35f),   ck.warning,   ck.warning.copy(alpha = 0.40f),   3f)
        AuraState.DANGER   -> AuraColors(lighten(ck.danger,  0.35f),   ck.danger,    ck.danger.copy(alpha = 0.40f),    1.6f)
        AuraState.OVERRUN  -> AuraColors(ck.overrun,                   ck.overrun,   ck.overrun.copy(alpha = 0.40f),   4f)
        AuraState.SAVED    -> AuraColors(lighten(ck.secondary, 0.40f), ck.secondary, ck.secondary.copy(alpha = 0.40f), 5f)
    }
}

// ── Theme composable ──────────────────────────────────────────────────────────

@Composable
fun CoinKarmaTheme(
    isDark: Boolean = true,
    paletteKey: String = "forest",
    content: @Composable () -> Unit,
) {
    val ck = buildCkPalette(isDark, paletteKey)
    val scheme = if (isDark) {
        darkColorScheme(
            primary    = ck.primary,
            secondary  = ck.secondary,
            background = ck.bg,
            surface    = ck.surface,
            onBackground = ck.text,
            onSurface  = ck.text,
        )
    } else {
        lightColorScheme(
            primary    = ck.primary,
            secondary  = ck.secondary,
            background = ck.bg,
            surface    = ck.surface,
            onBackground = ck.text,
            onSurface  = ck.text,
        )
    }
    CompositionLocalProvider(LocalCkPalette provides ck) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = CoinKarmaTypography,
            content     = content,
        )
    }
}
