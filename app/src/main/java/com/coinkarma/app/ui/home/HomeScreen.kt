package com.coinkarma.app.ui.home

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.TransactionEntity
import com.coinkarma.app.ui.theme.LocalCkPalette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(db: CoinKarmaDatabase, onLogClick: () -> Unit) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.Factory(db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current

    Box(modifier = Modifier.fillMaxSize().background(ck.bg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column {
                    Text(
                        text = "Good ${greeting()}, ${state.profile.displayName}",
                        color = ck.text,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = todayLabel(),
                        color = ck.textMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            item {
                AuraRing(
                    spent = state.todaySpent,
                    budget = state.profile.dailyBudget.toDouble(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                )
            }

            if (state.categoryBreakdown.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.categoryBreakdown.entries.take(4).forEach { (cat, amt) ->
                            StatPill(
                                label = cat.take(5).replaceFirstChar { it.uppercase() },
                                amount = amt,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Recent",
                    color = ck.text,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            if (state.recentTxs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "No transactions yet — tap + to log one",
                            color = ck.textMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(state.recentTxs, key = { it.id }) { tx ->
                    TxRow(tx)
                }
            }

            item { Box(modifier = Modifier.height(80.dp)) } // FAB clearance
        }

        FloatingActionButton(
            onClick = onLogClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = ck.primary,
            contentColor = ck.onPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Log spend")
        }
    }
}

@Composable
private fun AuraRing(spent: Double, budget: Double, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    val progress = (spent / budget.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()

    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 900, easing = EaseOutCubic),
        label = "aura_progress",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "aura_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 0.96f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    val arcColor = when {
        progress < 0.6f  -> ck.primary
        progress < 0.85f -> ck.warning
        else             -> ck.danger
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 18.dp.toPx()
            val inset = strokePx / 2 + 16.dp.toPx()
            val diameter = minOf(size.width, size.height) - inset * 2
            val tl = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            val center = Offset(size.width / 2, size.height / 2)

            // Pulse glow
            drawCircle(
                color = arcColor.copy(alpha = pulseAlpha),
                radius = diameter / 2 * pulseScale,
                center = center,
            )

            // Track
            drawArc(
                color = ck.surfaceStrong,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = tl,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Progress
            if (animProgress > 0f) {
                drawArc(
                    color = arcColor,
                    startAngle = 135f,
                    sweepAngle = 270f * animProgress,
                    useCenter = false,
                    topLeft = tl,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "₹${spent.toInt()}",
                color = ck.text,
                fontSize = 38.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-1).sp,
            )
            Text(
                text = "of ₹${budget.toInt()} today",
                color = ck.textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun StatPill(label: String, amount: Double, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = ck.surfaceStrong,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, color = ck.textMuted, style = MaterialTheme.typography.labelSmall)
            Text(
                "₹${amount.toInt()}",
                color = ck.text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun TxRow(tx: TransactionEntity) {
    val ck = LocalCkPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = RoundedCornerShape(12.dp), color = ck.surfaceStrong) {
                Text(
                    text = categoryEmoji(tx.category),
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Column {
                Text(
                    tx.merchant ?: tx.category.replaceFirstChar { it.uppercase() },
                    color = ck.text,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    formatTime(tx.timestamp),
                    color = ck.textMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Text(
            "₹${tx.amount.toInt()}",
            color = ck.danger,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

fun categoryEmoji(cat: String): String = when (cat) {
    "food"          -> "🍔"
    "transport"     -> "🚗"
    "shopping"      -> "🛒"
    "entertainment" -> "🎬"
    "health"        -> "💊"
    "utilities"     -> "💡"
    else            -> "💸"
}

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "morning"
        hour < 17 -> "afternoon"
        else      -> "evening"
    }
}

private fun todayLabel(): String =
    SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

private fun formatTime(ts: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
