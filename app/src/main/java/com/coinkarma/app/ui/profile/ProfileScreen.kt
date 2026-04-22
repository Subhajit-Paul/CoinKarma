package com.coinkarma.app.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.ui.theme.LocalCkPalette

private data class CreditLink(
    val label: String,
    val handle: String,
    val url: String,
    val icon: ImageVector,
)

private val credits = listOf(
    CreditLink("GitHub",   "Subhajit-Paul",                  "https://github.com/Subhajit-Paul",                         Icons.Filled.Code),
    CreditLink("Website",  "Subhajit-Paul.vercel.app/about", "https://Subhajit-Paul.vercel.app/about",                   Icons.Filled.Language),
    CreditLink("LinkedIn", "stochasticgradientdescent",      "https://linkedin.com/in/stochasticgradientdescent",         Icons.Filled.Person),
)

@Composable
fun ProfileScreen(db: CoinKarmaDatabase) {
    val vm: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory(db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current
    val ctx = LocalContext.current

    var showSmsRationale by remember { mutableStateOf(false) }
    var smsEnabled by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }
    var budgetText by remember(state.profile.dailyBudget) { mutableStateOf(state.profile.dailyBudget.toString()) }

    val smsPermissions = buildList {
        add(Manifest.permission.RECEIVE_SMS)
        add(Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        smsEnabled = results.values.all { it }
    }

    fun hasSmsPermissions() = smsPermissions.all {
        ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED
    }

    fun openUrl(url: String) {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ck.bg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Profile", color = ck.text, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Avatar + name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(ck.surfaceStrong, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    state.profile.displayName.firstOrNull()?.uppercase() ?: "A",
                    color = ck.text,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column {
                Text(state.profile.displayName, color = ck.text, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${state.profile.karma} karma · ${state.profile.streakDays}d streak",
                    color = ck.textMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader("Preferences")

        SettingRow(label = "Dark mode", sublabel = "Override system default") {
            Switch(
                checked = state.profile.darkMode,
                onCheckedChange = { vm.setDarkMode(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = ck.onPrimary, checkedTrackColor = ck.primary),
            )
        }

        HorizontalDivider(color = ck.divider, thickness = 0.5.dp)

        SettingRow(label = "Daily budget", sublabel = "Sets the aura ring limit") {
            OutlinedTextField(
                value = budgetText,
                onValueChange = { v ->
                    budgetText = v
                    v.toIntOrNull()?.let { vm.setDailyBudget(it) }
                },
                prefix = { Text("₹", color = ck.textMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.size(width = 100.dp, height = 52.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ck.primary,
                    unfocusedBorderColor = ck.border,
                    focusedTextColor = ck.text,
                    unfocusedTextColor = ck.text,
                    cursorColor = ck.primary,
                ),
                shape = RoundedCornerShape(10.dp),
            )
        }

        HorizontalDivider(color = ck.divider, thickness = 0.5.dp)

        SectionHeader("Automation")

        SettingRow(label = "Scan SMS", sublabel = "Auto-detect bank debits from SMS") {
            Switch(
                checked = smsEnabled || hasSmsPermissions(),
                onCheckedChange = { want ->
                    if (want) {
                        if (hasSmsPermissions()) smsEnabled = true else showSmsRationale = true
                    } else {
                        smsEnabled = false
                    }
                },
                colors = SwitchDefaults.colors(checkedThumbColor = ck.onPrimary, checkedTrackColor = ck.primary),
            )
        }

        HorizontalDivider(color = ck.divider, thickness = 0.5.dp)

        SectionHeader("About")

        // Credits button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showCredits = true }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ck.surfaceStrong, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = ck.primary, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Credits", color = ck.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("Developer · links", color = ck.textMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = ck.textDim, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    if (showSmsRationale) {
        AlertDialog(
            onDismissRequest = { showSmsRationale = false },
            containerColor = ck.surface,
            title = { Text("Why SMS access?", color = ck.text, style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "CoinKarma reads bank SMS messages to automatically log your UPI and card spends. " +
                    "We never upload your SMS — parsing happens entirely on-device.",
                    color = ck.textMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSmsRationale = false
                    permissionLauncher.launch(smsPermissions)
                }) { Text("Allow", color = ck.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showSmsRationale = false }) { Text("Not now", color = ck.textMuted) }
            },
        )
    }

    if (showCredits) {
        AlertDialog(
            onDismissRequest = { showCredits = false },
            containerColor = ck.surface,
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Made by", color = ck.textMuted, style = MaterialTheme.typography.labelLarge)
                    Text("Subhajit Paul", color = ck.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    credits.forEach { link ->
                        CreditRow(link = link, onClick = { openUrl(link.url) })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCredits = false }) { Text("Close", color = ck.primary) }
            },
        )
    }
}

@Composable
private fun CreditRow(link: CreditLink, onClick: () -> Unit) {
    val ck = LocalCkPalette.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = ck.surfaceStrong,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(link.icon, contentDescription = link.label, tint = ck.primary, modifier = Modifier.size(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(link.label, color = ck.textMuted, style = MaterialTheme.typography.labelSmall)
                Text(link.handle, color = ck.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Filled.OpenInNew, contentDescription = "Open", tint = ck.textDim, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val ck = LocalCkPalette.current
    Text(
        title,
        color = ck.textMuted,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingRow(
    label: String,
    sublabel: String? = null,
    control: @Composable () -> Unit,
) {
    val ck = LocalCkPalette.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = ck.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (sublabel != null) Text(sublabel, color = ck.textMuted, style = MaterialTheme.typography.bodySmall)
        }
        control()
    }
}
