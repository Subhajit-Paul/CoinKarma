package com.coinkarma.app.ui.screens.log

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.data.transactions.TransactionEntity
import com.coinkarma.app.ui.theme.CkIcons
import com.coinkarma.app.ui.theme.Inter
import com.coinkarma.app.ui.theme.LocalCkPalette
import com.coinkarma.app.ui.theme.SpaceGrotesk
import kotlinx.coroutines.launch

private data class Category(val key: String, val label: String)

private val CATEGORIES = listOf(
    Category("food",          "Food & Drinks"),
    Category("transport",     "Transport"),
    Category("shopping",      "Shopping"),
    Category("entertainment", "Entertainment"),
    Category("health",        "Health"),
    Category("utilities",     "Utilities"),
    Category("other",         "Other"),
)

private val KEYS = listOf("1","2","3","4","5","6","7","8","9",".","0","⌫")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSheet(db: CoinKarmaDatabase, onDismiss: () -> Unit) {
    val ck = LocalCkPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("food") }
    var note by remember { mutableStateOf("") }

    fun press(k: String) {
        when (k) {
            "⌫" -> amount = amount.dropLast(1)
            "." -> if (!amount.contains(".")) amount = "${amount.ifEmpty { "0" }}."
            else -> if (amount.length < 6) amount += k
        }
    }

    fun save() {
        val parsed = amount.toDoubleOrNull() ?: return
        if (parsed <= 0.0) return
        scope.launch {
            db.transactions().insert(
                TransactionEntity(
                    amount    = parsed,
                    category  = selectedCat,
                    note      = note.ifBlank { null },
                    merchant  = note.ifBlank { null },
                    timestamp = System.currentTimeMillis(),
                    source    = "manual",
                )
            )
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ck.sheet,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(ck.borderStrong, RoundedCornerShape(50)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ── Amount display ─────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "AMOUNT",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = ck.textSoft,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "₹",
                        fontFamily = SpaceGrotesk,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        color = ck.textDim,
                    )
                    // Animated amount: each digit change slides in from above
                    AnimatedContent(
                        targetState = if (amount.isEmpty()) "0" else amount,
                        transitionSpec = {
                            slideInVertically(tween(160)) { -it / 3 } + fadeIn(tween(120)) togetherWith
                            slideOutVertically(tween(140)) { it / 3 } + fadeOut(tween(100))
                        },
                        label = "amount_text",
                    ) { displayAmt ->
                        Text(
                            displayAmt,
                            fontFamily = SpaceGrotesk,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = (-2).sp,
                            color = ck.text,
                        )
                    }
                    BlinkingCursor(color = ck.primary)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Category pills ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CATEGORIES.forEach { cat ->
                    val isSelected = cat.key == selectedCat
                    val catColor = CkIcons.categoryColor(cat.key)

                    val bgAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        animationSpec = tween(180),
                        label = "cat_${cat.key}",
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(catColor.copy(alpha = bgAlpha))
                            .then(
                                if (!isSelected) Modifier.border(1.dp, ck.border, RoundedCornerShape(20.dp))
                                else Modifier
                            )
                            .then(
                                if (isSelected) Modifier
                                else Modifier.background(ck.surface)
                            )
                            .clickable(
                                interactionSource = remember { mutableStateOf(MutableInteractionSource()).value },
                                indication = null,
                            ) { selectedCat = cat.key }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = CkIcons.forCategory(cat.key),
                            contentDescription = cat.label,
                            tint = if (isSelected) Color(0xFF0B0B0B) else catColor,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            cat.label,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF0B0B0B) else ck.textMuted,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Note field ─────────────────────────────────────────────────
            BasicTextField(
                value = note,
                onValueChange = { if (it.length <= 40) note = it },
                textStyle = TextStyle(
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    color = ck.text,
                ),
                cursorBrush = SolidColor(ck.primary),
                singleLine = true,
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ck.surface)
                            .border(1.dp, ck.border, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        if (note.isEmpty()) {
                            Text("Add a note (optional)", fontFamily = Inter, fontSize = 14.sp, color = ck.textSoft)
                        }
                        inner()
                    }
                },
            )

            Spacer(Modifier.height(14.dp))

            // ── Custom numeric keypad ──────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                KEYS.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        row.forEach { k ->
                            KeypadKey(
                                label = k,
                                modifier = Modifier.weight(1f),
                                onClick = { press(k) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Confirm button ─────────────────────────────────────────────
            val parsed = amount.toDoubleOrNull()
            val enabled = parsed != null && parsed > 0.0

            val confirmSource = remember { MutableInteractionSource() }
            val confirmPressed by confirmSource.collectIsPressedAsState()
            val confirmScale by animateFloatAsState(
                targetValue = if (confirmPressed) 0.96f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                label = "confirm_scale",
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .graphicsLayer(scaleX = confirmScale, scaleY = confirmScale)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) ck.primary else ck.primary.copy(alpha = 0.25f))
                    .clickable(
                        interactionSource = confirmSource,
                        indication = null,
                        enabled = enabled,
                    ) { save() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Confirm · −₹${if (amount.isEmpty()) "0" else amount}",
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp,
                    color = if (enabled) ck.onPrimary else ck.textMuted,
                )
            }
        }
    }
}

// ── Keypad key with press scale feedback ─────────────────────────────────────

@Composable
private fun KeypadKey(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val ck = LocalCkPalette.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "key_scale",
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.28f else 0f,
        animationSpec = tween(80),
        label = "key_bg",
    )

    Box(
        modifier = modifier
            .height(52.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(RoundedCornerShape(12.dp))
            .background(ck.keypad)
            .background(ck.primary.copy(alpha = bgAlpha))
            .border(1.dp, ck.border, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = source,
                indication = null,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontFamily = SpaceGrotesk,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = ck.text,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Blinking cursor ───────────────────────────────────────────────────────────

@Composable
private fun BlinkingCursor(color: Color) {
    val inf = rememberInfiniteTransition(label = "cursor")
    val alpha by inf.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "blink",
    )
    Box(
        modifier = Modifier
            .padding(start = 2.dp, bottom = 8.dp)
            .size(width = 2.dp, height = 42.dp)
            .background(color.copy(alpha = alpha)),
    )
}
