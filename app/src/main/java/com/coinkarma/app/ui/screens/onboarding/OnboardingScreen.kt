package com.coinkarma.app.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coinkarma.app.data.CoinKarmaDatabase
import com.coinkarma.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(db: CoinKarmaDatabase, onComplete: () -> Unit) {
    val ctx = LocalContext.current
    val vm: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory(ctx.applicationContext as android.app.Application, db))
    val state by vm.uiState.collectAsState()
    val ck = LocalCkPalette.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 5 })

    LaunchedEffect(state.step) {
        pagerState.animateScrollToPage(state.step)
    }

    Box(modifier = Modifier.fillMaxSize().background(ck.bg)) {
        // Background animated gradient blobs
        val infiniteTransition = rememberInfiniteTransition(label = "bg")
        val blobOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 400f,
            animationSpec = infiniteRepeatable(
                tween(10000, easing = LinearEasing),
                RepeatMode.Reverse
            ),
            label = "blob"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ck.primary.copy(alpha = 0.08f), ck.primary.copy(alpha = 0f)),
                    center = androidx.compose.ui.geometry.Offset(blobOffset, blobOffset),
                    radius = 800f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ck.secondary.copy(alpha = 0.06f), ck.secondary.copy(alpha = 0f)),
                    center = androidx.compose.ui.geometry.Offset(this.size.width - blobOffset, this.size.height - blobOffset),
                    radius = 1000f
                )
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> WelcomeStep(ck)
                1 -> AboutStep(ck)
                2 -> PermissionsStep(ck)
                3 -> ProfileStep(ck, state.profile.displayName, state.profile.avatarPath, { vm.updateDisplayName(it) }, { vm.updateAvatar(it) })
                4 -> BudgetStep(ck, state.profile.dailyBudget) { vm.updateBudget(it) }
            }
        }

        // Navigation Footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (only shown if not on page 0)
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = { scope.launch { vm.prevStep() } },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            "Back",
                            fontFamily = SpaceGrotesk,
                            color = ck.textSoft,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(60.dp)) // Maintain spacing alignment
                }

                // Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.weight(1f)
                ) {
                    repeat(5) { i ->
                        val active = pagerState.currentPage == i
                        val width by animateDpAsState(
                            targetValue = if (active) 24.dp else 8.dp,
                            label = "indicator_width"
                        )
                        Box(
                            modifier = Modifier
                                .size(width, 8.dp)
                                .clip(CircleShape)
                                .background(if (active) ck.primary else ck.borderStrong)
                        )
                    }
                }

                // Action Button
                val isLast = pagerState.currentPage == 4
                val nameIsInvalid = pagerState.currentPage == 3 && state.profile.displayName.trim().isEmpty()
                Button(
                    onClick = {
                        if (isLast) {
                            vm.completeOnboarding()
                            onComplete()
                        } else {
                            vm.nextStep()
                        }
                    },
                    enabled = !nameIsInvalid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ck.text,
                        contentColor = ck.bg,
                        disabledContainerColor = ck.surfaceStrong,
                        disabledContentColor = ck.textMuted
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp).width(if (isLast) 160.dp else 140.dp)
                ) {
                    Text(
                        if (isLast) "Get Started" else "Continue",
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    }
                }
            }
        }
    }

@Composable
private fun WelcomeStep(ck: CkPalette) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1000)) + scaleIn(tween(1000, easing = EaseOutBack))
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ck.primary, ck.secondary))),
                contentAlignment = Alignment.Center
            ) {
                Icon(CkIcons.Leaf, contentDescription = null, tint = ck.onPrimary, modifier = Modifier.size(64.dp))
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(800, 400)) + slideInVertically(tween(800, 400)) { it / 2 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "CoinKarma",
                    fontFamily = SpaceGrotesk,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = ck.text,
                    letterSpacing = (-1).sp
                )
                Text(
                    "Financial zen, automated.",
                    fontFamily = Inter,
                    fontSize = 18.sp,
                    color = ck.textSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AboutStep(ck: CkPalette) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).padding(bottom = 100.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "What is CoinKarma?",
            fontFamily = SpaceGrotesk,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = ck.text,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        AboutCard(
            ck = ck,
            icon = CkIcons.Bolt,
            title = "Aura Ring",
            desc = "A living representation of your daily budget. Green is healthy, Red is a warning.",
            delay = 0
        )
        Spacer(Modifier.height(16.dp))
        AboutCard(
            ck = ck,
            icon = CkIcons.List,
            title = "Zero-Effort Logs",
            desc = "We automatically detect UPI and bank spends from your SMS. No manual entry needed.",
            delay = 200
        )
        Spacer(Modifier.height(16.dp))
        AboutCard(
            ck = ck,
            icon = CkIcons.Trophy,
            title = "Quests & Rewards",
            desc = "Turn saving into a game. Complete challenges to earn Karma and level up.",
            delay = 400
        )
    }
}

@Composable
private fun AboutCard(ck: CkPalette, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String, delay: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600, delay)) + slideInHorizontally(tween(600, delay)) { -50 }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ck.surface)
                .border(1.dp, ck.border, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(ck.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ck.primary, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(title, fontFamily = SpaceGrotesk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ck.text)
                Text(desc, fontFamily = Inter, fontSize = 13.sp, color = ck.textMuted, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun PermissionsStep(ck: CkPalette) {
    val ctx = LocalContext.current
    val permissions = buildList {
        add(Manifest.permission.RECEIVE_SMS)
        add(Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    var hasSms by remember { mutableStateOf(permissions.subList(0, 2).all { ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED }) }
    var hasNotif by remember { 
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true)
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        hasSms = permissions.subList(0, 2).all { map[it] == true }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotif = map[Manifest.permission.POST_NOTIFICATIONS] == true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).padding(bottom = 100.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Privacy & Access",
            fontFamily = SpaceGrotesk,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = ck.text
        )
        Text(
            "CoinKarma runs entirely on your device. We never upload your personal data or SMS messages.",
            fontFamily = Inter,
            fontSize = 15.sp,
            color = ck.textSoft,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        PermissionRow(
            ck = ck,
            title = "SMS Access",
            desc = "Required to auto-detect bank transactions.",
            granted = hasSms,
            onClick = { launcher.launch(permissions.toTypedArray()) }
        )
        
        Spacer(Modifier.height(16.dp))

        PermissionRow(
            ck = ck,
            title = "Notifications",
            desc = "Get notified instantly when a spend is detected.",
            granted = hasNotif,
            onClick = { launcher.launch(permissions.toTypedArray()) }
        )
    }
}

@Composable
private fun PermissionRow(ck: CkPalette, title: String, desc: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (granted) ck.primary.copy(alpha = 0.08f) else ck.surface)
            .border(1.dp, if (granted) ck.primary.copy(alpha = 0.3f) else ck.border, RoundedCornerShape(20.dp))
            .clickable(enabled = !granted) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(if (granted) ck.primary else ck.surfaceStrong),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (granted) CkIcons.Check else CkIcons.Plus,
                contentDescription = null,
                tint = if (granted) ck.onPrimary else ck.textSoft,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontFamily = SpaceGrotesk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = ck.text)
            Text(desc, fontFamily = Inter, fontSize = 12.sp, color = ck.textMuted)
        }
        if (granted) {
            Text("Granted", fontFamily = JetBrainsMono, fontSize = 11.sp, color = ck.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileStep(
    ck: CkPalette,
    currentName: String,
    currentAvatar: String?,
    onNameUpdate: (String) -> Unit,
    onAvatarUpdate: (String) -> Unit
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    val archetypes = listOf(
        "🧘" to "Zen Monk",
        "🥷" to "Budget Ninja",
        "🧠" to "Mindful",
        "💎" to "Collector",
        "🚀" to "Growth",
        "🛡️" to "Guardian",
        "🍃" to "Minimalist",
        "💰" to "Hoarder",
        "🍕" to "Foodie",
        "🚲" to "Traveler",
    )
    val selectedAvatar = currentAvatar ?: "🧘"
    val isNameEmpty = name.trim().isEmpty()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Curate Your Identity",
            fontFamily = SpaceGrotesk,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = ck.text,
            textAlign = TextAlign.Center
        )
        Text(
            "Choose an avatar and set your name. This is how you'll see yourself in the app.",
            fontFamily = Inter,
            fontSize = 15.sp,
            color = ck.textSoft,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        // Avatar Preview
        Box(
            modifier = Modifier.size(100.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(ck.primary.copy(alpha = 0.2f), ck.secondary.copy(alpha = 0.1f))))
                .border(2.dp, ck.primary.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selectedAvatar.length <= 2) {
                Text(selectedAvatar, fontSize = 52.sp)
            } else {
                Icon(
                    imageVector = CkIcons.forCategory(selectedAvatar),
                    contentDescription = null,
                    tint = ck.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                val cleaned = it.take(24)
                name = cleaned
                onNameUpdate(cleaned)
            },
            isError = isNameEmpty,
            supportingText = {
                Text(
                    if (isNameEmpty) "Display name cannot be empty" else "${name.length}/24 characters",
                    color = if (isNameEmpty) ck.danger else ck.textMuted,
                    fontFamily = Inter,
                )
            },
            label = { Text("Display name", fontFamily = Inter) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ck.primary,
                unfocusedBorderColor = ck.border,
                focusedLabelColor = ck.primary,
                cursorColor = ck.primary,
                errorBorderColor = ck.danger,
                errorLabelColor = ck.danger,
                errorCursorColor = ck.danger,
                focusedTextColor = ck.text,
                unfocusedTextColor = ck.text,
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium, fontSize = 18.sp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "SELECT AVATAR",
            fontFamily = JetBrainsMono,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = ck.textSoft,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
        )

        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(archetypes) { (emoji, label) ->
                    val active = currentAvatar == emoji
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) ck.primary.copy(alpha = 0.15f) else ck.surface)
                            .border(1.dp, if (active) ck.primary else ck.border, RoundedCornerShape(12.dp))
                            .clickable { onAvatarUpdate(emoji) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(emoji, fontSize = 28.sp)
                        Text(label, fontFamily = Inter, fontSize = 10.sp, color = if (active) ck.text else ck.textMuted, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetStep(ck: CkPalette, currentBudget: Int, onUpdate: (Int) -> Unit) {
    var budget by remember { mutableFloatStateOf(currentBudget.toFloat()) }
    var showDialog by remember { mutableStateOf(false) }
    var tempBudget by remember { mutableStateOf(budget.toInt().toString()) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Set Daily Budget", fontFamily = SpaceGrotesk) },
            text = {
                TextField(
                    value = tempBudget,
                    onValueChange = { if (it.all { char -> char.isDigit() }) tempBudget = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Amount (\u20B9)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ck.surfaceStrong,
                        unfocusedContainerColor = ck.surfaceStrong,
                        focusedIndicatorColor = ck.primary,
                        cursorColor = ck.primary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newVal = tempBudget.toIntOrNull()
                    if (newVal != null) {
                        val clamped = newVal.coerceIn(100, 10000)
                        budget = clamped.toFloat()
                        onUpdate(clamped)
                        showDialog = false
                    }
                }) {
                    Text("OK", color = ck.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel", color = ck.textSoft)
                }
            },
            containerColor = ck.surface,
            titleContentColor = ck.text,
            textContentColor = ck.textSoft
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Your Daily Budget",
            fontFamily = SpaceGrotesk,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = ck.text,
            textAlign = TextAlign.Center
        )
        Text(
            "We\u0027ll use this to calibrate your Aura Ring and track your progress.",
            fontFamily = Inter,
            fontSize = 15.sp,
            color = ck.textSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
        )

        Text(
            "\u20B9${budget.toInt()}",
            fontFamily = SpaceGrotesk,
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = ck.primary,
            letterSpacing = (-2).sp,
            modifier = Modifier.clickable {
                tempBudget = budget.toInt().toString()
                showDialog = true
            }
        )
        
        Spacer(Modifier.height(24.dp))

        Slider(
            value = budget,
            onValueChange = { 
                budget = it
                onUpdate(it.toInt())
            },
            valueRange = 100f..10000f,
            steps = 98,
            colors = SliderDefaults.colors(
                thumbColor = ck.primary,
                activeTrackColor = ck.primary,
                inactiveTrackColor = ck.surfaceStrong
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("\u20B9100", fontFamily = JetBrainsMono, fontSize = 12.sp, color = ck.textDim)
            Text("\u20B910,000", fontFamily = JetBrainsMono, fontSize = 12.sp, color = ck.textDim)
        }
    }
}
