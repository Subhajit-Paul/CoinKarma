package com.coinkarma.app.ui.screens.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.ui.theme.ALL_PALETTES
import com.coinkarma.app.ui.theme.CkIcons
import com.coinkarma.app.ui.theme.Inter
import com.coinkarma.app.ui.theme.JetBrainsMono
import com.coinkarma.app.ui.theme.LocalCkPalette
import com.coinkarma.app.ui.theme.SpaceGrotesk

// ── Level definitions ─────────────────────────────────────────────────────────

private data class Level(val n: Int, val name: String, val min: Int, val max: Int)

private val LEVELS = listOf(
    Level(1, "Broke Padawan",      0,     999),
    Level(2, "Conscious Spender",  1000,  2999),
    Level(3, "Frugal Apprentice",  3000,  5999),
    Level(4, "Savvy Saver",        6000,  9999),
    Level(5, "Budget Ninja",       10000, 14999),
    Level(6, "Wealth Guardian",    15000, 24999),
    Level(7, "Money Monk",         25000, 99999),
)

private val ACHIEVEMENTS = listOf(
    Triple("First Blood",         "Log your first transaction",  true),
    Triple("On Fire",             "7-day streak",                true),
    Triple("Challenger",          "Complete first challenge",     true),
    Triple("Iron Will",           "30-day streak",               false),
    Triple("Penny Pincher",       "Save 50% of budget in a day", true),
    Triple("Month Master",        "Full month under budget",      false),
    Triple("Categories Covered",  "Log in all 7 categories",     false),
    Triple("Early Bird",          "Log before 9AM × 5 days",     true),
)

@Composable
fun ProfileScreen(db: CoinKarmaDatabase) {
    val ctx = LocalContext.current
    val vm: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(db, ctx))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current

    var showSmsRationale by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var budgetText by remember(state.profile.dailyBudget) { mutableStateOf(state.profile.dailyBudget.toString()) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let { vm.exportBackup(it) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingImportUri = uri
    }

    // Screen entry stagger
    var launched by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { launched = true }
    val headerAlpha by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "prof_header",
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (launched) 1f else 0f,
        animationSpec = tween(420, 130, easing = FastOutSlowInEasing),
        label = "prof_content",
    )

    val karma = state.profile.karma
    val currentLevel = LEVELS.lastOrNull { karma >= it.min } ?: LEVELS.first()
    val nextLevel = LEVELS.getOrNull(currentLevel.n) ?: currentLevel
    val xpProgress = if (nextLevel.min > currentLevel.min) {
        (karma - currentLevel.min).toFloat() / (nextLevel.min - currentLevel.min)
    } else 1f

    val smsPermissions = buildList {
        add(Manifest.permission.RECEIVE_SMS); add(Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    fun hasSms() = smsPermissions.all { ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED }

    Column(
        modifier = Modifier.fillMaxSize().background(ck.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Profile", fontFamily = SpaceGrotesk, fontSize = 26.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp, color = ck.text,
            modifier = Modifier.graphicsLayer(alpha = headerAlpha))

        // ── Hero card: avatar + level + XP bar ────────────────────────────
        val xpAnim = remember { Animatable(0f) }
        LaunchedEffect(xpProgress) {
            xpAnim.animateTo(xpProgress.coerceIn(0f, 1f), animationSpec = tween(1000, 200, easing = EaseOutCubic))
        }

        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(ck.primary.copy(alpha = 0.14f), ck.secondary.copy(alpha = 0.06f))))
                .border(1.dp, ck.primary.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                .padding(18.dp)
                .graphicsLayer(alpha = headerAlpha),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        LevelBadge(level = currentLevel.n, size = 72.dp)
                        if (state.profile.avatarPath != null) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(ck.surface)
                                    .border(1.dp, ck.border, CircleShape)
                                    .padding(if (state.profile.avatarPath!!.length <= 2) 0.dp else 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.profile.avatarPath!!.length <= 2) {
                                    Text(state.profile.avatarPath!!, fontSize = 16.sp)
                                } else {
                                    Icon(
                                        imageVector = CkIcons.forCategory(state.profile.avatarPath!!),
                                        contentDescription = null,
                                        tint = CkIcons.categoryColor(state.profile.avatarPath!!),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Level ${currentLevel.n}".uppercase(), fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textMuted, fontWeight = FontWeight.Medium)
                        Text(currentLevel.name, fontFamily = SpaceGrotesk, fontSize = 22.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp, color = ck.text, modifier = Modifier.padding(top = 2.dp))
                        Text("${state.profile.displayName} · joined Feb 2026", fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${karma.formatK()} KARMA", fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textMuted)
                        Text("${nextLevel.min.formatK()}", fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.textMuted)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(ck.surfaceStrong)) {
                        Box(
                            modifier = Modifier.fillMaxWidth(xpAnim.value).height(6.dp)
                                .background(Brush.horizontalGradient(listOf(ck.primary, ck.secondary)), RoundedCornerShape(3.dp))
                        )
                    }
                    Text("${(nextLevel.min - karma).formatK()} karma to ${nextLevel.name}", fontFamily = Inter, fontSize = 11.sp, color = ck.textSoft)
                }
            }
        }

        // ── Stat pills 2×2 ────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Current streak", "${state.profile.streakDays} days", ck.accent, Modifier.weight(1f))
            StatCard("Longest streak", "21 days", ck.accent, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Karma this month", "+1,240", ck.primary, Modifier.weight(1f))
            StatCard("Last month", "+980", ck.text, Modifier.weight(1f))
        }

        // ── Aura skins ────────────────────────────────────────────────────
        Text("Aura skins", fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ck.text)
        val skins = listOf(
            Triple("Default", ck.primary,   true),
            Triple("Cyan",    ck.secondary, true),
            Triple("Purple",  ck.accent,    currentLevel.n >= 4),
            Triple("Gold",    ck.warning,   currentLevel.n >= 6),
            Triple("Legendary", ck.danger,  currentLevel.n >= 7),
        )
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            skins.forEach { (name, color, unlocked) ->
                Column(
                    modifier = Modifier.width(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ck.surface)
                        .border(1.dp, ck.border, RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                            .background(Brush.radialGradient(listOf(color, color.copy(alpha = 0.55f)))),
                    )
                    Text(name, fontFamily = Inter, fontSize = 11.sp, color = ck.text, fontWeight = FontWeight.Medium)
                    if (!unlocked) Text("Lv ${if (name == "Purple") 4 else if (name == "Gold") 6 else 7}", fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textSoft)
                }
            }
        }

        // ── Achievements ──────────────────────────────────────────────────
        Text("Achievements · ${ACHIEVEMENTS.count { it.third }} / ${ACHIEVEMENTS.size}", fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ck.text)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ACHIEVEMENTS.chunked(2).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (name, desc, unlocked) ->
                        Box(
                            modifier = Modifier.weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (unlocked) ck.accent.copy(alpha = 0.10f) else ck.surface)
                                .border(1.dp, if (unlocked) ck.accent.copy(alpha = 0.25f) else ck.border, RoundedCornerShape(12.dp))
                                .padding(10.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(if (unlocked) CkIcons.Trophy else CkIcons.Lock, contentDescription = null, tint = if (unlocked) ck.accent else ck.textMuted, modifier = Modifier.size(12.dp))
                                    Text(name, fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ck.text)
                                }
                                Text(desc, fontFamily = Inter, fontSize = 10.sp, color = ck.textMuted, lineHeight = 13.sp)
                            }
                        }
                    }
                    if (row.size == 1) Box(modifier = Modifier.weight(1f))
                }
            }
        }

        // ── Appearance ────────────────────────────────────────────────────
        Text("Appearance", fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ck.text)
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(ck.surface).border(1.dp, ck.border, RoundedCornerShape(14.dp)).padding(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("MODE", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("dark" to "Dark", "light" to "Light").forEach { (k, l) ->
                        val active = if (k == "dark") state.profile.darkMode else !state.profile.darkMode
                        Box(
                            modifier = Modifier.weight(1f).height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) ck.text else ck.surfaceStrong)
                                .border(1.dp, if (active) ck.text else ck.border, RoundedCornerShape(10.dp))
                                .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) { vm.setDarkMode(k == "dark") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(l, fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (active) ck.bg else ck.textMuted)
                        }
                    }
                }

                Text("${if (state.profile.darkMode) "DARK" else "LIGHT"} PALETTE", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.5.sp, color = ck.textSoft, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ALL_PALETTES.entries.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { (key, palette) ->
                                val activeKey = if (state.profile.darkMode) state.profile.paletteDark else state.profile.paletteLight
                                val isActive = activeKey == key
                                Row(
                                    modifier = Modifier.weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isActive) ck.primary.copy(alpha = 0.12f) else ck.surfaceStrong)
                                        .border(1.dp, if (isActive) ck.primary else ck.border, RoundedCornerShape(10.dp))
                                        .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) {
                                            if (state.profile.darkMode) vm.setPalette(dark = key)
                                            else vm.setPalette(light = key)
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row {
                                        listOf(palette.primary, palette.secondary, palette.accent).forEachIndexed { i, c ->
                                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(c).then(if (i > 0) Modifier.padding(start = 0.dp) else Modifier))
                                        }
                                    }
                                    Text(palette.name, fontFamily = Inter, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ck.text, modifier = Modifier.weight(1f))
                                    if (isActive) Icon(CkIcons.Check, contentDescription = null, tint = ck.primary, modifier = Modifier.size(12.dp))
                                }
                            }
                            if (row.size == 1) Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // ── Preferences ───────────────────────────────────────────────────
        Text("Preferences", fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ck.text)
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(ck.surface).border(1.dp, ck.border, RoundedCornerShape(14.dp)).padding(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PrefRow("Display Name", "How you appear in the app") {
                    var tempName by remember(state.profile.displayName) { mutableStateOf(state.profile.displayName) }
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it; vm.setDisplayName(it) },
                        singleLine = true,
                        modifier = Modifier.width(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ck.primary, unfocusedBorderColor = ck.border, focusedTextColor = ck.text, unfocusedTextColor = ck.text, cursorColor = ck.primary),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ck.divider))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Avatar", fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ck.text)
                    val archetypes = listOf("🧘", "🥷", "🧠", "💎", "🚀", "🛡️", "🍃", "💰", "🍕", "🚲")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        archetypes.forEach { emoji ->
                            val isActive = state.profile.avatarPath == emoji
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) ck.primary.copy(alpha = 0.2f) else ck.surfaceStrong)
                                    .border(1.dp, if (isActive) ck.primary else ck.border, CircleShape)
                                    .clickable { vm.setAvatar(emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 24.sp)
                            }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ck.divider))

                PrefRow("Daily budget", "Sets the aura ring limit") {
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { v -> budgetText = v; v.toIntOrNull()?.let { vm.setDailyBudget(it) } },
                        prefix = { Text("₹", color = ck.textMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.size(width = 96.dp, height = 52.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ck.primary, unfocusedBorderColor = ck.border, focusedTextColor = ck.text, unfocusedTextColor = ck.text, cursorColor = ck.primary),
                        shape = RoundedCornerShape(10.dp),
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ck.divider))
                PrefRow("Scan SMS", "Auto-detect bank debits from SMS") {
                    ToggleChip(checked = hasSms(), onToggle = {
                        if (!hasSms()) showSmsRationale = true
                    })
                }
            }
        }

        // ── Backup ────────────────────────────────────────────────────────
        Text("Backup", fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ck.text)
        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(ck.surface).border(1.dp, ck.border, RoundedCornerShape(14.dp)).padding(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                ActionRow(
                    icon = CkIcons.Chart,
                    label = "Export backup file",
                    sub = if (state.isBusy && state.backupStatus?.contains("Exporting") == true) "Writing JSON file..."
                          else "Save transactions, quests, and profile to a file",
                    iconTint = ck.primary,
                    iconBg = ck.primary.copy(alpha = 0.10f),
                    trailing = {
                        if (state.isBusy && state.backupStatus?.contains("Exporting") == true) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ck.primary, strokeWidth = 2.dp)
                        } else {
                            Icon(CkIcons.Arrow, contentDescription = null, tint = ck.textDim, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = {
                        if (!state.isBusy) {
                            exportLauncher.launch("coinkarma-backup-${System.currentTimeMillis()}.json")
                        }
                    },
                )

                Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ck.divider))

                ActionRow(
                    icon = CkIcons.Target,
                    label = "Import backup file",
                    sub = if (state.isBusy && state.backupStatus?.contains("Importing") == true) "Reading JSON file..."
                          else "Replace local data with a CoinKarma backup",
                    iconTint = ck.accent,
                    iconBg = ck.accent.copy(alpha = 0.10f),
                    trailing = {
                        if (state.isBusy && state.backupStatus?.contains("Importing") == true) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = ck.accent, strokeWidth = 2.dp)
                        } else {
                            Icon(CkIcons.Arrow, contentDescription = null, tint = ck.textDim, modifier = Modifier.size(16.dp))
                        }
                    },
                    onClick = { if (!state.isBusy) importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) },
                )

                // Status banner
                val backupStatus = state.backupStatus
                if (backupStatus != null && !state.isBusy) {
                    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(ck.divider))
                    Text(
                        backupStatus,
                        fontFamily = Inter,
                        fontSize = 11.sp,
                        color = if (backupStatus.contains("failed", ignoreCase = true)) ck.danger else ck.secondary,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }

        // ── Danger Zone ───────────────────────────────────────────────────
        Text("Danger Zone", fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ck.danger)
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ck.danger.copy(alpha = 0.04f))
                .border(1.dp, ck.danger.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
                .padding(14.dp),
        ) {
            ActionRow(
                icon = CkIcons.Lock,
                label = "Delete local data",
                sub = "Erase transactions, quests, and profile on this device",
                iconTint = ck.danger,
                iconBg = ck.danger.copy(alpha = 0.12f),
                trailing = { Icon(CkIcons.Arrow, contentDescription = null, tint = ck.danger.copy(alpha = 0.6f), modifier = Modifier.size(16.dp)) },
                onClick = { showDeleteConfirm = true },
            )
        }

        // ── Credits ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) { showCredits = true }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(ck.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Icon(CkIcons.User, contentDescription = null, tint = ck.primary, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Credits", fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ck.text)
                    Text("Developer · links", fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted)
                }
            }
            Icon(CkIcons.Arrow, contentDescription = null, tint = ck.textDim, modifier = Modifier.size(16.dp))
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (showSmsRationale) {
        AlertDialog(
            onDismissRequest = { showSmsRationale = false },
            containerColor = ck.sheet,
            title = { Text("Why SMS access?", fontFamily = SpaceGrotesk, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = ck.text) },
            text = { Text("CoinKarma reads bank SMS messages to automatically log your UPI and card spends. We never upload your SMS — parsing happens entirely on-device.", fontFamily = Inter, fontSize = 14.sp, color = ck.textMuted) },
            confirmButton = { TextButton(onClick = { showSmsRationale = false; permLauncher.launch(smsPermissions) }) { Text("Allow", color = ck.primary) } },
            dismissButton = { TextButton(onClick = { showSmsRationale = false }) { Text("Not now", color = ck.textMuted) } },
        )
    }

    if (pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            containerColor = ck.sheet,
            title = { Text("Import backup file?", fontFamily = SpaceGrotesk, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = ck.text) },
            text = { Text("This will replace all local CoinKarma data with the selected backup file. Export your current data first if you need a copy.", fontFamily = Inter, fontSize = 14.sp, color = ck.textMuted) },
            confirmButton = {
                TextButton(onClick = {
                    pendingImportUri?.let { vm.importBackup(it) }
                    pendingImportUri = null
                }) { Text("Import", color = ck.accent) }
            },
            dismissButton = { TextButton(onClick = { pendingImportUri = null }) { Text("Cancel", color = ck.textMuted) } },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = ck.sheet,
            title = { Text("Delete local data?", fontFamily = SpaceGrotesk, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = ck.text) },
            text = { Text("This will erase transactions, quests, and your profile from this device. Export a backup first if you need a copy.", fontFamily = Inter, fontSize = 14.sp, color = ck.textMuted) },
            confirmButton = { TextButton(onClick = { showDeleteConfirm = false; vm.deleteAllData() }) { Text("Delete local data", color = ck.danger) } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = ck.textMuted) } },
        )
    }

    if (showCredits) {
        val creditLinks = listOf(
            Triple("GitHub",   "Subhajit-Paul",                  "https://github.com/Subhajit-Paul"),
            Triple("Website",  "Subhajit-Paul.vercel.app/about", "https://Subhajit-Paul.vercel.app/about"),
            Triple("LinkedIn", "stochasticgradientdescent",       "https://linkedin.com/in/stochasticgradientdescent"),
        )
        AlertDialog(
            onDismissRequest = { showCredits = false },
            containerColor = ck.sheet,
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Made by", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.sp, color = ck.textMuted)
                    Text("Subhajit Paul", fontFamily = SpaceGrotesk, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = ck.text)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    creditLinks.forEach { (label, handle, url) ->
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ck.surfaceStrong)
                                .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null) {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            val ico = when (label) { "GitHub" -> CkIcons.Target; "Website" -> CkIcons.Chart; else -> CkIcons.User }
                            Icon(ico, contentDescription = label, tint = ck.primary, modifier = Modifier.size(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(label, fontFamily = JetBrainsMono, fontSize = 9.sp, color = ck.textMuted, letterSpacing = 1.sp)
                                Text(handle, fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ck.text)
                            }
                            Icon(CkIcons.Arrow, contentDescription = null, tint = ck.textDim, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCredits = false }) { Text("Close", color = ck.primary) } },
        )
    }
}

// ── Level badge orb ───────────────────────────────────────────────────────────

@Composable
fun LevelBadge(level: Int, size: Dp) {
    val ck = LocalCkPalette.current
    val color = when (level) {
        1, 2 -> ck.primary; 3, 4 -> ck.secondary; else -> ck.accent
    }
    Box(
        modifier = Modifier.size(size).clip(CircleShape)
            .background(Brush.radialGradient(listOf(color, color.copy(alpha = 0.55f)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$level",
            fontFamily = SpaceGrotesk,
            fontSize = (size.value * 0.38f).sp,
            fontWeight = FontWeight.SemiBold,
            color = ck.onPrimary,
        )
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    sub: String,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = { Icon(CkIcons.Arrow, contentDescription = null, tint = LocalCkPalette.current.textDim, modifier = Modifier.size(16.dp)) },
    onClick: () -> Unit,
) {
    val ck = LocalCkPalette.current
    Row(
        modifier = modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ck.text)
            Text(sub, fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted)
        }
        trailing()
    }
}

@Composable
private fun StatCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val ck = LocalCkPalette.current
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(ck.surface).border(1.dp, ck.border, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontFamily = Inter, fontSize = 11.sp, color = ck.textMuted)
            Text(value, fontFamily = JetBrainsMono, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = accent)
        }
    }
}

@Composable
private fun PrefRow(label: String, sub: String, control: @Composable () -> Unit) {
    val ck = LocalCkPalette.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ck.text)
            Text(sub, fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted)
        }
        control()
    }
}

@Composable
private fun ToggleChip(checked: Boolean, onToggle: () -> Unit) {
    val ck = LocalCkPalette.current
    Box(
        modifier = Modifier.size(width = 48.dp, height = 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) ck.primary else ck.surfaceStrong)
            .clickable(interactionSource = remember { mutableStateOf(MutableInteractionSource()).value }, indication = null, onClick = onToggle),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(modifier = Modifier.padding(3.dp).size(22.dp).clip(CircleShape).background(if (checked) ck.onPrimary else ck.textMuted))
    }
}

private fun Int.formatK(): String = if (this >= 1000) "%.1fk".format(this / 1000.0) else toString()
