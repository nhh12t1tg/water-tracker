package com.watertracker.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.theme.*

/**
 * QuickAddBar - One-touch hydration log pills with Gift button for Rewarded Ad themes.
 */
@Composable
fun QuickAddBar(
    onAddWater: (Int) -> Unit,
    onWatchRewardForTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Pulse animation for the Gift button to invite interaction
    val infiniteTransition = rememberInfiniteTransition(label = "GiftPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick volume presets
        listOf(150, 250, 500).forEach { volume ->
            QuickVolumePill(
                volumeMl = volume,
                onClick = { onAddWater(volume) },
                isDark = isDark
            )
        }

        // Rewarded Video Gift Button (Unlock Wave Colors)
        Box(
            modifier = Modifier
                .scale(pulseScale)
                .height(48.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = GoldReward.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                    )
                )
                .clickable(onClick = onWatchRewardForTheme)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = "Watch video to unlock themes",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Màu Nước",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun QuickVolumePill(
    volumeMl: Int,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    Box(
        modifier = modifier
            .height(48.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = CyanPrimary.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(24.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+${volumeMl}ml",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
        )
    }
}
