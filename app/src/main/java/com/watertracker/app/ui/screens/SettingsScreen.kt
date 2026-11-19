package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    var isDndEnabled by remember { mutableStateOf(true) }
    var intervalMinutes by remember { mutableIntStateOf(90) }
    var wakeUpTime by remember { mutableStateOf("07:00") }
    var bedTime by remember { mutableStateOf("22:00") }
    var showGdprToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài Đặt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Group 1: Nhắc nhở thông minh
            Text(
                text = "NHẮC NHỞ UỐNG NƯỚC",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            SettingCard(glassBg, glassBorder) {
                SettingRow(
                    icon = Icons.Default.Notifications,
                    title = "Tần suất nhắc nhở",
                    subtitle = "$intervalMinutes phút một lần"
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(60, 90, 120).forEach { mins ->
                            FilterChip(
                                selected = intervalMinutes == mins,
                                onClick = { intervalMinutes = mins },
                                label = { Text("${mins}p") }
                            )
                        }
                    }
                }
            }

            // Group 2: Khung giờ thức / ngủ (DND)
            Text(
                text = "CHẾ ĐỘ KHÔNG LÀM PHIỀN (DND)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            SettingCard(glassBg, glassBorder) {
                SettingToggleRow(
                    icon = Icons.Default.Bedtime,
                    title = "Tắt chuông ban đêm (DND)",
                    subtitle = "Không gửi thông báo khi đang ngủ ($bedTime - $wakeUpTime)",
                    checked = isDndEnabled,
                    onCheckedChange = { isDndEnabled = it }
                )

                HorizontalDivider(color = glassBorder)

                SettingRow(
                    icon = Icons.Default.WbSunny,
                    title = "Giờ thức dậy",
                    subtitle = wakeUpTime
                ) {}

                HorizontalDivider(color = glassBorder)

                SettingRow(
                    icon = Icons.Default.Bedtime,
                    title = "Giờ đi ngủ",
                    subtitle = bedTime
                ) {}
            }

            // Group 3: Quyền riêng tư & Quảng cáo (GDPR UMP)
            Text(
                text = "QUYỀN RIÊNG TƯ & ADMOB",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            SettingCard(glassBg, glassBorder) {
                SettingRow(
                    icon = Icons.Default.Security,
                    title = "Tùy chọn quyền riêng tư (GDPR / UMP)",
                    subtitle = "Quản lý dữ liệu đồng ý quảng cáo cá nhân hóa theo luật Châu Âu"
                ) {
                    OutlinedButton(
                        onClick = { showGdprToast = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Quản lý", fontSize = 12.sp)
                    }
                }
            }

            if (showGdprToast) {
                AlertDialog(
                    onDismissRequest = { showGdprToast = false },
                    confirmButton = {
                        TextButton(onClick = { showGdprToast = false }) {
                            Text("Đồng ý")
                        }
                    },
                    title = { Text("Google UMP Consent") },
                    text = {
                        Text("Trạng thái: Đã cấp quyền (Consent Obtained). Bạn có thể thu hồi hoặc điều chỉnh lựa chọn quảng cáo bất kỳ lúc nào theo GDPR TCF v2.2.")
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingCard(
    bgColor: Color,
    borderColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                )
            }
        }
        action()
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
