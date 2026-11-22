package com.watertracker.app.ui.screens

import android.app.Activity
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import com.google.android.ump.UserMessagingPlatform
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.data.WaterRepository
import com.watertracker.app.notification.ReminderManager
import com.watertracker.app.ui.components.NativeAdCard
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.i18n.SupportedLanguage
import com.watertracker.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    strings: Strings = EnglishStrings,
    currentLanguageCode: String = "en",
    onSelectLanguage: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { WaterRepository(context) }
    val isDark = isSystemInDarkTheme()
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    var isDndEnabled by remember { mutableStateOf(repository.isDndEnabled()) }
    var intervalMinutes by remember { mutableIntStateOf(repository.getReminderIntervalMinutes()) }
    var wakeUpTime by remember { mutableStateOf(repository.getWakeUpTime()) }
    var bedTime by remember { mutableStateOf(repository.getBedTime()) }
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showGdprToast by remember { mutableStateOf(false) }
    var isPersonalizedAds by remember { mutableStateOf(repository.isPersonalizedAdsEnabled()) }

    // Dismiss language picker if back button is pressed while picker dialog is open
    BackHandler(enabled = showLanguagePicker) {
        showLanguagePicker = false
    }

    val showTimePicker = { isWakeUp: Boolean ->
        val currentStr = if (isWakeUp) wakeUpTime else bedTime
        val parts = currentStr.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: (if (isWakeUp) 7 else 22)
        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formatted = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                if (isWakeUp) {
                    wakeUpTime = formatted
                    repository.setWakeUpTime(formatted)
                    ReminderManager.scheduleNextReminder(context)
                } else {
                    bedTime = formatted
                    repository.setBedTime(formatted)
                    ReminderManager.scheduleNextReminder(context)
                }
            },
            initialHour,
            initialMinute,
            true
        ).show()
    }

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
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = strings.settingsLanguageLabel,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${currentLang.flag} ${currentLang.displayName}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showLanguagePicker = true },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "${currentLang.flag} ${currentLang.code.uppercase()}",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
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
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.settingsReminderFrequency,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = strings.settingsReminderFreqFormat(intervalMinutes),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60, 90, 120).forEach { mins ->
                            FilterChip(
                                selected = intervalMinutes == mins,
                                onClick = {
                                    intervalMinutes = mins
                                    repository.setReminderIntervalMinutes(mins)
                                    ReminderManager.scheduleNextReminder(context)
                                },
                                modifier = Modifier.weight(1f),
                                label = {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${mins}m",
                                            fontWeight = if (intervalMinutes == mins) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
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
                            onCheckedChange = {
                                isDndEnabled = it
                                repository.setDndEnabled(it)
                                ReminderManager.scheduleNextReminder(context)
                            }
                        )
                    }

                    HorizontalDivider(color = Color(0x15FFFFFF))

                    // Clickable Wake-up time picker row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showTimePicker(true) }
                            .padding(vertical = 6.dp, horizontal = 2.dp),
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(wakeUpTime, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanPrimary, modifier = Modifier.size(13.dp))
                            }
                        }
                    }

                    // Clickable Bedtime picker row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showTimePicker(false) }
                            .padding(vertical = 6.dp, horizontal = 2.dp),
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(bedTime, fontWeight = FontWeight.Bold, color = CyanPrimary)
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyanPrimary, modifier = Modifier.size(13.dp))
                            }
                        }
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

            // Google AdMob Native Ad Card (Cleanly placed at bottom of Settings)
            NativeAdCard(
                adSponsoredLabel = if (strings.cancel == "Hủy") "Được tài trợ" else "Sponsored"
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (showGdprToast) {
                AlertDialog(
                    onDismissRequest = { showGdprToast = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = CyanPrimary)
                            Text(strings.settingsPrivacyTitle, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // 1. Personalized Ads Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.privacyPersonalizedAds,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = if (isPersonalizedAds) strings.privacyPersonalizedOn else strings.privacyPersonalizedOff,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                                Switch(
                                    checked = isPersonalizedAds,
                                    onCheckedChange = {
                                        isPersonalizedAds = it
                                        repository.setPersonalizedAdsEnabled(it)
                                    }
                                )
                            }

                            HorizontalDivider(color = Color(0x15FFFFFF))

                            // 2. 100% Local Storage Info Card
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = strings.privacyLocalStorageTitle,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CyanPrimary)
                                    )
                                    Text(
                                        text = strings.privacyLocalStorageDesc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // 3. Official Google UMP Form Button
                            OutlinedButton(
                                onClick = {
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        UserMessagingPlatform.showPrivacyOptionsForm(activity) { _ -> }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(strings.privacyGoogleFormButton, fontSize = 12.sp)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showGdprToast = false }) {
                            Text(strings.close, fontWeight = FontWeight.Bold)
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
