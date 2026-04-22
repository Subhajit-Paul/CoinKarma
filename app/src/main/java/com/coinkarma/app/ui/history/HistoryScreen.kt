package com.coinkarma.app.ui.history

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.TransactionEntity
import com.coinkarma.app.ui.home.categoryEmoji
import com.coinkarma.app.ui.theme.LocalCkPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(db: CoinKarmaDatabase) {
    val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current

    Column(modifier = Modifier.fillMaxSize().background(ck.bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("History", color = ck.text, style = MaterialTheme.typography.headlineMedium)
            Text(
                "₹${state.totalSpent.toInt()} total",
                color = ck.textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (state.grouped.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions yet", color = ck.textMuted, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                state.grouped.forEach { (day, txs) ->
                    item(key = day) {
                        Text(
                            text = day,
                            color = ck.textMuted,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                        HorizontalDivider(color = ck.divider, thickness = 0.5.dp)
                    }
                    items(txs, key = { it.id }) { tx ->
                        HistoryTxRow(tx, onDelete = { vm.delete(tx) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTxRow(tx: TransactionEntity, onDelete: () -> Unit) {
    val ck = LocalCkPalette.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(categoryEmoji(tx.category), style = MaterialTheme.typography.titleMedium)
            Column {
                Text(
                    tx.merchant ?: tx.category.replaceFirstChar { it.uppercase() },
                    color = ck.text,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        formatTime(tx.timestamp),
                        color = ck.textMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (tx.source == "sms") {
                        Text(
                            "SMS",
                            color = ck.secondary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
        Text(
            "₹${tx.amount.toInt()}",
            color = ck.danger,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ck.textDim)
        }
    }
}

private fun formatTime(ts: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
