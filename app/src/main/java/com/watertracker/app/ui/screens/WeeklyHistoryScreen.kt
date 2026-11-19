package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.watertracker.app.ui.theme.*

data class DayLogItem(
    val dayName: String,
    val intakeMl: Int,
    val targetMl: Int,
    val isToday: Boolean = false
)

data class StreakBadge(
    val title: String,
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
    onWatchRewardForStreakFreeze: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    val badges = listOf(
        StreakBadge("Khởi Đầu", 3, currentStreak >= 3),
        StreakBadge("Kiên Trì 1 Tuần", 7, currentStreak >= 7),
        StreakBadge("Thói Quen Vàng", 14, currentStreak >= 14),
        StreakBadge("Bậc Thầy Nước", 30, currentStreak >= 30)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch Sử & Huy Hiệu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = if (isDark) DeepBlueDark else SkyBlueLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
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
                        text = "Biểu Đồ 7 Ngày Qua",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // Bar Chart Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyData.forEach { day ->
                            val heightFraction = (day.intakeMl.toFloat() / day.targetMl.toFloat()).coerceIn(0.08f, 1.0f)
                            val isFull = day.intakeMl >= day.targetMl

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                // Animated Pillar Bar
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                        .background(
                                            if (isFull) WaterGradientCyan else Brush.verticalGradient(
                                                listOf(Color(0xFF94A3B8), Color(0xFF64748B))
                                            )
                                        )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = day.dayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (day.isToday) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. STREAK RECOVERY VIA REWARDED AD CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0369A1), Color(0xFF0284C7))
                        )
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, tint = CyanLight)
                            Text(
                                text = "Bảo Vệ Chuỗi Streak ($availableFreezes sẵn có)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = "Xem 1 video nhận 1 Khiên Đóng Băng khi lỡ quên uống",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp
                            )
                        )
                    }

                    Button(
                        onClick = onWatchRewardForStreakFreeze,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldReward),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+1 Khiên", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // 3. STREAK BADGES SECTION
            Text(
                text = "Huy Hiệu Chuỗi ($currentStreak Ngày)",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(badges) { badge ->
                    BadgeItem(badge = badge, isDark = isDark)
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: StreakBadge, isDark: Boolean) {
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val borderBrush = if (badge.isUnlocked) {
        Brush.linearGradient(listOf(Color(0xFFFBBF24), GoldReward))
    } else {
        Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
    }

    Column(
        modifier = Modifier
            .width(105.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(glassBg)
            .border(1.5.dp, borderBrush, RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(if (badge.isUnlocked) 8.dp else 0.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (badge.isUnlocked) Brush.radialGradient(listOf(Color(0xFFFF7043), FlameOrange))
                    else Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (badge.isUnlocked) Icons.Default.LocalFireDepartment else Icons.Default.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = badge.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            ),
            color = if (badge.isUnlocked) MaterialTheme.colorScheme.onBackground else Color.Gray
        )

        Text(
            text = "${badge.targetDays} ngày",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
