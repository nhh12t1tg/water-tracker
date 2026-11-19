package com.watertracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.theme.CyanPrimary

@Composable
fun CustomWaterDialog(
    selectedBeverage: BeverageItem,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    strings: Strings = EnglishStrings
) {
    var textValue by remember { mutableStateOf("300") }
    val parsedMl = textValue.toIntOrNull() ?: 0
    val effectiveMl = Math.round(parsedMl * selectedBeverage.hydrationFactor)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(selectedBeverage.icon, fontSize = 22.sp)
                Text(strings.customDialogTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.customDialogSelected(
                        selectedBeverage.name,
                        (selectedBeverage.hydrationFactor * 100).toInt()
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            textValue = input
                        }
                    },
                    label = { Text(strings.customDialogInputLabel) },
                    suffix = { Text("ml") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick preset pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(200, 330, 450, 700).forEach { preset ->
                        FilterChip(
                            selected = parsedMl == preset,
                            onClick = { textValue = preset.toString() },
                            label = { Text("${preset}ml", fontSize = 11.sp) }
                        )
                    }
                }

                if (parsedMl > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = CyanPrimary.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.customDialogNetHydration(effectiveMl),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedMl > 0) {
                        onConfirm(parsedMl)
                    }
                },
                enabled = parsedMl > 0,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text(strings.confirm)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
