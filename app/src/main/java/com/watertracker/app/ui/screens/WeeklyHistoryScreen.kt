package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.components.AdaptiveBannerContainer
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.theme.*

data class DayLogItem(
    val dayName: String,
    val intakeMl: Int,
    val targetMl: Int,
    val isToday: Boolean = false
)

data class StreakBadge(
    val title: String,
    val daysLabel: String,
    val targetDays: Int,
    val isUnlocked: Boolean
)

/**
 * WeeklyHistoryScreen - 7-day hydration chart & Streak achievements.
 * Includes Rewarded Video option to freeze/restore streaks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyHistoryScreen(
    weeklyData: List<DayLogItem>,
    currentStreak: Int,
    availableFreezes: Int,
    onBack: () -> Unit,
    onWatchRewardForStreakFreeze: () -> Unit,
    strings: Strings = EnglishStrings
) {
    val isDark = isSystemInDarkTheme()
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    val badges = listOf(
        StreakBadge(strings.badgeStarter, strings.badgeStarterDays, 3, currentStreak >= 3),
        StreakBadge(strings.badgeWeek, strings.badgeWeekDays, 7, currentStreak >= 7),
        StreakBadge(strings.badgeGold, strings.badgeGoldDays, 14, currentStreak >= 14),
        StreakBadge(strings.badgeMaster, strings.badgeMasterDays, 30, currentStreak >= 30)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.historyTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            AdaptiveBannerContainer(
                isTestMode = true
            )
        },
        containerColor = if (isDark) DeepBlueDark else SkyBlueLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. 7-DAY BAR CHART CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(glassBg)
                    .border(1.dp, glassBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = strings.historyChartTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    // Weekly Bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyData.forEach { day ->
                            DayBarColumn(day = day)
                        }
                    }
                }
            }

            // 2. STREAK FREEZE REWARD CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0284C7).copy(alpha = 0.2f), Color(0xFF0EA5E9).copy(alpha = 0.1f))
                        )
                    )
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = Color(0xFF38BDF8))
                        }
                        Column {
                            Text(
                                text = strings.streakShieldTitle(availableFreezes),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = strings.streakShieldDesc,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Button(
                        onClick = onWatchRewardForStreakFreeze,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.addShieldCta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 3. STREAK BADGES
            Text(
                text = strings.badgesTitle(currentStreak),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(badges) { badge ->
                    BadgeCard(badge = badge, glassBg = glassBg, glassBorder = glassBorder)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DayBarColumn(day: DayLogItem) {
    val barFillProgress = (day.intakeMl.toFloat() / day.targetMl.coerceAtLeast(1)).coerceIn(0f, 1f)
    val realPercentage = if (day.targetMl > 0) Math.round(day.intakeMl.toFloat() * 100f / day.targetMl.toFloat()) else 0
    val isAchieved = day.intakeMl >= day.targetMl

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        // Percentage/Status Indicator (Uncapped: shows true percentage even when > 100%)
        Text(
            text = "$realPercentage%",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isAchieved) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Bar Track
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0x15FFFFFF)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(barFillProgress)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (isAchieved) {
                            Brush.verticalGradient(listOf(CyanPrimary, MintAccent))
                        } else {
                            Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
                        }
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day Label (e.g. Mon, Tue)
        Text(
            text = day.dayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (day.isToday) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun BadgeCard(
    badge: StreakBadge,
    glassBg: Color,
    glassBorder: Color
) {
    val badgeColor = if (badge.isUnlocked) GoldReward else Color.Gray

    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(glassBg)
            .border(1.dp, if (badge.isUnlocked) GoldReward.copy(alpha = 0.5f) else glassBorder, RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (badge.isUnlocked) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Unlocked",
                    tint = GoldReward,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = badge.title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onBackground else Color.Gray
            ),
            maxLines = 1
        )

        Text(
            text = badge.daysLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        )
    }
}
