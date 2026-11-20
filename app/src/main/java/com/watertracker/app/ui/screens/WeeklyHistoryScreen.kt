package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.data.DetailedAnalytics
import com.watertracker.app.ui.components.AdaptiveBannerContainer
import com.watertracker.app.ui.components.NativeAdCard
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.theme.*
import java.util.Locale

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

fun calculateAnalytics(data: List<DayLogItem>): DetailedAnalytics {
    val totalMl = data.sumOf { it.intakeMl }
    val targetTotalMl = data.sumOf { it.targetMl }
    val completionRate = if (targetTotalMl > 0) {
        Math.round(totalMl.toFloat() * 100f / targetTotalMl.toFloat())
    } else 0

    var peakMl = 0
    var peakName = ""
    var achievedDays = 0

    data.forEach { item ->
        if (item.intakeMl > peakMl) {
            peakMl = item.intakeMl
            peakName = item.dayName
        }
        if (item.intakeMl >= item.targetMl && item.targetMl > 0) {
            achievedDays++
        }
    }

    val avg = if (data.isNotEmpty()) totalMl / data.size else 0

    return DetailedAnalytics(
        totalMl = totalMl,
        targetTotalMl = targetTotalMl,
        completionRatePercent = completionRate,
        peakDayMl = peakMl,
        peakDayName = peakName,
        achievedDaysCount = achievedDays,
        totalDaysCount = data.size,
        averageDailyMl = avg
    )
}

/**
 * WeeklyHistoryScreen - 7-day and 30-day hydration analytics & Streak achievements.
 * Includes Deep Analytics reporting, Native Ads, and Rewarded Video streak protection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyHistoryScreen(
    weeklyData: List<DayLogItem>,
    monthlyData: List<DayLogItem> = emptyList(),
    currentStreak: Int,
    availableFreezes: Int,
    onBack: () -> Unit,
    onWatchRewardForStreakFreeze: () -> Unit,
    strings: Strings = EnglishStrings
) {
    val isDark = isSystemInDarkTheme()
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder
    val isVi = strings.cancel == "Hủy"

    var selectedTab by remember { mutableIntStateOf(0) } // 0: 7 Days (Week), 1: 30 Days (Month)
    val activeData = if (selectedTab == 0 || monthlyData.isEmpty()) weeklyData else monthlyData
    val analytics = remember(activeData) { calculateAnalytics(activeData) }

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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. PERIOD SELECTOR TABS (7 NGÀY vs 30 NGÀY)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(glassBg)
                    .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab 0: 7 Days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 0) CyanPrimary else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVi) "📅 7 Ngày qua (Tuần)" else "📅 Last 7 Days (Week)",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }

                // Tab 1: 30 Days
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == 1) CyanPrimary else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVi) "📊 30 Ngày qua (Tháng)" else "📊 Last 30 Days (Month)",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            // 2. BÁO CÁO PHÂN TÍCH CHUYÊN SÂU (DEEP ANALYTICS REPORT CARD)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(glassBg)
                    .border(1.dp, glassBorder, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = if (isVi) "Báo Cáo Phân Tích Chuyên Sâu" else "Deep Hydration Analytics",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Text(
                                text = if (selectedTab == 0) {
                                    if (isVi) "Tổng kết chi tiết chu kỳ 7 ngày" else "7-day cycle comprehensive breakdown"
                                } else {
                                    if (isVi) "Tổng kết chi tiết chu kỳ 30 ngày" else "30-day monthly comprehensive breakdown"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 4 Key Analytics Metric Blocks (2x2 Grid)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Metric 1: Total Volume
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            title = if (isVi) "Tổng nạp" else "Total Intake",
                            value = "${String.format(Locale.US, "%.1f", analytics.totalMl / 1000f)} L",
                            subtitle = "${analytics.totalMl} ml",
                            tintColor = CyanPrimary,
                            glassBg = glassBg,
                            glassBorder = glassBorder
                        )

                        // Metric 2: Completion Rate
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            title = if (isVi) "Đạt chuẩn" else "Target Rate",
                            value = "${analytics.completionRatePercent}%",
                            subtitle = if (analytics.completionRatePercent >= 100) (if (isVi) "Vượt mục tiêu" else "Exceeded") else (if (isVi) "Tiến độ chuẩn" else "On track"),
                            tintColor = if (analytics.completionRatePercent >= 100) Color(0xFFF59E0B) else MintLight,
                            glassBg = glassBg,
                            glassBorder = glassBorder
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Metric 3: Peak Day
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            title = if (isVi) "Kỷ lục ngày" else "Peak Day",
                            value = "${analytics.peakDayMl} ml",
                            subtitle = if (analytics.peakDayName.isNotEmpty()) (if (isVi) "Ngày: ${analytics.peakDayName}" else "Day: ${analytics.peakDayName}") else "-",
                            tintColor = Color(0xFFF59E0B),
                            glassBg = glassBg,
                            glassBorder = glassBorder
                        )

                        // Metric 4: Goal Days Met
                        MetricBox(
                            modifier = Modifier.weight(1f),
                            title = if (isVi) "Số ngày đạt" else "Goals Met",
                            value = "${analytics.achievedDaysCount} / ${analytics.totalDaysCount}",
                            subtitle = if (isVi) "ngày hoàn thành" else "days achieved",
                            tintColor = Color(0xFF818CF8),
                            glassBg = glassBg,
                            glassBorder = glassBorder
                        )
                    }

                    // Smart Health Habit Encouragement Pill
                    val feedbackText = when {
                        analytics.completionRatePercent >= 80 -> {
                            if (isVi) "🌟 Phong độ xuất sắc! Bạn duy trì thói quen uống nước rất đều đặn và khoa học."
                            else "🌟 Outstanding! You maintain a remarkably consistent and healthy hydration habit."
                        }
                        analytics.completionRatePercent >= 50 -> {
                            if (isVi) "💧 Tiến độ rất tốt! Hãy tiếp tục duy trì và phân bổ đều lượng nước trong ngày nhé."
                            else "💧 Great progress! Keep pacing your water intake evenly throughout the day."
                        }
                        else -> {
                            if (isVi) "⚡ Hãy chú ý bổ sung nước thường xuyên hơn để cơ thể luôn tràn đầy năng lượng nhé!"
                            else "⚡ Remember to drink more water regularly to stay energized and focused!"
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyanPrimary.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = feedbackText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // 3. ADVANCED HYDRATION BAR / TREND CHART
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(glassBg)
                    .border(1.dp, glassBorder, RoundedCornerShape(22.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTab == 0) {
                                strings.historyChartTitle
                            } else {
                                if (isVi) "Biểu Đồ Xu Hướng 30 Ngày" else "30-Day Hydration Trend Chart"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )

                        Text(
                            text = if (selectedTab == 0) (if (isVi) "Mục tiêu: 100%" else "Goal: 100%") else (if (isVi) "Cuộn ngang ➔" else "Scroll ➔"),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (selectedTab == 0) {
                        // 7-day Bar columns evenly spaced
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
                    } else {
                        // 30-day Horizontally Scrollable Bars
                        val monthScrollState = rememberScrollState()
                        LaunchedEffect(Unit) {
                            // Auto scroll to latest date
                            monthScrollState.scrollTo(monthScrollState.maxValue)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .horizontalScroll(monthScrollState),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            activeData.forEach { day ->
                                DayBarColumn(day = day, barWidth = 18.dp)
                            }
                        }
                    }
                }
            }

            // 4. GOOGLE ADMOB NATIVE AD CARD (QUẢNG CÁO TỰ NHIÊN)
            NativeAdCard(
                adSponsoredLabel = if (isVi) "Được tài trợ" else "Sponsored"
            )

            // 5. STREAK FREEZE REWARD CARD
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

            // 6. STREAK BADGES
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
private fun MetricBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    tintColor: Color,
    glassBg: Color,
    glassBorder: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = tintColor
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DayBarColumn(
    day: DayLogItem,
    barWidth: Dp = 22.dp
) {
    val barFillProgress = (day.intakeMl.toFloat() / day.targetMl.coerceAtLeast(1)).coerceIn(0f, 1f)
    val realPercentage = if (day.targetMl > 0) Math.round(day.intakeMl.toFloat() * 100f / day.targetMl.toFloat()) else 0
    val isOverAchieved = realPercentage > 100
    val isAchieved = realPercentage >= 100
    val isDark = isSystemInDarkTheme()

    val barGradient = when {
        isOverAchieved -> Brush.verticalGradient(
            listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFEA580C))
        )
        isAchieved -> Brush.verticalGradient(
            listOf(CyanPrimary, MintAccent)
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
        )
    }

    val textColor = when {
        isOverAchieved -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
        isAchieved -> if (isDark) MintLight else MintDark
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = "$realPercentage%",
            fontSize = 9.sp,
            fontWeight = if (isAchieved) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(barWidth)
                .height(120.dp)
                .clip(RoundedCornerShape(barWidth / 2))
                .background(Color(0x15FFFFFF)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(barFillProgress)
                    .clip(RoundedCornerShape(barWidth / 2))
                    .background(barGradient)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = day.dayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (day.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (day.isToday) {
                    if (isOverAchieved) (if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)) else CyanPrimary
                } else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            ),
            textAlign = TextAlign.Center
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
