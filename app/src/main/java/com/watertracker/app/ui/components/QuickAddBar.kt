package com.watertracker.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.theme.*

/**
 * QuickAddBar - Responsive one-touch hydration log pills with Gift/Palette button.
 * Uses flexible weights and single-line constraints to fit any phone width without text overflow.
 */
@Composable
fun QuickAddBar(
    onAddWater: (Int) -> Unit,
    onWatchRewardForTheme: () -> Unit,
    onOpenCustomDialog: () -> Unit = {},
    strings: Strings = EnglishStrings,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Pulse animation for the Theme Unlock button to invite interaction
    val infiniteTransition = rememberInfiniteTransition(label = "GiftPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick volume presets (+150ml, +250ml, +500ml)
        listOf(150, 250, 500).forEach { volume ->
            QuickVolumePill(
                volumeMl = volume,
                onClick = { onAddWater(volume) },
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }

        // Custom volume button (+ Other / + Khác)
        Box(
            modifier = Modifier
                .weight(1.05f)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(if (isDark) GlassDarkBackground else GlassLightBackground)
                .border(1.dp, if (isDark) GlassDarkBorder else GlassLightBorder, RoundedCornerShape(23.dp))
                .clickable(onClick = onOpenCustomDialog)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = strings.quickAddCustom,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp
                )
            )
        }

        // Rewarded Video Gift/Theme Button (Unlock Wave Colors)
        Box(
            modifier = Modifier
                .weight(1.25f)
                .scale(pulseScale)
                .height(46.dp)
                .shadow(6.dp, RoundedCornerShape(23.dp), ambientColor = GoldReward.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(23.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                    )
                )
                .clickable(onClick = onWatchRewardForTheme)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Unlock themes",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = strings.themesButton,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
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
            .height(46.dp)
            .shadow(3.dp, RoundedCornerShape(23.dp), ambientColor = CyanPrimary.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(23.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(23.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$volumeMl",
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
        )
    }
}
