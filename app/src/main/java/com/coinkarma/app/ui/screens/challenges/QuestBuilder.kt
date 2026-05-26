package com.coinkarma.app.ui.screens.challenges

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinkarma.app.ui.theme.CkIcons
import com.coinkarma.app.ui.theme.Inter
import com.coinkarma.app.ui.theme.JetBrainsMono
import com.coinkarma.app.ui.theme.LocalCkPalette
import com.coinkarma.app.ui.theme.SpaceGrotesk

object QuestBuilder {

    private data class Preset(val icon: String, val title: String, val desc: String, val cap: Int, val days: Int)

    private val PRESETS = listOf(
        Preset("🚬", "No Cigarette Week",  "Zero spends on cigarettes for 7 days",     0,   7),
        Preset("🍦", "Ice Cream Cap",       "Under ₹200 on ice cream this weekend",     200, 2),
        Preset("🛵", "Walk More",           "Under ₹100 on rides this week",            100, 7),
        Preset("☕", "Brew at Home",        "No coffee-shop spends · 5 days",            0,   5),
        Preset("🛍", "Shopping Freeze",     "Zero shopping for 14 days",                 0,  14),
    )

    private val EMOJIS = listOf(
        "🎯", "🚀", "💎", "🔥", "🌈", "🍕", "🍔", "🍺", "☕️", "🎮", "🎸", "📚", "🧘", "🚲", "✈️", "🏖", "🏡", "🐶", "🐱", "🌱", "💰", "💸", "💳", "🛒", "🛍", "🎁"
    )

    private val DURATIONS = listOf(
        2 to "Weekend", 3 to "3d", 7 to "1w", 14 to "2w", 30 to "1m",
    )

    private fun xpFor(days: Int, cap: Int): Int = (100 + days * 15 + if (cap == 0) 60 else 0)
    private fun rarityFor(xp: Int): String = when {
        xp >= 500 -> "legendary"; xp >= 300 -> "epic"; xp >= 150 -> "rare"; else -> "common"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CustomQuestBuilderSheet(
        onDismiss: () -> Unit,
        onCreate: (title: String, icon: String, cap: Int, days: Int, xp: Int, rarity: String) -> Unit,
    ) {
        val ck = LocalCkPalette.current
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var step by remember { mutableStateOf("pick") } // pick | tune
        var title by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("🎯") }
        var cap by remember { mutableFloatStateOf(0f) }
        var days by remember { mutableIntStateOf(7) }
        var customColor by remember { mutableStateOf<Color?>(null) }
        var showIconPicker by remember { mutableStateOf(false) }
        var showCustomDuration by remember { mutableStateOf(false) }

        val xp = xpFor(days, cap.toInt())
        val rarity = rarityFor(xp)

        @Composable
        fun rc(): Color = customColor ?: when (rarity) {
            "common"    -> ck.textMuted
            "rare"      -> ck.secondary
            "epic"      -> ck.accent
            "legendary" -> ck.warning
            else        -> ck.text
        }

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = ck.sheet,
            dragHandle = {
                Box(modifier = Modifier.padding(top = 14.dp, bottom = 8.dp).size(width = 36.dp, height = 4.dp).background(ck.borderStrong, RoundedCornerShape(50)))
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (step == "pick") {
                    Text("Forge your own quest", fontFamily = SpaceGrotesk, fontSize = 22.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp, color = ck.text)
                    Text("Pick a template or start from scratch. You earn XP when you finish.", fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PRESETS.forEach { preset ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ck.surface)
                                    .border(1.dp, ck.border, RoundedCornerShape(14.dp))
                                    .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) {
                                        title = preset.title; icon = preset.icon; cap = preset.cap.toFloat(); days = preset.days; step = "tune"
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(ck.surfaceStrong), contentAlignment = Alignment.Center) {
                                    Text(preset.icon, fontSize = 22.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.title, fontFamily = SpaceGrotesk, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ck.text)
                                    Text(preset.desc, fontFamily = Inter, fontSize = 11.sp, color = ck.textMuted)
                                }
                                Icon(CkIcons.Arrow, contentDescription = null, tint = ck.textSoft, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, ck.border, RoundedCornerShape(14.dp))
                            .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) {
                                title = ""; icon = "🎯"; cap = 0f; days = 7; step = "tune"
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+ Start from scratch", fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ck.textMuted)
                    }
                } else {
                    // Back + title
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(ck.surface).border(1.dp, ck.border, RoundedCornerShape(10.dp))
                                .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) { step = "pick" },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(CkIcons.Back, contentDescription = null, tint = ck.textMuted, modifier = Modifier.size(14.dp))
                        }
                        Text("Shape your quest", fontFamily = SpaceGrotesk, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = ck.text)
                    }

                    // Live preview card
                    val rc = rc()
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(rc.copy(alpha = 0.20f), rc.copy(alpha = 0.05f))))
                            .border(1.dp, rc.copy(alpha = 0.40f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                                    .background(rc.copy(alpha = 0.22f))
                                    .clickable { showIconPicker = !showIconPicker },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(icon, fontSize = 32.sp)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("${rarity.replaceFirstChar { it.uppercase() }} · Custom".uppercase(), fontFamily = JetBrainsMono, fontSize = 9.sp, letterSpacing = 1.4.sp, color = rc, fontWeight = FontWeight.SemiBold)
                                Text(title.ifBlank { "Untitled quest" }, fontFamily = SpaceGrotesk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = ck.text)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(CkIcons.Bolt, contentDescription = null, tint = rc, modifier = Modifier.size(11.dp))
                                    Text("+$xp XP", fontFamily = JetBrainsMono, fontSize = 12.sp, color = rc, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showIconPicker, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(ck.surface, RoundedCornerShape(12.dp)).padding(8.dp)) {
                            LazyVerticalGrid(columns = GridCells.Adaptive(40.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(EMOJIS) { e ->
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                            .background(if (icon == e) ck.primary.copy(alpha = 0.2f) else Color.Transparent)
                                            .clickable { icon = e; showIconPicker = false },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(e, fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Title
                    SectionLabel("Title")
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ck.surface)
                            .border(1.dp, ck.border, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = title,
                            onValueChange = { if (it.length <= 40) title = it },
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Inter, fontSize = 14.sp, color = ck.text),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(ck.primary),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (title.isEmpty()) Text("e.g. No cigarettes this week", fontFamily = Inter, fontSize = 14.sp, color = ck.textSoft)
                                inner()
                            },
                        )
                    }

                    // Custom Color
                    SectionLabel("Theme Color")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(null, ck.primary, ck.secondary, ck.accent, ck.warning, ck.danger).forEach { color ->
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape)
                                    .background(color ?: ck.textMuted)
                                    .border(2.dp, if (customColor == color) ck.text else Color.Transparent, CircleShape)
                                    .clickable { customColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (color == null) Text("?", color = ck.bg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (customColor == color) Icon(CkIcons.Check, contentDescription = null, tint = if (color == null || color == ck.accent) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Spend cap
                    SectionLabel("Spend cap: ${if (cap == 0f) "Zero" else "₹${cap.toInt()}"}")
                    Slider(
                        value = cap, onValueChange = { cap = it },
                        valueRange = 0f..2000f, steps = 39,
                        colors = SliderDefaults.colors(thumbColor = rc(), activeTrackColor = rc()),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("₹0 (zero-spend)", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                        Text("₹2,000", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                    }

                    // Duration
                    SectionLabel("Duration: $days days")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DURATIONS.forEach { (d, l) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (days == d && !showCustomDuration) rc() else ck.surface)
                                    .border(1.dp, if (days == d && !showCustomDuration) rc() else ck.border, RoundedCornerShape(10.dp))
                                    .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) { days = d; showCustomDuration = false }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            ) {
                                Text(l, fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (days == d && !showCustomDuration) ck.bg else ck.textMuted)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (showCustomDuration) rc() else ck.surface)
                                .border(1.dp, if (showCustomDuration) rc() else ck.border, RoundedCornerShape(10.dp))
                                .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) { showCustomDuration = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Text("Custom", fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (showCustomDuration) ck.bg else ck.textMuted)
                        }
                    }
                    
                    if (showCustomDuration) {
                        Slider(
                            value = days.toFloat(), onValueChange = { days = it.toInt() },
                            valueRange = 15f..365f, steps = 350,
                            colors = SliderDefaults.colors(thumbColor = rc(), activeTrackColor = rc()),
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("15 days", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                            Text("365 days", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                        }
                    }

                    // Forge button
                    val enabled = title.trim().isNotEmpty()
                    val rc2 = rc()
                    Box(
                        modifier = Modifier.fillMaxWidth().height(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (enabled) rc2 else ck.primary.copy(alpha = 0.25f))
                            .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null, enabled = enabled) {
                                onCreate(title.trim(), icon, cap.toInt(), days, xp, rarity)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Forge quest · +$xp XP possible", fontFamily = Inter, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp, color = if (enabled) ck.bg else ck.textMuted)
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionLabel(label: String) {
        Text(label.uppercase(), fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = LocalCkPalette.current.textSoft, fontWeight = FontWeight.SemiBold)
    }
}
