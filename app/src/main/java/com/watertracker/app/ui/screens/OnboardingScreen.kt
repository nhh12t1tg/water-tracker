package com.watertracker.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.i18n.SupportedLanguage
import com.watertracker.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: (weightKg: Float, gender: String, activity: String, customGoalMl: Int?) -> Unit,
    strings: Strings = EnglishStrings,
    currentLanguageCode: String = "en",
    onSelectLanguage: (String) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val glassBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val glassBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    var selectedGender by remember { mutableStateOf("male") }
    var weightKg by remember { mutableFloatStateOf(65f) }
    var selectedActivity by remember { mutableStateOf("moderate") }
    var showLanguagePicker by remember { mutableStateOf(false) }

    // Live Scientific Calculation (EFSA Clinical Physiology Standards)
    val multiplier = if (selectedGender == "male") 35 else 31
    val activityOffset = when (selectedActivity) {
        "moderate" -> 350
        "active" -> 700
        else -> 0
    }
    val calculatedGoal = Math.round(weightKg * multiplier + activityOffset)

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(listOf(DeepBlueDark, Color(0xFF0F172A), DeepBlueCard))
    } else {
        Brush.verticalGradient(listOf(SkyBlueLight, Color(0xFFE0F2FE), Color(0xFFBAE6FD)))
    }

    Scaffold(
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
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Top Language Switcher Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val currentLang = SupportedLanguage.fromCode(currentLanguageCode)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(glassBg)
                            .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                            .clickable { showLanguagePicker = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(currentLang.flag, fontSize = 14.sp)
                        Text(
                            text = currentLang.code.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // App Brand Hero
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CyanPrimary, MintAccent)))
                        .shadow(12.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💧", fontSize = 32.sp)
                }

                Text(
                    text = strings.onboardingWelcome,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                )

                Text(
                    text = strings.onboardingSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // 1. Gender / Sex
                Text(
                    text = strings.genderTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GenderCard(
                        title = strings.genderMale,
                        icon = "👨",
                        formulaDesc = strings.genderMaleRate,
                        isSelected = selectedGender == "male",
                        onClick = { selectedGender = "male" },
                        modifier = Modifier.weight(1f)
                    )
                    GenderCard(
                        title = strings.genderFemale,
                        icon = "👩",
                        formulaDesc = strings.genderFemaleRate,
                        isSelected = selectedGender == "female",
                        onClick = { selectedGender = "female" },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2. Weight
                Text(
                    text = strings.weightTitle(weightKg.toInt()),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(glassBg)
                        .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Slider(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        valueRange = 35f..130f,
                        steps = 94,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanPrimary,
                            activeTrackColor = CyanPrimary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("35 kg", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${weightKg.toInt()} kg", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 14.sp)
                        Text("130 kg", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // 3. Physical Activity
                Text(
                    text = strings.activityTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActivityRow(
                        icon = Icons.Default.Spa,
                        title = strings.activitySedentary,
                        offsetText = "+0 ml",
                        isSelected = selectedActivity == "sedentary",
                        onClick = { selectedActivity = "sedentary" }
                    )
                    ActivityRow(
                        icon = Icons.Default.DirectionsRun,
                        title = strings.activityModerate,
                        offsetText = "+350 ml",
                        isSelected = selectedActivity == "moderate",
                        onClick = { selectedActivity = "moderate" }
                    )
                    ActivityRow(
                        icon = Icons.Default.FitnessCenter,
                        title = strings.activityActive,
                        offsetText = "+700 ml",
                        isSelected = selectedActivity == "active",
                        onClick = { selectedActivity = "active" }
                    )
                }

                // Live Result Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CyanPrimary.copy(alpha = 0.25f), MintAccent.copy(alpha = 0.25f))
                            )
                        )
                        .border(1.5.dp, CyanPrimary, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = strings.recommendedGoalTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = strings.recommendedGoalFormat(calculatedGoal),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanPrimary,
                            fontSize = 30.sp
                        )
                    )
                }

                // Big Start Button
                Button(
                    onClick = {
                        onComplete(weightKg, selectedGender, selectedActivity, calculatedGoal)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = CyanPrimary.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary
                    )
                ) {
                    Text(
                        text = strings.startTrackingCta,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
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
private fun GenderCard(
    title: String,
    icon: String,
    formulaDesc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else if (isDark) GlassDarkBackground else GlassLightBackground
    val borderColor = if (isSelected) CyanPrimary else if (isDark) GlassDarkBorder else GlassLightBorder

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 28.sp)
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(text = formulaDesc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActivityRow(
    icon: ImageVector,
    title: String,
    offsetText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else if (isDark) GlassDarkBackground else GlassLightBackground
    val borderColor = if (isSelected) CyanPrimary else if (isDark) GlassDarkBorder else GlassLightBorder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            )
        }
        Text(
            text = offsetText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        )
    }
}
