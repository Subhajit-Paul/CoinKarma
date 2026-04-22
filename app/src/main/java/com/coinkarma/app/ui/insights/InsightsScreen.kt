package com.coinkarma.app.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.ui.home.categoryEmoji
import com.coinkarma.app.ui.theme.LocalCkPalette

private val categoryColors = listOf(
    Color(0xFF22C55E), Color(0xFF06B6D4), Color(0xFFA855F7),
    Color(0xFFFBBF24), Color(0xFFEF4444), Color(0xFFF97316), Color(0xFF64748B),
)

@Composable
fun InsightsScreen(db: CoinKarmaDatabase) {
    val vm: InsightsViewModel = viewModel(factory = InsightsViewModel.Factory(db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ck.bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Insights", color = ck.text, style = MaterialTheme.typography.headlineMedium)

        // Summary stat pills
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = "Avg/day",
                value = "₹${state.avgDaily.toInt()}",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Top spend",
                value = state.topCategory?.replaceFirstChar { it.uppercase() } ?: "—",
                modifier = Modifier.weight(1f),
            )
        }

        // Weekly bar chart
        if (state.weeklyTotals.isNotEmpty()) {
            Text("This week", color = ck.text, style = MaterialTheme.typography.titleMedium)
            WeeklyBarChart(state.weeklyTotals)
        }

        // Category breakdown
        if (state.categoryTotals.isNotEmpty()) {
            Text("By category", color = ck.text, style = MaterialTheme.typography.titleMedium)
            val total = state.categoryTotals.values.sum().coerceAtLeast(1.0)
            state.categoryTotals.entries.forEachIndexed { i, (cat, amt) ->
                CategoryRow(
                    emoji = categoryEmoji(cat),
                    label = cat.replaceFirstChar { it.uppercase() },
                    amount = amt,
                    fraction = (amt / total).toFloat(),
                    color = categoryColors[i % categoryColors.size],
                )
            }
        }

        if (state.categoryTotals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Log some transactions to see insights", color = ck.textMuted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = ck.surface) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = ck.textMuted, style = MaterialTheme.typography.bodySmall)
            Text(value, color = ck.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun WeeklyBarChart(totals: List<DayTotal>) {
    val ck = LocalCkPalette.current
    val maxAmt = totals.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / (totals.size * 2)
                val gap = barWidth
                totals.forEachIndexed { i, day ->
                    val barHeight = (day.amount / maxAmt * size.height).toFloat().coerceAtLeast(4f)
                    val x = i * (barWidth + gap) + gap / 2
                    val y = size.height - barHeight
                    drawRoundRect(
                        color = ck.primary.copy(alpha = 0.8f),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6f, 6f),
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            totals.forEach { day ->
                Text(day.label, color = ck.textMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CategoryRow(emoji: String, label: String, amount: Double, fraction: Float, color: Color) {
    val ck = LocalCkPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Text(emoji, style = MaterialTheme.typography.bodyMedium)
                Text(label, color = ck.text, style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${(fraction * 100).toInt()}%",
                    color = ck.textMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "₹${amount.toInt()}",
                    color = ck.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Surface(shape = RoundedCornerShape(50), color = ck.surfaceStrong, modifier = Modifier.fillMaxWidth().height(6.dp)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(6.dp)
                        .background(color, RoundedCornerShape(50)),
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
