package com.coinkarma.app.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.transactions.TransactionEntity
import com.coinkarma.app.ui.theme.AuraColors
import com.coinkarma.app.ui.theme.AuraState
import com.coinkarma.app.ui.theme.CkIcons
import com.coinkarma.app.ui.theme.JetBrainsMono
import com.coinkarma.app.ui.theme.LocalCkPalette
import com.coinkarma.app.ui.theme.SpaceGrotesk
import com.coinkarma.app.ui.theme.auraColors
import com.coinkarma.app.ui.theme.auraLabel
import com.coinkarma.app.ui.theme.auraStateFor
import com.coinkarma.app.ui.theme.auraSub
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(db: CoinKarmaDatabase, onLogClick: () -> Unit, onProfileClick: () -> Unit) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current
    val listState = rememberLazyListState()

    // Stagger sections in on composition
    var launched by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { launched = true }

    val headerAlpha by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "header_alpha",
    )
    val headerSlide by animateFloatAsState(
        targetValue = if (launched) 0f else (-14f),
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "header_slide",
    )
    val pillAlpha by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(400, 160, easing = FastOutSlowInEasing),
        label = "pill_alpha",
    )

    val collapseRatio by remember {
        derivedStateOf {
            val offset = listState.firstVisibleItemScrollOffset
                .toFloat() + (listState.firstVisibleItemIndex * 1000f)
            (offset / 500f).coerceIn(0f, 1f)
        }
    }

    val auraState = auraStateFor(state.todaySpent, state.profile.dailyBudget.toDouble(), state.profile.savedModeOn)
    val ac = auraColors(auraState, ck)
    val auraColor = ac.glow

    Box(modifier = Modifier.fillMaxSize().background(ck.bg)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // ── Header ──────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 4.dp)
                        .graphicsLayer(alpha = headerAlpha, translationY = headerSlide),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = todayLabel().uppercase(),
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            letterSpacing = 1.5.sp,
                            color = ck.textSoft,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "Good ${greeting()}, ${state.profile.displayName}",
                            fontFamily = SpaceGrotesk,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-0.5).sp,
                            color = ck.text,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    // Avatar initials circle with gradient or selected icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ck.primary, ck.secondary)))
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onProfileClick() },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.profile.avatarPath != null) {
                            if (state.profile.avatarPath!!.length <= 2) {
                                Text(
                                    text = state.profile.avatarPath!!,
                                    fontSize = 20.sp
                                )
                            } else {
                                Icon(
                                    imageVector = CkIcons.forCategory(state.profile.avatarPath!!),
                                    contentDescription = null,
                                    tint = ck.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Text(
                                text = state.profile.displayName.take(2).uppercase(),
                                fontFamily = SpaceGrotesk,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ck.onPrimary,
                            )
                        }
                    }
                }
            }

            // ── Aura orb (collapses on scroll) ────────────────────────────
            item {
                val auraAlpha = (1f - collapseRatio).coerceIn(0f, 1f)
                val auraHeightDp = (270f * (1f - collapseRatio * 0.95f)).coerceAtLeast(0f).dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(auraHeightDp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (auraAlpha > 0.05f) {
                        AuraOrb(
                            spent = state.todaySpent,
                            budget = state.profile.dailyBudget.toDouble(),
                            savedMode = state.profile.savedModeOn,
                            auraState = auraState,
                            ac = ac,
                            modifier = Modifier
                                .size(230.dp)
                                .align(Alignment.Center),
                        )
                    }
                }
            }

            // ── Compact spent header / full spent text ────────────────────
            item {
                val showCompact = collapseRatio > 0.4f
                if (showCompact) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PulsingDot(color = auraColor)
                            Column {
                                Text(
                                    "SPENT TODAY",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 9.sp,
                                    color = ck.textSoft,
                                    letterSpacing = 1.sp,
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("₹", fontFamily = SpaceGrotesk, fontSize = 16.sp, fontWeight = FontWeight.Normal, color = ck.textDim)
                                    AnimatedCounter(value = state.todaySpent.toInt(), color = auraColor, fontSize = 26.sp)
                                    Text(" / ${state.profile.dailyBudget}", fontFamily = SpaceGrotesk, fontSize = 16.sp, fontWeight = FontWeight.Normal, color = ck.textDim)
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("₹", fontFamily = SpaceGrotesk, fontSize = 22.sp, fontWeight = FontWeight.Normal, color = ck.textDim)
                            AnimatedCounter(value = state.todaySpent.toInt(), color = auraColor, fontSize = 40.sp, fontWeight = FontWeight.Medium)
                            Text(" / ${state.profile.dailyBudget}", fontFamily = SpaceGrotesk, fontSize = 22.sp, fontWeight = FontWeight.Normal, color = ck.textDim)
                        }
                        Text(
                            auraSub(auraState),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            fontSize = 12.sp,
                            color = ck.textMuted,
                            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                        )
                    }
                }
            }

            // ── Stat pills ────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .graphicsLayer(alpha = pillAlpha),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val remaining = (state.profile.dailyBudget - state.todaySpent).let {
                        if (it < 0) 0.0 else it
                    }
                    val over = (state.todaySpent - state.profile.dailyBudget).let {
                        if (it < 0) 0.0 else it
                    }
                    StatPill("Karma", "${state.profile.karma}", ck.primary, Modifier.weight(1f))
                    StatPill("Streak", "${state.profile.streakDays}d", ck.accent, Modifier.weight(1f))
                    StatPill(
                        label = if (over > 0) "Over" else "Left",
                        value = "₹${(if (over > 0) over else remaining).toInt()}",
                        accent = if (over > 0) ck.danger else ck.text,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Active challenge card ──────────────────────────────────────
            item {
                ActiveChallengeCard(ck = ck, modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .graphicsLayer(alpha = pillAlpha))
            }

            // ── Today header ──────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 18.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text("Today", fontFamily = SpaceGrotesk, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = ck.text)
                    Text(
                        "${state.recentTxs.size} entries",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = ck.textSoft,
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).padding(horizontal = 20.dp).background(ck.divider))
            }

            // ── Transaction list ──────────────────────────────────────────
            if (state.recentTxs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("No transactions yet — tap + to log one", fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textMuted)
                    }
                }
            } else {
                items(state.recentTxs, key = { it.id }) { tx ->
                    TxRow(tx = tx, showDivider = true, modifier = Modifier.animateItem(
                        fadeInSpec = tween(300),
                        fadeOutSpec = tween(200),
                        placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ))
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Animated counter text ─────────────────────────────────────────────────────

@Composable
fun AnimatedCounter(
    value: Int,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight = FontWeight.Medium,
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            if (targetState > initialState) {
                slideInVertically { -it / 2 } + fadeIn(tween(200)) togetherWith
                slideOutVertically { it / 2 } + fadeOut(tween(150))
            } else {
                slideInVertically { it / 2 } + fadeIn(tween(200)) togetherWith
                slideOutVertically { -it / 2 } + fadeOut(tween(150))
            }
        },
        label = "counter",
    ) { v ->
        Text(
            "$v",
            fontFamily = SpaceGrotesk,
            fontSize = fontSize,
            fontWeight = fontWeight,
            letterSpacing = (-1).sp,
            color = color,
        )
    }
}

// ── Aura Orb ─────────────────────────────────────────────────────────────────

@Composable
fun AuraOrb(
    spent: Double,
    budget: Double,
    savedMode: Boolean,
    auraState: AuraState,
    ac: AuraColors,
    modifier: Modifier = Modifier,
) {
    val ratio = (spent / budget.coerceAtLeast(1.0)).coerceIn(0.0, 1.2).toFloat()
    val ck = LocalCkPalette.current

    val animRatio = remember { Animatable(0f) }
    LaunchedEffect(ratio) {
        animRatio.animateTo(ratio, animationSpec = tween(900, easing = EaseOutCubic))
    }

    // Entry scale (orb scales in on first composition)
    val entryScale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entryScale.animateTo(
            1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        )
    }

    val inf = rememberInfiniteTransition(label = "aura")

    val speedMs = (ac.speed * 1000).toInt().coerceIn(800, 7000)
    val breatheScale by inf.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(speedMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe_scale",
    )
    val breatheAlpha by inf.animateFloat(
        initialValue = 0.30f, targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(speedMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe_alpha",
    )
    val crackleAlpha by inf.animateFloat(
        initialValue = 0f, targetValue = if (auraState == AuraState.DANGER || auraState == AuraState.OVERRUN) 0.22f else 0f,
        animationSpec = infiniteRepeatable(tween(180, easing = LinearEasing), RepeatMode.Reverse),
        label = "crackle",
    )

    Box(
        modifier = modifier.graphicsLayer(scaleX = entryScale.value, scaleY = entryScale.value),
        contentAlignment = Alignment.Center,
    ) {
        // Blurred radial glow halo
        Box(
            modifier = Modifier
                .size(230.dp)
                .blur(24.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ac.ring.copy(alpha = breatheAlpha), ac.ring.copy(alpha = 0f)),
                        radius = 350f,
                    ),
                    CircleShape,
                ),
        )

        // Arc ring
        Canvas(modifier = Modifier.size(230.dp)) {
            val strokeW = 3.dp.toPx()
            val inset = strokeW / 2 + 6.dp.toPx()
            val d = size.minDimension - inset * 2
            val tl = Offset((size.width - d) / 2, (size.height - d) / 2)
            val arcSize = Size(d, d)

            drawArc(
                color = if (ck.isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.06f),
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = tl, size = arcSize,
                style = Stroke(width = 2.dp.toPx()),
            )
            if (animRatio.value > 0f) {
                drawArc(
                    color = ac.glow,
                    startAngle = -90f,
                    sweepAngle = 360f * animRatio.value.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = tl, size = arcSize,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )
            }
        }

        // Orb body
        val orbScale = if (auraState == AuraState.OVERRUN) 0.88f else 1f - ratio * 0.06f
        Box(
            modifier = Modifier
                .size(230.dp * 0.68f * orbScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ac.core, ac.glow, ac.glow),
                        center = Offset(0.35f * 230f * 0.68f * orbScale, 0.30f * 230f * 0.68f * orbScale),
                        radius = 230f * 0.68f * orbScale,
                    ),
                ),
        ) {
            if (crackleAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = crackleAlpha), Color.White.copy(alpha = 0f)),
                                center = Offset(0.7f * 100f, 0.6f * 100f),
                                radius = 80f,
                            ),
                        ),
                )
            }
        }

        // State label badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (ck.isDark) Color(0xD9141414) else Color(0xF0FFFFFF))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = auraLabel(auraState).uppercase(),
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = ac.glow,
            )
        }
    }
}

// ── Pulsing dot ───────────────────────────────────────────────────────────────

@Composable
fun PulsingDot(color: Color, size: androidx.compose.ui.unit.Dp = 10.dp) {
    val inf = rememberInfiniteTransition(label = "dot")
    val scale by inf.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot_scale",
    )
    Box(
        modifier = Modifier
            .size(size * scale)
            .clip(CircleShape)
            .background(color),
    )
}

// ── Stat Pill ─────────────────────────────────────────────────────────────────

@Composable
fun StatPill(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ck.surface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = label.uppercase(),
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = ck.textSoft,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMono,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
                color = accent,
            )
        }
    }
}

// ── Active challenge card ────────────────────────────────────────────────────

@Composable
private fun ActiveChallengeCard(
    ck: com.coinkarma.app.ui.theme.CkPalette,
    modifier: Modifier = Modifier,
) {
    val progress = 3f / 5f
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(progress, animationSpec = tween(900, 300, easing = EaseOutCubic))
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(ck.secondary.copy(alpha = 0.18f), ck.primary.copy(alpha = 0.08f))
                    )
                )
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ck.secondary.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(CkIcons.Target, contentDescription = null, tint = ck.secondary, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(
                                "ACTIVE CHALLENGE · 3 DAYS LEFT",
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                letterSpacing = 1.5.sp,
                                color = ck.secondary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                "Under-Limit Streak",
                                fontFamily = SpaceGrotesk,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = ck.text,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                    Text("+200", fontFamily = JetBrainsMono, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ck.secondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(ck.surfaceStrong),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animProgress.value)
                                .height(4.dp)
                                .background(
                                    Brush.horizontalGradient(listOf(ck.secondary, ck.primary)),
                                    RoundedCornerShape(2.dp),
                                ),
                        )
                    }
                    Text("3 / 5", fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textMuted)
                }
            }
        }
    }
}

// ── Transaction Row ───────────────────────────────────────────────────────────

@Composable
fun TxRow(tx: TransactionEntity, showDivider: Boolean, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    val catColor = CkIcons.categoryColor(tx.category)

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ck.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = CkIcons.forCategory(tx.category),
                    contentDescription = tx.category,
                    tint = catColor,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.merchant ?: tx.category.replaceFirstChar { it.uppercase() },
                    fontFamily = SpaceGrotesk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ck.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(tx.category.replaceFirstChar { it.uppercase() }, fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textSoft)
                    Text("·", color = ck.textSoft, fontSize = 11.sp)
                    Text(formatTime(tx.timestamp), fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textSoft)
                }
            }

            Text(
                text = "−₹${tx.amount.toInt()}",
                fontFamily = JetBrainsMono,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
                color = ck.text,
            )
        }
        if (showDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ck.divider))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun greeting(): String {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when { h < 12 -> "morning"; h < 17 -> "afternoon"; else -> "evening" }
}
private fun todayLabel() = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
fun formatTime(ts: Long): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
fun categoryEmoji(cat: String) = when (cat) {
    "food" -> "🍔"; "transport" -> "🚗"; "shopping" -> "🛒"
    "entertainment" -> "🎬"; "health" -> "💊"; "utilities" -> "💡"; else -> "💸"
}
