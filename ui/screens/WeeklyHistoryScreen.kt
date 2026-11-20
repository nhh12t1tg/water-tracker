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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
 * Redesigned with UI/UX Pro Max high-contrast pastel cards and clean segmented control.
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
    val isVi = strings.cancel == "Hủy"

    // Primary Card background (Pure white in light mode, Deep slate in dark mode)
    val cardBackground = if (isDark) DeepBlueCard else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

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
            // 1. HIGH-CONTRAST SEGMENTED CONTROL TABS (7 NGÀY vs 30 NGÀY)
            val segmentTrackBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
            val segmentBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(segmentTrackBg)
                    .border(1.dp, segmentBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 0: 7 Days
                val isSelected0 = selectedTab == 0
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected0) {
                                Modifier
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF334155) else Color.White)
                            } else {
                                Modifier.background(Color.Transparent)
                            }
                        )
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVi) "📅 7 Ngày" else "📅 7 Days",
                        fontWeight = if (isSelected0) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected0) {
                            if (isDark) Color(0xFF38BDF8) else CyanPrimary
                        } else {
                            if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        },
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                // Tab 1: 30 Days
                val isSelected1 = selectedTab == 1
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected1) {
                                Modifier
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF334155) else Color.White)
                            } else {
                                Modifier.background(Color.Transparent)
                            }
                        )
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isVi) "📊 30 Ngày" else "📊 30 Days",
                        fontWeight = if (isSelected1) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (isSelected1) {
                            if (isDark) Color(0xFF38BDF8) else CyanPrimary
                        } else {
                            if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        },
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
            }

            // 2. BÁO CÁO PHÂN TÍCH CHUYÊN SÂU (DEEP ANALYTICS REPORT CARD)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBackground)
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyanPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 4 DISTINCT HIGH-CONTRAST METRIC BLOCKS (2x2 Grid)
                    // Designed with pastel tinted card backgrounds to eliminate white-on-white collision
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Metric 1: Total Intake (Sky Tint)
                        PastelMetricBox(
                            modifier = Modifier.weight(1f),
                            title = if (isVi) "Tổng nạp" else "Total Intake",
                            value = "${String.format(Locale.US, "%.1f", analytics.totalMl / 1000f)} L",
                            subtitle = "${analytics.totalMl} ml",
                            bgLight = Color(0xFFF0F9FF), // Sky 50
                            borderLight = Color(0xFFBAE6FD), // Sky 200
                            accentColor = CyanPrimary,
                            isDark = isDark
                        )

                        // Metric 2: Completion Rate (Emerald Tint)
                        PastelMetricBox(
                            modifier = Modifier.weight(1f),
                            title = if (isVi) "Đạt chuẩn" else "Target Rate",
                            value = "${analytics.completionRatePercent}%",
                            subtitle = if (analytics.completionRatePercent >= 100) (if (isVi) "Vượt mục tiêu" else "Exceeded") else (if (isVi) "Tiến độ chuẩn" else "On track"),
                            bgLight = Color(0xFFF0FDF4), // Emerald 50
                            borderLight = Color(0xFFBBF7D0), // Emerald 200
                            accentColor = if (analytics.completionRatePercent >= 100) Color(0xFFD97706) else Color(0xFF16A34A),
                            isDark = isDark
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Metric 3: Peak Day (Amber Tint)
                        PastelMetricBox(
                            modifier = Modifier.weight(1f),
                            title = if (isVi) "Kỷ lục ngày" else "Peak Day",
                            value = "${analytics.peakDayMl} ml",
                            subtitle = if (analytics.peakDayName.isNotEmpty()) (if (isVi) "Ngày: ${analytics.peakDayName}" else "Day: ${analytics.peakDayName}") else "-",
                            bgLight = Color(0xFFFFFBEB), // Amber 50
                            borderLight = Color(0xFFFDE68A), // Amber 200
                            accentColor = Color(0xFFD97706),
                            isDark = isDark
                        )

                        // Metric 4: Goal Days Met (Indigo Tint)
                        PastelMetricBox(
                            modifier = Modifier.weight(1f),
                            title = if (isVi) "Số ngày đạt" else "Goals Met",
                            value = "${analytics.achievedDaysCount} / ${analytics.totalDaysCount}",
                            subtitle = if (isVi) "ngày hoàn thành" else "days achieved",
                            bgLight = Color(0xFFEEF2FF), // Indigo 50
                            borderLight = Color(0xFFC7D2FE), // Indigo 200
                            accentColor = Color(0xFF4F46E5),
                            isDark = isDark
                        )
                    }

                    // Smart Health Habit Encouragement Pill (High Contrast Soft Banner)
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
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFE0F2FE))
                            .border(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF7DD3FC), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = feedbackText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0369A1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            // 3. ADVANCED HYDRATION BAR / TREND CHART (Scroll text removed completely)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBackground)
                    .border(1.dp, cardBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (selectedTab == 0) {
                            strings.historyChartTitle
                        } else {
                            if (isVi) "Biểu Đồ 30 Ngày" else "30-Day Hydration Chart"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

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
                    BadgeCard(badge = badge, isDark = isDark)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * High-contrast pastel metric box that completely prevents white-on-white blend.
 */
@Composable
private fun PastelMetricBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    bgLight: Color,
    borderLight: Color,
    accentColor: Color,
    isDark: Boolean
) {
    val boxBg = if (isDark) Color(0xFF0F172A) else bgLight
    val boxBorder = if (isDark) accentColor.copy(alpha = 0.4f) else borderLight

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(boxBg)
            .border(1.dp, boxBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isDark) Color(0xFF64748B) else Color(0xFF64748B),
                fontWeight = FontWeight.Medium
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
                .background(if (isDark) Color(0x22FFFFFF) else Color(0xFFF1F5F9)),
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
    isDark: Boolean
) {
    val badgeColor = if (badge.isUnlocked) GoldReward else Color.Gray
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (badge.isUnlocked) GoldReward.copy(alpha = 0.6f) else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))

    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
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
