package com.watertracker.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.window.Dialog
import com.watertracker.app.ads.AdMobManager
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

    // Intercept phone's system back button/gesture to return to HOME instead of exiting app
    BackHandler(enabled = currentScreen != AppScreen.HOME && repository.isOnboarded()) {
        currentScreen = AppScreen.HOME
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
            LaunchedEffect(Unit) {
                val act = context as? Activity
                if (act != null) {
                    AdMobManager.preloadInterstitial(act)
                }
            }
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

                    // Trigger 1 Interstitial ad on onboarding completion
                    val act = context as? Activity
                    if (act != null) {
                        AdMobManager.showInterstitial(act) {}
                    }
                }
            )
        }
        AppScreen.HISTORY -> {
            val weeklyData = remember(currentMl, strings) {
                repository.getWeeklyHistory(strings.daysOfWeek)
            }
            val monthlyData = remember(currentMl) {
                repository.getMonthlyHistory()
            }
            WeeklyHistoryScreen(
                weeklyData = weeklyData,
                monthlyData = monthlyData,
                currentStreak = streakDays,
                availableFreezes = availableFreezes,
                strings = strings,
                onBack = { currentScreen = AppScreen.HOME },
                onWatchRewardForStreakFreeze = {
                    val activity = context as? Activity
                    if (activity != null) {
                        AdMobManager.showRewarded(
                            activity = activity,
                            onRewardGranted = {
                                repository.addStreakFreeze()
                                availableFreezes = repository.getAvailableFreezes()
                            }
                        )
                    } else {
                        repository.addStreakFreeze()
                        availableFreezes = repository.getAvailableFreezes()
                    }
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
                    val previousMl = currentMl
                    val newTotal = repository.addWaterLog(effectiveMl, effectiveMl)
                    currentMl = newTotal
                    streakDays = repository.getStreakDays()

                    val halfGoal = targetMl / 2
                    if (previousMl < halfGoal && newTotal >= halfGoal && !repository.isHalfGoalAdShownToday()) {
                        repository.setHalfGoalAdShownToday(true)
                        if (newTotal < targetMl) {
                            val activity = context as? Activity
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {}
                            }
                        }
                    }

                    if (previousMl < targetMl && newTotal >= targetMl && !repository.isGoalDialogShownToday()) {
                        repository.setGoalDialogShownToday(true)
                        repository.setHalfGoalAdShownToday(true)
                        showGoalDialog = true
                    } else if (previousMl >= targetMl) {
                        val count = repository.incrementPostGoalAddsCount()
                        if (count % 4 == 0) {
                            val activity = context as? Activity
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {}
                            }
                        }
                    }
                },
                onOpenCustomDialog = { showCustomDialog = true },
                onOpenThemeReward = { showThemeDialog = true },
                onOpenHistory = { currentScreen = AppScreen.HISTORY },
                onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                onGoalReached = {
                    if (!repository.isGoalDialogShownToday()) {
                        repository.setGoalDialogShownToday(true)
                        repository.setHalfGoalAdShownToday(true)
                        showGoalDialog = true
                    }
                },
                strings = strings
            )

            if (showCustomDialog) {
                CustomWaterDialog(
                    selectedBeverage = selectedBeverage,
                    onDismiss = { showCustomDialog = false },
                    strings = strings,
                    onConfirm = { customRawMl ->
                        val previousMl = currentMl
                        val effective = Math.round(customRawMl * selectedBeverage.hydrationFactor)
                        val newTotal = repository.addWaterLog(customRawMl, effective)
                        currentMl = newTotal
                        streakDays = repository.getStreakDays()
                        showCustomDialog = false

                        val halfGoal = targetMl / 2
                        if (previousMl < halfGoal && newTotal >= halfGoal && !repository.isHalfGoalAdShownToday()) {
                            repository.setHalfGoalAdShownToday(true)
                            if (newTotal < targetMl) {
                                val activity = context as? Activity
                                if (activity != null) {
                                    AdMobManager.showInterstitial(activity) {}
                                }
                            }
                        }

                        if (previousMl < targetMl && newTotal >= targetMl && !repository.isGoalDialogShownToday()) {
                            repository.setGoalDialogShownToday(true)
                            repository.setHalfGoalAdShownToday(true)
                            showGoalDialog = true
                        } else if (previousMl >= targetMl) {
                            val count = repository.incrementPostGoalAddsCount()
                            if (count % 4 == 0) {
                                val activity = context as? Activity
                                if (activity != null) {
                                    AdMobManager.showInterstitial(activity) {}
                                }
                            }
                        }
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
                                        if (index == 0) {
                                            selectedThemeIndex = index
                                            repository.setActiveThemeIndex(index)
                                            showThemeDialog = false
                                        } else {
                                            val activity = context as? Activity
                                            if (activity != null) {
                                                AdMobManager.showRewarded(
                                                    activity = activity,
                                                    onRewardGranted = {
                                                        selectedThemeIndex = index
                                                        repository.setActiveThemeIndex(index)
                                                    },
                                                    onDismissed = {
                                                        showThemeDialog = false
                                                    }
                                                )
                                            } else {
                                                selectedThemeIndex = index
                                                repository.setActiveThemeIndex(index)
                                                showThemeDialog = false
                                            }
                                        }
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
                    onDismissAndShowInterstitial = {
                        showGoalDialog = false
                        val activity = context as? Activity
                        if (activity != null) {
                            AdMobManager.showInterstitial(activity) {}
                        }
                    }
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
    var pendingAddMl by remember { mutableStateOf<Int?>(null) }

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
                // TOP BAR: Streak Badge & Highest Honor Badge & Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Streak Pill & Honor Badge Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Streak Flame Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark) GlassDarkBackground else GlassLightBackground)
                                .clickable { onOpenHistory() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak flame",
                                tint = FlameOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = strings.streakDays(currentStreakDays),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }

                        // 2. Highest Honor Badge Pill (Huy Hiệu Danh Dự)
                        val (badgeIcon, badgeTitle, badgeColor) = when {
                            currentStreakDays >= 30 -> Triple("👑", strings.badgeMaster, GoldReward)
                            currentStreakDays >= 14 -> Triple("🥇", strings.badgeGold, GoldReward)
                            currentStreakDays >= 7  -> Triple("🥈", strings.badgeWeek, Color(0xFF38BDF8))
                            currentStreakDays >= 3  -> Triple("🥉", strings.badgeStarter, Color(0xFFF97316))
                            else -> Triple("🌱", "${currentStreakDays}/3d", Color(0xFF10B981))
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (currentStreakDays >= 3) {
                                        badgeColor.copy(alpha = 0.16f)
                                    } else {
                                        if (isDark) GlassDarkBackground else GlassLightBackground
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (currentStreakDays >= 3) badgeColor.copy(alpha = 0.45f) else Color(0x15FFFFFF),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { onOpenHistory() }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = badgeIcon, fontSize = 13.sp)
                            Text(
                                text = badgeTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentStreakDays >= 3) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
                        pendingAddMl = rawMl
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

            // CONFIRMATION POPUP FOR INTAKE VOLUME (Balanced 50/50 Button Layout & High Contrast)
            if (pendingAddMl != null) {
                val volume = pendingAddMl!!
                val effectiveMl = Math.round(volume * selectedBeverage.hydrationFactor)
                val isVi = strings.cancel == "Hủy"

                Dialog(onDismissRequest = { pendingAddMl = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) DeepBlueCard else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Icon in subtle circular badge
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = selectedBeverage.icon, fontSize = 32.sp)
                            }

                            // Title
                            Text(
                                text = strings.quickAddConfirmTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                textAlign = TextAlign.Center
                            )

                            // Intake Amount Highlight Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyanPrimary.copy(alpha = 0.1f))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+$volume ml ${selectedBeverage.name}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CyanPrimary
                                    )
                                )
                            }

                            // Body text
                            Text(
                                text = strings.quickAddConfirmDesc(volume, selectedBeverage.name),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // BALANCED 2-BUTTON ROW (Equal 50/50 width, 48dp touch target, high contrast, non-wrapping)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cancel Button: Distinct neutral solid slate background, high contrast text
                                Button(
                                    onClick = { pendingAddMl = null },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                        contentColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF334155)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(
                                        text = strings.cancel,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Confirm Button: Vibrant Cyan primary background, white bold text
                                Button(
                                    onClick = {
                                        val toAdd = effectiveMl
                                        onAddWater(toAdd)
                                        lastLoggedFeedback = strings.bhiFeedbackFormat(
                                            selectedBeverage.icon,
                                            volume,
                                            selectedBeverage.name,
                                            toAdd,
                                            (selectedBeverage.hydrationFactor * 100).toInt()
                                        )
                                        pendingAddMl = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanPrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                ) {
                                    Text(
                                        text = strings.quickAddConfirmButton,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
