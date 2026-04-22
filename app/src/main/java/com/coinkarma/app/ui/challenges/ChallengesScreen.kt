package com.coinkarma.app.ui.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.CustomQuestEntity
import com.coinkarma.app.ui.theme.LocalCkPalette

private val rarityColor = mapOf(
    "common"    to Color(0xFF64748B),
    "rare"      to Color(0xFF06B6D4),
    "epic"      to Color(0xFFA855F7),
    "legendary" to Color(0xFFFBBF24),
)

@Composable
fun ChallengesScreen(db: CoinKarmaDatabase) {
    val vm: ChallengesViewModel = viewModel(factory = ChallengesViewModel.Factory(db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current

    // Seed default quests on first open
    LaunchedEffect(state.active.isEmpty() && state.completed.isEmpty()) {
        if (state.active.isEmpty() && state.completed.isEmpty()) vm.addDefaultQuests()
    }

    Box(modifier = Modifier.fillMaxSize().background(ck.bg)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Quests", color = ck.text, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (state.active.isNotEmpty()) {
                item {
                    Text(
                        "Active",
                        color = ck.textMuted,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                items(state.active, key = { it.id }) { quest ->
                    QuestTile(quest, onDelete = { vm.delete(quest.id) })
                }
            }

            if (state.completed.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Completed", color = ck.textMuted, style = MaterialTheme.typography.labelLarge)
                }
                items(state.completed, key = { it.id }) { quest ->
                    QuestTile(quest, onDelete = { vm.delete(quest.id) })
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun QuestTile(quest: CustomQuestEntity, onDelete: () -> Unit) {
    val ck = LocalCkPalette.current
    val rColor = rarityColor[quest.rarity] ?: ck.textMuted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, rColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ck.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ck.surfaceStrong, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(quest.icon, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(quest.title, color = ck.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (quest.completed) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = ck.primary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    "${quest.days}d · cap ₹${quest.cap} · ${quest.xp} XP",
                    color = ck.textMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                // Progress bar
                val progress = if (quest.days > 0) quest.progressDays.toFloat() / quest.days else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(ck.surfaceStrong, RoundedCornerShape(50)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(rColor, RoundedCornerShape(50)),
                    )
                }
            }

            // Rarity badge
            Box(
                modifier = Modifier
                    .background(rColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    quest.rarity.replaceFirstChar { it.uppercase() },
                    color = rColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
