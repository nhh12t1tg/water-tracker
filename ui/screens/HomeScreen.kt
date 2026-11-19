package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
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
enum class AppScreen {
    HOME,
    HISTORY,
    SETTINGS
}

@Composable
fun HomeScreen() {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var currentMl by remember { mutableIntStateOf(1250) }
    var targetMl by remember { mutableIntStateOf(2500) }
    var streakDays by remember { mutableIntStateOf(5) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectedThemeIndex by remember { mutableIntStateOf(0) }

    val themes = listOf(
        Pair("Cyan Tươi Mát", WaterGradientCyan),
        Pair("Mint Bạc Hà", WaterGradientMint),
        Pair("Sunset Hoàng Hôn", Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFEA580C)))),
        Pair("Deep Ocean", Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF1E3A8A))))
    )

    when (currentScreen) {
        AppScreen.HISTORY -> {
            val sampleWeeklyData = listOf(
                DayLogItem("T2", 2400, 2500),
                DayLogItem("T3", 2600, 2500),
                DayLogItem("T4", 2500, 2500),
                DayLogItem("T5", 2700, 2500),
                DayLogItem("T6", 2500, 2500),
                DayLogItem("T7", 2100, 2500),
                DayLogItem("CN", currentMl, targetMl, isToday = true)
            )
            WeeklyHistoryScreen(
                weeklyData = sampleWeeklyData,
                currentStreak = streakDays,
                availableFreezes = 1,
                onBack = { currentScreen = AppScreen.HOME },
                onWatchRewardForStreakFreeze = {
                    streakDays += 1
                }
            )
        }
        AppScreen.SETTINGS -> {
            SettingsScreen(
                onBack = { currentScreen = AppScreen.HOME }
            )
        }
        AppScreen.HOME -> {
            HomeScreen(
                currentMl = currentMl,
                targetMl = targetMl,
                currentStreakDays = streakDays,
                activeWaveBrush = themes[selectedThemeIndex].second,
                onAddWater = { ml -> currentMl += ml },
                onOpenThemeReward = { showThemeDialog = true },
                onOpenHistory = { currentScreen = AppScreen.HISTORY },
                onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                onGoalReached = { showGoalDialog = true }
            )

            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFF59E0B))
                            Text("Mở Khóa Màu Nước Mới")
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Xem 1 video ngắn (Rewarded Ad) để mở khóa màu nước yêu thích:", fontSize = 13.sp)
                            themes.forEachIndexed { index, (name, _) ->
                                OutlinedButton(
                                    onClick = {
                                        selectedThemeIndex = index
                                        showThemeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedThemeIndex == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                ) {
                                    Text(
                                        text = if (index == 0) "$name (Mặc định)" else "🎁 $name ${if (selectedThemeIndex == index) "✓" else ""}",
                                        fontWeight = if (selectedThemeIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text("Đóng")
                        }
                    }
                )
            }

            if (showGoalDialog) {
                GoalCompletedDialog(
                    totalMl = targetMl,
                    onDismissAndShowInterstitial = { showGoalDialog = false }
                )
            }
        }
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
    var selectedBeverage by remember { mutableStateOf(SCIENTIFIC_BEVERAGES[0]) }
    var lastLoggedFeedback by remember { mutableStateOf<String?>(null) }

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

                // Daily Motivation Subtitle or Beverage Intake Feedback
                Text(
                    text = lastLoggedFeedback ?: if (currentMl >= targetMl) "Tuyệt vời! Bạn đã đủ nước hôm nay 🎉" else "Hãy uống từng ngụm nhỏ để cơ thể hấp thụ tốt nhất",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = if (lastLoggedFeedback != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (lastLoggedFeedback != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.weight(0.6f))

                // SCIENTIFIC BEVERAGE SELECTION BAR
                BeverageSelectorBar(
                    selectedBeverage = selectedBeverage,
                    onSelectBeverage = { selectedBeverage = it },
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // BOTTOM CONTROLS: Quick-Add & Rewarded Gift
                QuickAddBar(
                    onAddWater = { rawMl ->
                        val effectiveMl = Math.round(rawMl * selectedBeverage.hydrationFactor)
                        onAddWater(effectiveMl)
                        lastLoggedFeedback = "${selectedBeverage.icon} +${rawMl}ml ${selectedBeverage.name} ➔ Hấp thu thực tế: +${effectiveMl}ml (${(selectedBeverage.hydrationFactor * 100).toInt()}% BHI)"
                    },
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
