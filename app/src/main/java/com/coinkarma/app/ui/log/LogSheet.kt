package com.coinkarma.app.ui.log

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinkarma.app.CoinKarmaApp
import com.coinkarma.app.data.TransactionEntity
import com.coinkarma.app.ui.theme.LocalCkPalette
import kotlinx.coroutines.launch

private val categories = listOf(
    "food" to "🍔",
    "transport" to "🚗",
    "shopping" to "🛒",
    "entertainment" to "🎬",
    "health" to "💊",
    "utilities" to "💡",
    "other" to "💸",
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogSheet(db: com.coinkarma.app.data.CoinKarmaDatabase, onDismiss: () -> Unit) {
    val ck = LocalCkPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("other") }

    val dismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ck.sheet,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(ck.border, RoundedCornerShape(50)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("Log spend", color = ck.text, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            // Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount", color = ck.textMuted) },
                prefix = { Text("₹ ", color = ck.textMuted, fontSize = 18.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ck.primary,
                    unfocusedBorderColor = ck.border,
                    focusedTextColor = ck.text,
                    unfocusedTextColor = ck.text,
                    cursorColor = ck.primary,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            // Category picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category", color = ck.textMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { (key, emoji) ->
                        val selected = key == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) ck.primary.copy(alpha = 0.15f) else ck.surfaceStrong)
                                .border(
                                    width = if (selected) 1.dp else 0.dp,
                                    color = if (selected) ck.primary else ck.border,
                                    shape = RoundedCornerShape(50),
                                )
                                .clickable { selectedCategory = key }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(emoji, fontSize = 15.sp)
                                Text(
                                    key.replaceFirstChar { it.uppercase() },
                                    color = if (selected) ck.primary else ck.text,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note / merchant (optional)", color = ck.textMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ck.primary,
                    unfocusedBorderColor = ck.border,
                    focusedTextColor = ck.text,
                    unfocusedTextColor = ck.text,
                    cursorColor = ck.primary,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            // Save button
            val amount = amountText.toDoubleOrNull()
            Button(
                onClick = {
                    if (amount != null && amount > 0) {
                        scope.launch {
                            db.transactions().insert(
                                TransactionEntity(
                                    amount = amount,
                                    category = selectedCategory,
                                    note = note.ifBlank { null },
                                    merchant = note.ifBlank { null },
                                    timestamp = System.currentTimeMillis(),
                                    source = "manual",
                                )
                            )
                            dismiss()
                        }
                    }
                },
                enabled = amount != null && amount > 0,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ck.primary,
                    contentColor = ck.onPrimary,
                    disabledContainerColor = ck.surfaceStrong,
                    disabledContentColor = ck.textMuted,
                ),
            ) {
                Text("Save spend", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
