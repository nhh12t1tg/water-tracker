package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Language
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
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.i18n.SupportedLanguage
import com.watertracker.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    strings: Strings = EnglishStrings,
    currentLanguageCode: String = "en",
    onSelectLanguage: (String) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    var isDndEnabled by remember { mutableStateOf(true) }
    var intervalMinutes by remember { mutableIntStateOf(90) }
    var wakeUpTime by remember { mutableStateOf("07:00") }
    var bedTime by remember { mutableStateOf("22:00") }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showGdprToast by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
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
            // Group 0: Language & Localization (Prominent for international users)
            Text(
                text = strings.settingsLanguageSection,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            SettingCard(glassBg, glassBorder) {
                val currentLang = SupportedLanguage.fromCode(currentLanguageCode)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguagePicker = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = strings.settingsLanguageLabel,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "${currentLang.flag} ${currentLang.displayName}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showLanguagePicker = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(currentLang.code.uppercase(), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Group 1: Smart Reminders
            Text(
                text = strings.settingsRemindersSection,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            SettingCard(glassBg, glassBorder) {
                SettingRow(
                    icon = Icons.Default.Notifications,
                    title = strings.settingsReminderFrequency,
                    subtitle = strings.settingsReminderFreqFormat(intervalMinutes)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(60, 90, 120).forEach { mins ->
                            FilterChip(
                                selected = intervalMinutes == mins,
                                onClick = { intervalMinutes = mins },
                                label = { Text("${mins}m") }
                            )
                        }
                    }
                }
            }

            // Group 2: DND Quiet Hours
            Text(
                text = strings.settingsDndSection,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            SettingCard(glassBg, glassBorder) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingRow(
                        icon = Icons.Default.Bedtime,
                        title = strings.settingsDndTitle,
                        subtitle = strings.settingsDndSubtitle(wakeUpTime, bedTime)
                    ) {
                        Switch(
                            checked = isDndEnabled,
                            onCheckedChange = { isDndEnabled = it }
                        )
                    }

                    HorizontalDivider(color = Color(0x15FFFFFF))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                            Text(strings.settingsWakeTime, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(wakeUpTime, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
                            Text(strings.settingsBedTime, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(bedTime, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    }
                }
            }

            // Group 3: Privacy & GDPR UMP
            Text(
                text = strings.settingsPrivacySection,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )

            SettingCard(glassBg, glassBorder) {
                SettingRow(
                    icon = Icons.Default.Security,
                    title = strings.settingsPrivacyTitle,
                    subtitle = strings.settingsPrivacyDesc
                ) {
                    OutlinedButton(onClick = { showGdprToast = true }) {
                        Text(strings.manage)
                    }
                }
            }

            if (showGdprToast) {
                AlertDialog(
                    onDismissRequest = { showGdprToast = false },
                    title = { Text(strings.settingsPrivacyTitle, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(strings.settingsPrivacyDesc)
                    },
                    confirmButton = {
                        TextButton(onClick = { showGdprToast = false }) {
                            Text(strings.close)
                        }
                    }
                )
            }

            // Language Selection Dialog
            if (showLanguagePicker) {
                AlertDialog(
                    onDismissRequest = { showLanguagePicker = false },
                    title = {
                        Text(strings.settingsLanguageLabel, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SupportedLanguage.entries.forEach { lang ->
                                val isCurrent = lang.code.equals(currentLanguageCode, ignoreCase = true)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCurrent) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isCurrent) CyanPrimary else Color(0x22FFFFFF),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            onSelectLanguage(lang.code)
                                            showLanguagePicker = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(lang.flag, fontSize = 20.sp)
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent) CyanPrimary else MaterialTheme.colorScheme.onBackground
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isCurrent) {
                                        Text("✓", fontWeight = FontWeight.Bold, color = CyanPrimary)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showLanguagePicker = false }) {
                            Text(strings.close)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingCard(
    glassBg: Color,
    glassBorder: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        trailing()
    }
}
