package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.data.WaterRepository
import com.watertracker.app.ui.components.*
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.LocalizationManager
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.theme.*

enum class AppScreen {
    ONBOARDING,
    HOME,
    HISTORY,
    SETTINGS
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val repository = remember { WaterRepository(context) }

    var currentLanguageCode by remember { mutableStateOf(repository.getAppLanguage()) }
    val strings = remember(currentLanguageCode) { LocalizationManager.getStrings(currentLanguageCode) }

    var currentScreen by remember {
        mutableStateOf(if (repository.isOnboarded()) AppScreen.HOME else AppScreen.ONBOARDING)
    }

    var currentMl by remember { mutableIntStateOf(repository.getTodayConsumedMl()) }
    var targetMl by remember { mutableIntStateOf(repository.getDailyGoalMl()) }
    var streakDays by remember { mutableIntStateOf(repository.getStreakDays()) }
    var availableFreezes by remember { mutableIntStateOf(repository.getAvailableFreezes()) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var selectedThemeIndex by remember { mutableIntStateOf(repository.getActiveThemeIndex()) }

    val localizedBeverages = remember(strings) { getScientificBeverages(strings) }
    var selectedBeverage by remember { mutableStateOf(localizedBeverages[0]) }

    // Keep selectedBeverage synchronized if language changes
    LaunchedEffect(strings) {
        val matching = localizedBeverages.find { it.id == selectedBeverage.id }
        if (matching != null) {
            selectedBeverage = matching
        }
    }

    val themes = listOf(
        Pair(strings.themeCyan, WaterGradientCyan),
        Pair(strings.themeMint, WaterGradientMint),
        Pair(strings.themeSunset, Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFEA580C)))),
        Pair(strings.themeDeepOcean, Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF1E3A8A))))
    )

    when (currentScreen) {
        AppScreen.ONBOARDING -> {
            OnboardingScreen(
                strings = strings,
                currentLanguageCode = currentLanguageCode,
                onSelectLanguage = { langCode ->
                    repository.setAppLanguage(langCode)
                    currentLanguageCode = langCode
                },
                onComplete = { weightKg, gender, activity, calculatedGoal ->
                    repository.saveOnboardingProfile(weightKg, gender, activity, calculatedGoal)
                    targetMl = calculatedGoal ?: 2500
                    currentMl = 0
                    streakDays = 0
                    currentScreen = AppScreen.HOME
                }
            )
        }
        AppScreen.HISTORY -> {
            val weeklyData = remember(currentMl, strings) {
                repository.getWeeklyHistory(strings.daysOfWeek)
            }
            WeeklyHistoryScreen(
                weeklyData = weeklyData,
                currentStreak = streakDays,
                availableFreezes = availableFreezes,
                strings = strings,
                onBack = { currentScreen = AppScreen.HOME },
                onWatchRewardForStreakFreeze = {
                    repository.addStreakFreeze()
                    availableFreezes = repository.getAvailableFreezes()
                }
            )
        }
        AppScreen.SETTINGS -> {
            SettingsScreen(
                strings = strings,
                currentLanguageCode = currentLanguageCode,
                onSelectLanguage = { langCode ->
                    repository.setAppLanguage(langCode)
                    currentLanguageCode = langCode
                },
                onBack = { currentScreen = AppScreen.HOME }
            )
        }
        AppScreen.HOME -> {
            HomeScreen(
                currentMl = currentMl,
                targetMl = targetMl,
                currentStreakDays = streakDays,
                activeWaveBrush = themes[selectedThemeIndex.coerceIn(0, themes.lastIndex)].second,
                selectedBeverage = selectedBeverage,
                onSelectBeverage = { selectedBeverage = it },
                onAddWater = { effectiveMl ->
                    val newTotal = repository.addWaterLog(effectiveMl, effectiveMl)
                    currentMl = newTotal
                    streakDays = repository.getStreakDays()
                },
                onOpenCustomDialog = { showCustomDialog = true },
                onOpenThemeReward = { showThemeDialog = true },
                onOpenHistory = { currentScreen = AppScreen.HISTORY },
                onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                onGoalReached = { showGoalDialog = true },
                strings = strings
            )

            if (showCustomDialog) {
                CustomWaterDialog(
                    selectedBeverage = selectedBeverage,
                    onDismiss = { showCustomDialog = false },
                    strings = strings,
                    onConfirm = { customRawMl ->
                        val effective = Math.round(customRawMl * selectedBeverage.hydrationFactor)
                        val newTotal = repository.addWaterLog(customRawMl, effective)
                        currentMl = newTotal
                        streakDays = repository.getStreakDays()
                        showCustomDialog = false
                    }
                )
            }

            if (showThemeDialog) {
                AlertDialog(
                    onDismissRequest = { showThemeDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color(0xFFF59E0B))
                            Text(strings.themeDialogTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(strings.themeDialogSubtitle, fontSize = 13.sp)
                            themes.forEachIndexed { index, (name, _) ->
                                OutlinedButton(
                                    onClick = {
                                        selectedThemeIndex = index
                                        repository.setActiveThemeIndex(index)
                                        showThemeDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selectedThemeIndex == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                    )
                                ) {
                                    Text(
                                        text = if (index == 0) name else "🎁 $name ${if (selectedThemeIndex == index) "✓" else ""}",
                                        fontWeight = if (selectedThemeIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemeDialog = false }) {
                            Text(strings.close)
                        }
                    }
                )
            }

            if (showGoalDialog) {
                GoalCompletedDialog(
                    totalMl = targetMl,
                    strings = strings,
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
    selectedBeverage: BeverageItem = SCIENTIFIC_BEVERAGES[0],
    onSelectBeverage: (BeverageItem) -> Unit = {},
    onAddWater: (Int) -> Unit,
    onOpenCustomDialog: () -> Unit = {},
    onOpenThemeReward: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onGoalReached: () -> Unit,
    strings: Strings = EnglishStrings
) {
    val isDark = isSystemInDarkTheme()
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            // Dynamic fluid sizing for the wave circle that adapts to all screen heights/widths
            val waveCircleSize = (screenWidth * 0.62f).coerceIn(180.dp, 255.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TOP BAR: Streak Badge & Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
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
                            text = strings.streakDays(currentStreakDays),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    // Action Icons (History & Settings)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onOpenHistory) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = strings.historyTitle,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = strings.settingsTitle,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.3f))

                // CENTER: Liquid Wave Circle (Responsive size)
                LiquidWaveCircle(
                    currentMl = currentMl,
                    targetMl = targetMl,
                    waveBrush = activeWaveBrush,
                    size = waveCircleSize
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Motivation Subtitle / BHI Feedback (Single or 2 lines max with clean ellipsis)
                Text(
                    text = lastLoggedFeedback ?: if (currentMl >= targetMl) strings.motivationGoalReached else strings.motivationDefault,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = if (lastLoggedFeedback != null) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (lastLoggedFeedback != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(0.5f))

                // SCIENTIFIC BEVERAGE SELECTION BAR
                BeverageSelectorBar(
                    selectedBeverage = selectedBeverage,
                    onSelectBeverage = onSelectBeverage,
                    strings = strings,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // BOTTOM CONTROLS: Quick-Add, Custom & Rewarded Gift
                QuickAddBar(
                    onAddWater = { rawMl ->
                        val effectiveMl = Math.round(rawMl * selectedBeverage.hydrationFactor)
                        onAddWater(effectiveMl)
                        lastLoggedFeedback = strings.bhiFeedbackFormat(
                            selectedBeverage.icon,
                            rawMl,
                            selectedBeverage.name,
                            effectiveMl,
                            (selectedBeverage.hydrationFactor * 100).toInt()
                        )
                    },
                    onOpenCustomDialog = onOpenCustomDialog,
                    onWatchRewardForTheme = onOpenThemeReward,
                    strings = strings,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // BOTTOM ADMOB ADAPTIVE BANNER (Fixed 60dp height, >=16dp padding)
                AdaptiveBannerContainer(
                    isTestMode = true
                )
            }
        }
    }
}
