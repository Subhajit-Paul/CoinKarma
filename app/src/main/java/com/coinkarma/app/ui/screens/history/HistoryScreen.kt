package com.coinkarma.app.ui.screens.history

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.transactions.TransactionEntity
import com.coinkarma.app.ui.screens.home.StatPill
import com.coinkarma.app.ui.screens.home.TxRow
import com.coinkarma.app.ui.theme.CkIcons
import com.coinkarma.app.ui.theme.Inter
import com.coinkarma.app.ui.theme.JetBrainsMono
import com.coinkarma.app.ui.theme.LocalCkPalette
import com.coinkarma.app.ui.theme.SpaceGrotesk
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(db: CoinKarmaDatabase) {
    val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current
    var view by remember { mutableStateOf("overview") }
    var filter by remember { mutableStateOf("all") }

    // Screen entry stagger
    var launched by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { launched = true }
    val titleAlpha by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "hist_title",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(420, 120, easing = FastOutSlowInEasing),
        label = "hist_content",
    )

    val periodLabel = remember { SimpleDateFormat("MMM", Locale.getDefault()).format(Date()) + " · 30d" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ck.bg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .graphicsLayer(alpha = titleAlpha),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("History", fontFamily = SpaceGrotesk, fontSize = 26.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp, color = ck.text)
            Text(periodLabel, fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textMuted)
        }

        // View toggle pill
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ck.surface)
                .border(1.dp, ck.border, RoundedCornerShape(12.dp))
                .padding(3.dp)
                .graphicsLayer(alpha = titleAlpha),
        ) {
            listOf("overview" to "Overview", "patterns" to "Patterns", "timeline" to "Timeline").forEach { (k, l) ->
                val isActive = view == k
                val segAlpha by animateFloatAsState(
                    targetValue = if (isActive) 1f else 0f,
                    animationSpec = tween(200),
                    label = "seg_$k",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(ck.surfaceOverlay.copy(alpha = ck.surfaceOverlay.alpha * segAlpha))
                        .clickable(
                            interactionSource = remember { mutableStateOf(MutableInteractionSource()).value },
                            indication = null,
                        ) { view = k }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        l,
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) ck.text else ck.textMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp)
                .graphicsLayer(alpha = contentAlpha),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (view) {
                "overview" -> OverviewTab(state)
                "patterns" -> PatternsTab(state)
                "timeline" -> TimelineTab(state.grouped, filter) { filter = it }
            }
        }
    }
}

// ── Overview tab ──────────────────────────────────────────────────────────────

@Composable
private fun OverviewTab(state: HistoryUiState) {
    val ck = LocalCkPalette.current
    val total = state.totalSpent.toInt()
    val budget30 = state.dailyBudget * 30

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(ck.primary.copy(alpha = 0.08f), ck.surface)))
            .border(1.dp, ck.border, RoundedCornerShape(18.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("TOTAL SPENT", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("₹${total.formatK()}", fontFamily = SpaceGrotesk, fontSize = 36.sp, fontWeight = FontWeight.Medium, letterSpacing = (-1.2).sp, color = ck.text)
                if (state.momChangePct != null) {
                    val up = state.momChangePct >= 0
                    Text(
                        "${if (up) "↑" else "↓"} ${kotlin.math.abs(state.momChangePct)}% MoM",
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = if (up) ck.danger else ck.primary,
                    )
                }
            }
            Text(
                "Budget: ₹${budget30.formatK()} · ${if (total > budget30) "over budget" else "₹${(budget30 - total).formatK()} left"}",
                fontFamily = Inter,
                fontSize = 11.sp,
                color = if (total > budget30) ck.danger else ck.primary,
            )
            Spacer(Modifier.height(8.dp))
            if (state.spend30d.any { it > 0 }) {
                CumulativeAreaChart(state.spend30d, state.dailyBudget.toDouble(), ck.primary, ck.textSoft)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Day 1", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                    Text("— budget pace", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                    Text("Day 30", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                }
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatPill("Daily avg", if (state.avgDaily > 0) "₹${state.avgDaily}" else "—", ck.text, Modifier.weight(1f))
        StatPill("Zero days", "${state.zeroDays}", ck.secondary, Modifier.weight(1f))
        StatPill("Over days", "${state.overDays}", ck.danger, Modifier.weight(1f))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ck.surface)
            .border(1.dp, ck.border, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("WHERE IT WENT", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold)
            if (state.catBreakdown.isEmpty()) {
                Text("No transactions yet", fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    CategoryDonut(data = state.catBreakdown, modifier = Modifier.size(130.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.catBreakdown.forEach { (cat, _, pct) ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(CkIcons.categoryColor(cat)))
                                Text(cat.replaceFirstChar { it.uppercase() }, fontFamily = Inter, fontSize = 11.sp, color = ck.text, modifier = Modifier.weight(1f))
                                Text("$pct%", fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(ck.accent.copy(alpha = 0.14f), ck.danger.copy(alpha = 0.06f))))
            .border(1.dp, ck.accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(CkIcons.Trophy, contentDescription = null, tint = ck.accent, modifier = Modifier.size(16.dp))
                Text("PERSONAL RECORDS", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.accent, fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecordCell("Longest streak", if (state.longestStreak > 0) "${state.longestStreak} days" else "—", Modifier.weight(1f))
                RecordCell("Zero-spend days", "${state.zeroSpendDaysAllTime}", Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RecordCell("Most saved / day", if (state.mostSavedDay > 0) "₹${state.mostSavedDay}" else "—", Modifier.weight(1f))
                RecordCell("Over-budget days", "${state.overDays}", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecordCell(label: String, value: String, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    Column(modifier = modifier) {
        Text(label, fontFamily = Inter, fontSize = 11.sp, color = ck.textMuted)
        Text(value, fontFamily = SpaceGrotesk, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = ck.text)
    }
}

// ── Patterns tab ──────────────────────────────────────────────────────────────

@Composable
private fun PatternsTab(state: HistoryUiState) {
    val ck = LocalCkPalette.current

    Card16 {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("30-DAY HEATMAP", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold)
                Text("vs ₹${state.dailyBudget}/day", fontFamily = JetBrainsMono, fontSize = 10.sp, color = ck.textMuted)
            }
            SpendHeatmap(state.spend30d, state.dailyBudget.toDouble())
        }
    }

    Card16 {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("BY DAY OF WEEK", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold)
            DayOfWeekChart(state.dayOfWeekTotals, state.dailyBudget)
            if (state.spikeDayName != null && state.spikeMultiple != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ck.danger.copy(alpha = 0.10f))
                        .border(1.dp, ck.danger.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⚠️", fontSize = 14.sp)
                        Text(
                            "${state.spikeDayName} spike. ",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = ck.danger,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "You spend ${"%.1f".format(state.spikeMultiple)}× more on ${state.spikeDayName}s.",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = ck.textMuted,
                        )
                    }
                }
            }
        }
    }

    Card16 {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("BY TIME OF DAY", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold)
            val colors = listOf(ck.accent, ck.primary, ck.secondary, ck.warning)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.timeOfDay.forEachIndexed { i, (label, range, pct) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ck.surfaceStrong)
                            .border(1.dp, ck.border, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(label, fontFamily = Inter, fontSize = 9.sp, color = ck.textMuted)
                        Text(range, fontFamily = JetBrainsMono, fontSize = 8.sp, color = ck.textSoft)
                        Text("$pct%", fontFamily = SpaceGrotesk, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = ck.text)
                        Box(modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(ck.border)) {
                            Box(modifier = Modifier.fillMaxWidth(pct / 100f).height(3.dp).background(colors[i], RoundedCornerShape(2.dp)))
                        }
                    }
                }
            }
        }
    }
}

// ── Timeline tab ──────────────────────────────────────────────────────────────

@Composable
private fun TimelineTab(
    grouped: List<Pair<String, List<TransactionEntity>>>,
    filter: String,
    onFilter: (String) -> Unit,
) {
    val ck = LocalCkPalette.current
    val filters = listOf("all", "food", "transport", "shopping", "entertainment", "health")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        filters.forEach { f ->
            val isActive = filter == f
            val chipAlpha by animateFloatAsState(
                targetValue = if (isActive) 1f else 0f,
                animationSpec = tween(180),
                label = "chip_$f",
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isActive) ck.text
                        else ck.surface.copy(alpha = 1f - chipAlpha * 0.3f)
                    )
                    .border(1.dp, if (isActive) ck.text else ck.border, RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { mutableStateOf(MutableInteractionSource()).value },
                        indication = null,
                    ) { onFilter(f) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    f.replaceFirstChar { it.uppercase() },
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isActive) ck.bg else ck.textMuted,
                )
            }
        }
    }

    if (grouped.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
            Text("No transactions yet", fontFamily = Inter, fontSize = 13.sp, color = ck.textMuted)
        }
        return
    }

    grouped.forEach { (day, items) ->
        val filtered = if (filter == "all") items else items.filter { it.category == filter }
        if (filtered.isEmpty()) return@forEach
        val dayTotal = filtered.sumOf { it.amount }.toInt()

        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(day.uppercase(), fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold)
                Text("₹$dayTotal", fontFamily = JetBrainsMono, fontSize = 12.sp, color = ck.textMuted)
            }
            filtered.forEachIndexed { i, tx ->
                TxRow(tx = tx, showDivider = i < filtered.size - 1)
            }
        }
    }
}

// ── Charts ────────────────────────────────────────────────────────────────────

@Composable
private fun CumulativeAreaChart(data: List<Double>, budget: Double, primary: Color, muted: Color) {
    val totalBudget = budget * data.size
    var cum = 0.0
    val cumData = data.map { cum += it; cum }
    val maxY = maxOf(cum, totalBudget) * 1.05

    // Draw-in animation
    val anim = remember { Animatable(0f) }
    LaunchedEffect(data) {
        anim.snapTo(0f)
        anim.animateTo(1f, animationSpec = tween(1100, easing = EaseOutCubic))
    }
    val progress = anim.value

    Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        val w = size.width; val h = size.height
        val n = data.size
        val drawN = (n * progress).toInt().coerceAtLeast(2).coerceAtMost(n)
        fun x(i: Int) = i.toFloat() / (n - 1).coerceAtLeast(1) * (w - 4f) + 2f
        fun y(v: Double) = (h - 2f - (v / maxY * (h - 4f))).toFloat()

        val areaPath = Path().apply {
            moveTo(x(0), y(cumData[0]))
            for (i in 1 until drawN) lineTo(x(i), y(cumData[i]))
            lineTo(x(drawN - 1), h); lineTo(x(0), h); close()
        }
        drawPath(areaPath, Brush.verticalGradient(listOf(primary.copy(alpha = 0.45f), primary.copy(alpha = 0f))))

        drawLine(
            muted.copy(alpha = 0.7f),
            Offset(x(0), y(0.0)),
            Offset(x(n - 1), y(totalBudget)),
            strokeWidth = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 8f)),
        )

        val linePath = Path().apply {
            moveTo(x(0), y(cumData[0]))
            for (i in 1 until drawN) lineTo(x(i), y(cumData[i]))
        }
        drawPath(linePath, primary, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(primary, 4.dp.toPx(), Offset(x(drawN - 1), y(cumData[drawN - 1])))
    }
}

@Composable
private fun CategoryDonut(data: List<CatBreakdown>, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    val total = data.sumOf { it.amt.toDouble() }.toFloat().coerceAtLeast(1f)

    val anim = remember { Animatable(0f) }
    LaunchedEffect(data) {
        anim.snapTo(0f)
        anim.animateTo(1f, animationSpec = tween(900, easing = EaseOutCubic))
    }

    Canvas(modifier = modifier) {
        val cx = size.width / 2; val cy = size.height / 2
        val r = minOf(cx, cy) - 12.dp.toPx()
        val sw = 18.dp.toPx()
        drawCircle(ck.surfaceStrong, r, Offset(cx, cy), style = Stroke(sw))
        var angle = -Math.PI.toFloat() / 2
        data.forEach { (cat, amt) ->
            val sweep = (amt / total) * 2 * Math.PI.toFloat() * anim.value
            val x1 = cx + r * kotlin.math.cos(angle)
            val y1 = cy + r * kotlin.math.sin(angle)
            val p = Path().apply {
                moveTo(x1, y1)
                arcTo(
                    androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r),
                    Math.toDegrees(angle.toDouble()).toFloat(),
                    Math.toDegrees(sweep.toDouble()).toFloat(),
                    false,
                )
            }
            drawPath(p, CkIcons.categoryColor(cat), style = Stroke(sw, cap = StrokeCap.Butt))
            angle += (amt / total) * 2 * Math.PI.toFloat()
        }
    }
}

@Composable
private fun SpendHeatmap(data: List<Double>, budget: Double) {
    val ck = LocalCkPalette.current
    val cols = 6

    // Staggered fade in
    val anim = remember { Animatable(0f) }
    LaunchedEffect(data) {
        anim.snapTo(0f)
        anim.animateTo(1f, animationSpec = tween(600, easing = EaseOutCubic))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.graphicsLayer(alpha = anim.value),
    ) {
        data.take(30).chunked(cols).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { v ->
                    val ratio = (v / budget).coerceIn(0.0, 1.4)
                    val color = when {
                        v == 0.0 -> ck.secondary.copy(alpha = 0.45f)
                        ratio > 1 -> ck.danger.copy(alpha = (0.4f + 0.4f * ((ratio - 1) * 2).coerceIn(0.0, 1.0)).toFloat())
                        else -> ck.primary.copy(alpha = (0.12f + 0.70f * ratio).toFloat())
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color)
                            .then(
                                if (v == 0.0) Modifier.border(1.dp, ck.secondary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                else Modifier.border(1.dp, ck.border, RoundedCornerShape(6.dp))
                            ),
                    )
                }
                repeat(cols - row.size) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("LESS", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
            listOf(0.1, 0.3, 0.55, 0.8, 1.0, 1.2).forEach { r ->
                val c = if (r > 1) ck.danger.copy(alpha = (0.4f + 0.3f * (r - 1)).toFloat()) else ck.primary.copy(alpha = (0.12f + 0.7f * r).toFloat())
                Box(modifier = Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(c))
            }
            Text("MORE", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp))
                    .background(ck.secondary.copy(alpha = 0.45f))
                    .border(1.dp, ck.secondary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
            Text("zero", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
        }
    }
}

@Composable
private fun DayOfWeekChart(data: List<Pair<String, Int>>, dailyBudget: Int) {
    if (data.isEmpty()) return
    val ck = LocalCkPalette.current
    val max = data.maxOf { it.second }.toFloat().coerceAtLeast(1f)

    // Bars animate up from zero
    val anim = remember { Animatable(0f) }
    LaunchedEffect(data) {
        anim.snapTo(0f)
        anim.animateTo(1f, animationSpec = tween(800, easing = EaseOutCubic))
    }

    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        data.forEach { (d, v) ->
            val h = (v / max * 90 * anim.value).dp
            val over = v > dailyBudget
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (v > 0) {
                    Text("₹$v", fontFamily = JetBrainsMono, fontSize = 7.sp, color = ck.textSoft, modifier = Modifier.padding(bottom = 4.dp))
                }
                if (v > 0 && h > 0.dp) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(h).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (over) Brush.verticalGradient(listOf(ck.danger, ck.danger.copy(alpha = 0.6f)))
                                else Brush.verticalGradient(listOf(ck.primary, ck.primary.copy(alpha = 0.55f)))
                            )
                    )
                }
                Text(d, fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textMuted, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun Card16(content: @Composable () -> Unit) {
    val ck = LocalCkPalette.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ck.surface)
            .border(1.dp, ck.border, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) { content() }
}

private fun Int.formatK(): String = if (this >= 1000) "%.1fk".format(this / 1000.0) else toString()
