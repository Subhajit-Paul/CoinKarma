package com.coinkarma.app.ui.atoms

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinkarma.app.ui.theme.CkIcons
import com.coinkarma.app.ui.theme.JetBrainsMono
import com.coinkarma.app.ui.theme.LocalCkPalette

data class TabItem(
    val key: String,
    val icon: ImageVector,
    val label: String,
)

val CkTabs = listOf(
    TabItem("home",       CkIcons.Home,   "Home"),
    TabItem("history",    CkIcons.List,   "History"),
    TabItem("challenges", CkIcons.Target, "Quests"),
    TabItem("profile",    CkIcons.User,   "Profile"),
)

@Composable
fun CkTabBar(
    selected: String,
    onSelect: (String) -> Unit,
    onLogTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ck = LocalCkPalette.current

    val inf = rememberInfiniteTransition(label = "fab")
    val fabPulse by inf.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fab_pulse",
    )
    val fabGlow by inf.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "fab_spin",
    )

    // FAB press feedback
    val fabSource = remember { MutableInteractionSource() }
    val fabPressed by fabSource.collectIsPressedAsState()
    val fabScale by animateFloatAsState(
        targetValue = if (fabPressed) 0.88f else fabPulse,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "fab_scale",
    )

    Box(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(999.dp),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.3f),
                )
                .clip(RoundedCornerShape(999.dp))
                .background(ck.surfaceOverlay)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabBtn(CkTabs[0], selected, onSelect)
            TabBtn(CkTabs[1], selected, onSelect)

            // Central FAB
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .graphicsLayer(scaleX = fabScale, scaleY = fabScale),
                contentAlignment = Alignment.Center,
            ) {
                // Sweep glow halo
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .blur(10.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            Brush.sweepGradient(listOf(ck.primary, ck.secondary, ck.accent, ck.primary)),
                            CircleShape,
                        ),
                )
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(ck.primary, ck.secondary)))
                        .clickable(
                            interactionSource = fabSource,
                            indication = null,
                            onClick = onLogTap,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = CkIcons.Plus,
                        contentDescription = "Log spend",
                        tint = ck.onPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            TabBtn(CkTabs[2], selected, onSelect)
            TabBtn(CkTabs[3], selected, onSelect)
        }
    }
}

@Composable
private fun TabBtn(
    tab: TabItem,
    selected: String,
    onSelect: (String) -> Unit,
) {
    val ck = LocalCkPalette.current
    val isActive = selected == tab.key

    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()

    // Icon scale: bounce in on activate, press down on tap
    val iconScale by animateFloatAsState(
        targetValue = when {
            pressed  -> 0.82f
            isActive -> 1.12f
            else     -> 1f
        },
        animationSpec = spring(
            dampingRatio = if (isActive) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "tab_icon_scale",
    )

    // Active pill background alpha
    val bgAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "tab_bg_alpha",
    )

    // Text + icon color (interpolated)
    val colorAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "tab_color",
    )
    val iconColor = lerp(ck.textSoft, ck.text, colorAlpha)
    val labelColor = lerp(ck.textSoft, ck.text, colorAlpha)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ck.text.copy(alpha = bgAlpha * 0.10f))
            .clickable(
                interactionSource = source,
                indication = null,
            ) { onSelect(tab.key) }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconColor,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale),
            )
            Text(
                text = tab.label,
                fontSize = 9.sp,
                fontFamily = JetBrainsMono,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = labelColor,
                letterSpacing = 0.4.sp,
            )
        }
    }
}

private fun lerp(a: Color, b: Color, t: Float): Color = Color(
    red   = a.red   + (b.red   - a.red)   * t,
    green = a.green + (b.green - a.green) * t,
    blue  = a.blue  + (b.blue  - a.blue)  * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)
