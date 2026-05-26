package com.coinkarma.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coinkarma.app.ui.theme.CkIcons
import com.coinkarma.app.ui.theme.Inter
import com.coinkarma.app.ui.theme.LocalCkPalette
import com.coinkarma.app.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    val ck = LocalCkPalette.current

    var phase by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        delay(80)
        phase = 1       // orb enters
        delay(480)
        phase = 2       // text enters
        delay(920)
        phase = 3       // fade out
        delay(460)
        onComplete()
    }

    val orbScale by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
        label = "orb_scale",
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (phase >= 1) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "glow_alpha",
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "text_alpha",
    )
    val textSlide by animateFloatAsState(
        targetValue = if (phase >= 2) 0f else 28f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "text_slide",
    )
    val rootAlpha by animateFloatAsState(
        targetValue = if (phase >= 3) 0f else 1f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "root_alpha",
    )

    val inf = rememberInfiniteTransition(label = "splash_breathe")
    val breathe by inf.animateFloat(
        initialValue = 0.88f, targetValue = 1.14f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe",
    )

    // Rotation for the secondary glow ring
    val rotAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rotAnim.animateTo(360f, animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Restart))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ck.bg)
            .graphicsLayer(alpha = rootAlpha),
        contentAlignment = Alignment.Center,
    ) {
        // Ambient blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ck.primary.copy(alpha = 0.09f * glowAlpha), ck.primary.copy(alpha = 0f)),
                    center = Offset(size.width * 0.28f, size.height * 0.30f),
                    radius = size.minDimension * 0.72f,
                ),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ck.secondary.copy(alpha = 0.07f * glowAlpha), ck.secondary.copy(alpha = 0f)),
                    center = Offset(size.width * 0.76f, size.height * 0.66f),
                    radius = size.minDimension * 0.65f,
                ),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(172.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Outer breathing glow halo
                Box(
                    modifier = Modifier
                        .size((172 * breathe).dp)
                        .blur(36.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ck.primary.copy(alpha = 0.52f * glowAlpha),
                                    ck.primary.copy(alpha = 0f),
                                ),
                                radius = 450f,
                            ),
                            CircleShape,
                        ),
                )
                // Inner secondary halo
                Box(
                    modifier = Modifier
                        .size((110 * breathe).dp)
                        .blur(22.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ck.secondary.copy(alpha = 0.38f * glowAlpha),
                                    ck.secondary.copy(alpha = 0f),
                                ),
                                radius = 280f,
                            ),
                            CircleShape,
                        ),
                )

                // Orb body with 3D radial gradient
                Box(
                    modifier = Modifier
                        .scale(orbScale)
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    splashLighten(ck.primary, 0.50f),
                                    ck.primary,
                                    ck.secondary.copy(alpha = 0.75f),
                                ),
                                center = Offset(36f, 28f),
                                radius = 220f,
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = CkIcons.Leaf,
                        contentDescription = null,
                        tint = ck.onPrimary.copy(alpha = 0.96f),
                        modifier = Modifier.size(54.dp),
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(alpha = textAlpha, translationY = textSlide),
            ) {
                Text(
                    "CoinKarma",
                    fontFamily = SpaceGrotesk,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.2).sp,
                    color = ck.text,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Financial zen, automated.",
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    color = ck.textSoft,
                    letterSpacing = 0.3.sp,
                )
            }
        }
    }
}

internal fun splashLighten(c: Color, amt: Float): Color {
    val r = c.red   + (1f - c.red)   * amt
    val g = c.green + (1f - c.green) * amt
    val b = c.blue  + (1f - c.blue)  * amt
    return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f))
}
