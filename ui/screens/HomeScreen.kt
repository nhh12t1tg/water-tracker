package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.components.*
import com.watertracker.app.ui.theme.*

/**
 * HomeScreen - Primary Water Tracker Dashboard.
 * Layout Flow:
 * - Top Bar: Streak badge + Navigation actions.
 * - Center: Liquid Wave animation circle.
 * - Controls: Quick-add water pills + Gift theme unlock.
 * - Bottom: Isolated AdMob Banner container.
 */
@Composable
fun HomeScreen() {
    var currentMl by remember { mutableStateOf(1250) }
    var targetMl by remember { mutableStateOf(2500) }
    var streakDays by remember { mutableStateOf(5) }
    var showGoalDialog by remember { mutableStateOf(false) }

    HomeScreen(
        currentMl = currentMl,
        targetMl = targetMl,
        currentStreakDays = streakDays,
        onAddWater = { ml -> currentMl += ml },
        onOpenThemeReward = { /* Rewarded ad */ },
        onOpenHistory = { /* Open history */ },
        onOpenSettings = { /* Open settings */ },
        onGoalReached = { showGoalDialog = true }
    )

    if (showGoalDialog) {
        GoalCompletedDialog(
            totalMl = targetMl,
            onDismissAndShowInterstitial = { showGoalDialog = false }
        )
    }
}

@Composable
fun HomeScreen(
    currentMl: Int,
    targetMl: Int,
    currentStreakDays: Int,
    activeWaveBrush: Brush = WaterGradientCyan,
    onAddWater: (Int) -> Unit,
    onOpenThemeReward: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onGoalReached: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    // Trigger celebration when reaching 100% target
    LaunchedEffect(currentMl, targetMl) {
        if (currentMl >= targetMl && targetMl > 0) {
            onGoalReached()
        }
    }

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(DeepBlueDark, Color(0xFF0F172A), DeepBlueCard))
    } else {
        Brush.verticalGradient(listOf(SkyBlueLight, Color(0xFFE0F2FE), Color(0xFFBAE6FD)))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP BAR: Streak Badge & Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Streak Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) GlassDarkBackground else GlassLightBackground)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak flame",
                            tint = FlameOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "$currentStreakDays ngày",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    // Action Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onOpenHistory) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.4f))

                // CENTER: Liquid Wave Circle
                LiquidWaveCircle(
                    currentMl = currentMl,
                    targetMl = targetMl,
                    waveBrush = activeWaveBrush,
                    size = 270.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Daily Motivation Subtitle
                Text(
                    text = if (currentMl >= targetMl) "Tuyệt vời! Bạn đã đủ nước hôm nay 🎉" else "Hãy uống từng ngụm nhỏ để cơ thể hấp thụ tốt nhất",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.weight(0.6f))

                // BOTTOM CONTROLS: Quick-Add & Rewarded Gift
                QuickAddBar(
                    onAddWater = onAddWater,
                    onWatchRewardForTheme = onOpenThemeReward,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // BOTTOM ADMOB ADAPTIVE BANNER (Fixed 60dp height, separated by padding)
                AdaptiveBannerContainer(
                    isTestMode = true
                )
            }
        }
    }
}
