package com.watertracker.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

/**
 * LiquidWaveCircle - Central interactive hydration visualizer.
 * Features dual-layer sine waves, glassmorphism border highlight, and fluid progress transition.
 */
@Composable
fun LiquidWaveCircle(
    currentMl: Int,
    targetMl: Int,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    waveBrush: Brush = WaterGradientCyan
) {
    val isDark = isSystemInDarkTheme()
    // Visual level height clamped to 1.0f (100% full circle)
    val visualProgress = (currentMl.toFloat() / targetMl.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    // Smooth level height animation (0.0 to 1.0)
    val animatedProgress by animateFloatAsState(
        targetValue = visualProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "WaterLevelProgress"
    )

    // Continuous infinite horizontal wave drift
    val transition = rememberInfiniteTransition(label = "WaveAnimation")
    val wavePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    val wavePhaseBack by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhaseBack"
    )

    val glassBorderColor = if (isDark) GlassDarkBorder else GlassLightBorder
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = CyanPrimary.copy(alpha = 0.25f),
                spotColor = CyanPrimary.copy(alpha = 0.35f)
            )
            .clip(CircleShape)
            .background(glassBg)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.4f else 0.8f),
                        glassBorderColor,
                        CyanLight.copy(alpha = 0.3f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Wave Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = this.size.width
            val height = this.size.height
            val waterLevelY = height * (1f - (animatedProgress.coerceAtMost(1.0f)))
            val amplitude = 14.dp.toPx() * (1f - (animatedProgress - 0.5f).let { kotlin.math.abs(it) * 0.5f })

            val circleClipPath = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(0f, 0f, width, height))
            }

            clipPath(circleClipPath) {
                // Secondary Wave (Deeper, semi-transparent background wave)
                val backWavePath = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, waterLevelY)
                    for (x in 0..width.toInt() step 5) {
                        val angle = (x / width) * 2f * PI.toFloat() + wavePhaseBack
                        val y = waterLevelY + amplitude * 0.75f * sin(angle)
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(width, height)
                    close()
                }
                drawPath(path = backWavePath, brush = waveBrush, alpha = 0.45f)

                // Primary Wave (Front crisp wave)
                val frontWavePath = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, waterLevelY)
                    for (x in 0..width.toInt() step 5) {
                        val angle = (x / width) * 2f * PI.toFloat() + wavePhase
                        val y = waterLevelY + amplitude * sin(angle)
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(width, height)
                    close()
                }
                drawPath(path = frontWavePath, brush = waveBrush, alpha = 0.85f)
            }
        }

        // Inner Hydration Status Text Overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val realPercentage = if (targetMl > 0) Math.round(currentMl.toFloat() * 100f / targetMl.toFloat()) else 0
            Text(
                text = "$realPercentage%",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 46.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        blurRadius = 8f
                    )
                ),
                color = if (animatedProgress > 0.55f) Color.White else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$currentMl / $targetMl ml",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (animatedProgress > 0.55f) Color.White.copy(alpha = 0.95f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
