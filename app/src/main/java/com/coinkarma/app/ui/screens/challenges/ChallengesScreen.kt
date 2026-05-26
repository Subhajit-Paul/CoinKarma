package com.coinkarma.app.ui.screens.challenges

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.quests.CustomQuestEntity
import com.coinkarma.app.ui.screens.challenges.QuestBuilder.CustomQuestBuilderSheet
import com.coinkarma.app.ui.theme.CkIcons
import com.coinkarma.app.ui.theme.Inter
import com.coinkarma.app.ui.theme.JetBrainsMono
import com.coinkarma.app.ui.theme.LocalCkPalette
import com.coinkarma.app.ui.theme.SpaceGrotesk

private val RARITY_LABELS = mapOf(
    "common" to "Common", "rare" to "Rare", "epic" to "Epic", "legendary" to "Legendary"
)

@Composable
fun rarityColor(rarity: String): Color {
    val ck = LocalCkPalette.current
    return when (rarity) {
        "common"    -> ck.textMuted
        "rare"      -> ck.secondary
        "epic"      -> ck.accent
        "legendary" -> ck.warning
        else        -> ck.text
    }
}

@Composable
fun ChallengesScreen(db: CoinKarmaDatabase) {
    val vm: ChallengesViewModel = viewModel(factory = ChallengesViewModel.Factory(db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current
    var builderOpen by remember { mutableStateOf(false) }
    var questToDelete by remember { mutableStateOf<Long?>(null) }

    // Screen entry stagger
    var launched by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { launched = true }
    val titleAlpha by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "q_title",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(420, 120, easing = FastOutSlowInEasing),
        label = "q_content",
    )

    LaunchedEffect(state.active.isEmpty() && state.completed.isEmpty()) {
        if (state.active.isEmpty() && state.completed.isEmpty()) vm.addDefaultQuests()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(ck.bg),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ── Title ──────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp)
                    .graphicsLayer(alpha = titleAlpha),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("Quests", fontFamily = SpaceGrotesk, fontSize = 26.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp, color = ck.text)
                Text("${state.completed.size}/${state.active.size + state.completed.size} completed", fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textMuted)
            }
            Text("Week 17 · refreshes Monday", fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted, modifier = Modifier.padding(bottom = 4.dp))
        }

        // ── Season XP bar ──────────────────────────────────────────────────
        item {
            val xpAnim = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                xpAnim.animateTo(0.62f, animationSpec = tween(1000, 200, easing = EaseOutCubic))
            }
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ck.surface)
                    .border(1.dp, ck.border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .graphicsLayer(alpha = contentAlpha),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SEASON XP", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.sp, color = ck.textSoft)
                        Text("1,240 / 2,000", fontFamily = JetBrainsMono, fontSize = 10.sp, color = ck.accent, fontWeight = FontWeight.SemiBold)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(ck.surfaceStrong)) {
                        Box(
                            modifier = Modifier.fillMaxWidth(xpAnim.value).height(8.dp)
                                .background(Brush.horizontalGradient(listOf(ck.primary, ck.secondary, ck.accent)), RoundedCornerShape(4.dp)),
                        ) {
                            Box(
                                modifier = Modifier.align(Alignment.CenterEnd)
                                    .size(12.dp).clip(CircleShape)
                                    .background(ck.accent),
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tier 3", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                        Text("760 XP to Tier 4", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                    }
                }
            }
        }

        // ── Hero quest ─────────────────────────────────────────────────────
        item { HeroQuestCard() }

        // ── Available quests grid ──────────────────────────────────────────
        item {
            Text("AVAILABLE", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
        }
        item {
            val available = listOf(
                Triple("Category Freeze", "Zero shopping spend · 3 days", "rare"),
                Triple("Micro-Budget Day", "Under ₹250 for one day", "common"),
                Triple("No-Delivery Week", "Skip food delivery · 7 days", "epic"),
                Triple("Zero Spend Day", "Log zero spends for a day", "rare"),
            )
            // 2-column grid using a fixed height to avoid nested scroll
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                available.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (title, desc, rarity) ->
                            QuestTile(
                                title = title, desc = desc, rarity = rarity,
                                icon = "target", done = false, locked = false, failed = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // ── Custom quests ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("✦", color = ck.accent, fontSize = 12.sp)
                    Text("YOUR CUSTOM QUESTS", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.accent, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(ck.accent.copy(alpha = 0.15f))
                        .border(1.dp, ck.accent.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
                        .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) { builderOpen = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(CkIcons.Plus, contentDescription = null, tint = ck.accent, modifier = Modifier.size(12.dp))
                        Text("New", fontFamily = Inter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ck.accent)
                    }
                }
            }
        }

        // Active custom quests
        items(state.active, key = { it.id }) { quest ->
            CustomQuestTile(quest = quest, onDelete = { questToDelete = quest.id })
        }

        // Placeholder if no custom quests
        if (state.active.isEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(ck.accent.copy(alpha = 0.08f), ck.primary.copy(alpha = 0.04f))))
                        .border(1.dp, ck.accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) { builderOpen = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(ck.accent.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                        Text("✦", fontSize = 20.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Forge your first quest", fontFamily = SpaceGrotesk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ck.text)
                        Text("Cut cigarettes, cap ice-cream, freeze shopping — whatever matters to you", fontFamily = Inter, fontSize = 11.sp, color = ck.textMuted, modifier = Modifier.padding(top = 2.dp))
                    }
                    Icon(CkIcons.Arrow, contentDescription = null, tint = ck.accent, modifier = Modifier.size(16.dp))
                }
            }
        }

        // ── Legendary boss quest ───────────────────────────────────────────
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(ck.warning.copy(alpha = 0.20f), ck.danger.copy(alpha = 0.12f))))
                    .border(1.dp, ck.warning.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("◆ LEGENDARY BOSS QUEST", fontFamily = JetBrainsMono, fontSize = 9.sp, letterSpacing = 2.sp, color = ck.warning, fontWeight = FontWeight.SemiBold)
                    Text("The Month of Discipline", fontFamily = SpaceGrotesk, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp, color = ck.text)
                    Text("Finish April under your monthly budget. One shot per season.", fontFamily = Inter, fontSize = 11.sp, color = ck.textMuted)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(ck.surfaceStrong)) {
                            Box(modifier = Modifier.fillMaxWidth(0.72f).height(5.dp).background(Brush.horizontalGradient(listOf(ck.warning, ck.danger)), RoundedCornerShape(3.dp)))
                        }
                        Text("22/30d", fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.warning, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(CkIcons.Bolt, contentDescription = null, tint = ck.warning, modifier = Modifier.size(12.dp))
                        Text("+800 XP + Gold Aura Skin", fontFamily = JetBrainsMono, fontSize = 12.sp, color = ck.warning, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ── Hall of Fame ───────────────────────────────────────────────────
        item {
            Text("HALL OF FAME", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
        }
        item {
            val hall = listOf(
                Triple("First Blood", "Logged first transaction", "common") to true,
                Triple("On Fire", "7-day streak achieved", "rare") to true,
                Triple("Log Consistency", "Completed Apr 7", "common") to true,
                Triple("Save ₹300", "Expired Mar 31", "rare") to false,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                hall.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (qdata, done) ->
                            val (title, desc, rarity) = qdata
                            QuestTile(
                                title = title, desc = desc, rarity = rarity, icon = "target",
                                done = done, locked = false, failed = !done,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    if (builderOpen) {
        QuestBuilder.CustomQuestBuilderSheet(
            onDismiss = { builderOpen = false },
            onCreate = { title, icon, cap, days, xp, rarity ->
                vm.createCustomQuest(title, icon, cap, days, xp, rarity)
                builderOpen = false
            },
        )
    }

    if (questToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { questToDelete = null },
            containerColor = ck.sheet,
            title = { Text("Abandon Quest?", fontFamily = SpaceGrotesk, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = ck.text) },
            text = { Text("This quest and all its progress will be lost forever. You can always forge it again later.", fontFamily = Inter, fontSize = 14.sp, color = ck.textMuted) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    questToDelete?.let { vm.delete(it) }
                    questToDelete = null
                }) { Text("Abandon", color = ck.danger, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { questToDelete = null }) {
                    Text("Keep it", color = ck.textSoft)
                }
            }
        )
    }
}

// ── Hero quest card ───────────────────────────────────────────────────────────

@Composable
private fun HeroQuestCard() {
    val ck = LocalCkPalette.current
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(ck.secondary.copy(alpha = 0.22f), ck.primary.copy(alpha = 0.12f), ck.accent.copy(alpha = 0.16f))))
            .border(1.dp, ck.secondary.copy(alpha = 0.40f), RoundedCornerShape(20.dp))
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Rarity badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(ck.accent.copy(alpha = 0.22f))
                    .border(1.dp, ck.accent.copy(alpha = 0.40f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(ck.accent))
                    Text("Epic · Weekly Quest", fontFamily = JetBrainsMono, fontSize = 9.sp, letterSpacing = 2.sp, color = ck.accent, fontWeight = FontWeight.SemiBold)
                }
            }

            // Ring + info row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                QuestRing(progress = 3, total = 5, color = ck.secondary, modifier = Modifier.size(76.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Under-Limit Streak", fontFamily = SpaceGrotesk, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp, color = ck.text)
                    Text("Stay under ₹500 for 5 days in a row", fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(CkIcons.Bolt, contentDescription = null, tint = ck.accent, modifier = Modifier.size(12.dp))
                        Text("+200 XP", fontFamily = JetBrainsMono, fontSize = 13.sp, color = ck.accent, fontWeight = FontWeight.SemiBold)
                        Text("· 3 days left", fontFamily = Inter, fontSize = 10.sp, color = ck.textSoft)
                    }
                }
            }

            // Day markers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (1..5).forEach { i ->
                    val done = i <= 3
                    Box(
                        modifier = Modifier.weight(1f).height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (done) ck.primary.copy(alpha = 0.20f) else ck.surface)
                            .border(1.dp, if (done) ck.primary else ck.border, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) {
                            Icon(CkIcons.Check, contentDescription = null, tint = ck.primary, modifier = Modifier.size(14.dp))
                        } else {
                            Text("D$i", fontFamily = SpaceGrotesk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ck.textSoft)
                        }
                    }
                }
            }
        }
    }
}

// ── Quest ring (circular progress) ────────────────────────────────────────────

@Composable
fun QuestRing(progress: Int, total: Int, color: Color, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    val targetPct = progress.toFloat() / total.toFloat()

    val animPct = remember { Animatable(0f) }
    LaunchedEffect(targetPct) {
        animPct.snapTo(0f)
        animPct.animateTo(targetPct, animationSpec = tween(900, easing = EaseOutCubic))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f - 6.dp.toPx()
            val sw = 5.dp.toPx()
            drawCircle(ck.surfaceStrong, r, style = Stroke(sw))
            if (animPct.value > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animPct.value,
                    useCenter = false,
                    topLeft = Offset(size.width / 2 - r, size.height / 2 - r),
                    size = Size(r * 2, r * 2),
                    style = Stroke(sw, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$progress", fontFamily = SpaceGrotesk, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = LocalCkPalette.current.text, lineHeight = 18.sp)
            Text("/ $total", fontFamily = JetBrainsMono, fontSize = 9.sp, color = LocalCkPalette.current.textMuted)
        }
    }
}

// ── Quest tile ────────────────────────────────────────────────────────────────

@Composable
fun QuestTile(
    title: String,
    desc: String,
    rarity: String,
    icon: String,
    done: Boolean,
    locked: Boolean,
    failed: Boolean,
    modifier: Modifier = Modifier,
    custom: Boolean = false,
) {
    val ck = LocalCkPalette.current
    val rc = rarityColor(rarity)
    val bgColor = when {
        done   -> ck.primary.copy(alpha = 0.08f)
        locked -> ck.surface
        else   -> Color.Transparent
    }
    val borderColor = when {
        done   -> ck.primary.copy(alpha = 0.30f)
        locked -> ck.border
        else   -> rc.copy(alpha = 0.30f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (!done && !locked) Brush.verticalGradient(listOf(rc.copy(alpha = 0.10f), ck.surface))
                else Brush.verticalGradient(listOf(bgColor, bgColor))
            )
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.TopStart,
    ) {
        // Top color strip
        Box(
            modifier = Modifier.fillMaxWidth().height(3.dp)
                .background(if (done) ck.primary else rc),
        )
        Column(
            modifier = Modifier.padding(12.dp).padding(top = 3.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (done) ck.primary.copy(alpha = 0.20f) else rc.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    val ico = when { done -> CkIcons.Check; failed -> CkIcons.X; locked -> CkIcons.Lock; else -> CkIcons.forCategory(if (icon == "target") "other" else icon) }
                    val tint = when { done -> ck.primary; failed -> ck.danger; locked -> ck.textMuted; else -> rc }
                    Icon(ico, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(rc.copy(alpha = 0.12f))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                ) {
                    Text(
                        if (custom) "CUSTOM" else (RARITY_LABELS[rarity] ?: rarity).uppercase(),
                        fontFamily = JetBrainsMono,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                        color = rc,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontFamily = SpaceGrotesk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp, color = ck.text, lineHeight = 15.sp)
                Text(desc, fontFamily = Inter, fontSize = 11.sp, color = ck.textMuted, lineHeight = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(CkIcons.Bolt, contentDescription = null, tint = rc, modifier = Modifier.size(10.dp))
                val xp = mapOf("common" to 80, "rare" to 150, "epic" to 250, "legendary" to 400)[rarity] ?: 120
                Text("+$xp XP", fontFamily = JetBrainsMono, fontSize = 11.sp, color = rc, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Custom quest tile ─────────────────────────────────────────────────────────

@Composable
private fun CustomQuestTile(quest: CustomQuestEntity, onDelete: () -> Unit) {
    val ck = LocalCkPalette.current
    val rc = rarityColor(quest.rarity)
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(rc.copy(alpha = 0.12f), ck.surface)))
            .border(1.dp, rc.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(999.dp)).background(rc))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(rc.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(quest.icon, fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("CUSTOM · ${RARITY_LABELS[quest.rarity]?.uppercase()}", fontFamily = JetBrainsMono, fontSize = 8.sp, letterSpacing = 1.2.sp, color = rc, fontWeight = FontWeight.SemiBold)
                    Text(quest.title, fontFamily = SpaceGrotesk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ck.text)
                }
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape)
                        .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null, onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(CkIcons.X, contentDescription = "Delete", tint = ck.textSoft, modifier = Modifier.size(12.dp))
                }
            }
            Text("${if (quest.cap == 0) "Zero-spend" else "Cap: ₹${quest.cap}"} · ${quest.days} days · +${quest.xp} XP", fontFamily = Inter, fontSize = 11.sp, color = ck.textMuted)
            Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(ck.surfaceStrong)) {
                val progress = if (quest.days > 0) quest.progressDays.toFloat() / quest.days else 0f
                Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(5.dp).background(rc, RoundedCornerShape(3.dp)))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Day ${quest.progressDays} / ${quest.days}", fontFamily = JetBrainsMono, fontSize = 10.sp, color = ck.textSoft)
                Text("${quest.days - quest.progressDays} left", fontFamily = JetBrainsMono, fontSize = 10.sp, color = ck.textSoft)
            }
        }
    }
}
