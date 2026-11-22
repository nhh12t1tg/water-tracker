package com.watertracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.theme.CyanPrimary
import com.watertracker.app.ui.theme.DeepBlueCard

@Composable
fun CustomWaterDialog(
    selectedBeverage: BeverageItem,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    strings: Strings = EnglishStrings
) {
    val isDark = isSystemInDarkTheme()
    var textValue by remember { mutableStateOf("300") }
    val parsedMl = textValue.toIntOrNull() ?: 0
    val effectiveMl = Math.round(parsedMl * selectedBeverage.hydrationFactor)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) DeepBlueCard else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Title with Beverage Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(selectedBeverage.icon, fontSize = 24.sp)
                    Text(
                        text = strings.customDialogTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 19.sp
                        )
                    )
                }

                // Selected Beverage Info
                Text(
                    text = strings.customDialogSelected(
                        selectedBeverage.name,
                        (selectedBeverage.hydrationFactor * 100).toInt()
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Input TextField
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            textValue = input
                        }
                    },
                    label = { Text(strings.customDialogInputLabel) },
                    suffix = { Text("ml", fontWeight = FontWeight.SemiBold) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick preset pills: 200ml, 330ml, 450ml, 700ml (Equal 25% weight, singleLine, never wrap)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(200, 330, 450, 700).forEach { preset ->
                        val isSelected = parsedMl == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) CyanPrimary.copy(alpha = 0.2f)
                                    else (if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) CyanPrimary else (if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { textValue = preset.toString() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${preset}ml",
                                maxLines = 1,
                                softWrap = false,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // Effective Hydration Feedback Pill
                if (parsedMl > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CyanPrimary.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.customDialogNetHydration(effectiveMl),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                fontSize = 13.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // BALANCED 2-BUTTON ROW (Equal 50/50 width, 48dp height, high contrast, non-wrapping)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel Button: Neutral solid slate grey background, high contrast text
                    Button(
                        onClick = onDismiss,
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
                            if (parsedMl > 0) {
                                onConfirm(parsedMl)
                            }
                        },
                        enabled = parsedMl > 0,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = CyanPrimary.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = strings.confirm,
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
