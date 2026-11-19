package com.watertracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.i18n.EnglishStrings
import com.watertracker.app.ui.i18n.Strings
import com.watertracker.app.ui.theme.CyanPrimary
import com.watertracker.app.ui.theme.GlassDarkBackground
import com.watertracker.app.ui.theme.GlassLightBackground

data class BeverageItem(
    val id: String,
    val name: String,
    val icon: String,
    val hydrationFactor: Float,
    val description: String = ""
)

fun getScientificBeverages(strings: Strings): List<BeverageItem> {
    return listOf(
        BeverageItem("water", strings.beverageWater, "💧", 1.0f, "BHI: 100%"),
        BeverageItem("tea", strings.beverageTea, "🍵", 0.85f, "BHI: 85%"),
        BeverageItem("coffee", strings.beverageCoffee, "☕", 0.60f, "BHI: 60%"),
        BeverageItem("juice", strings.beverageJuice, "🧃", 0.80f, "BHI: 80%"),
        BeverageItem("milk", strings.beverageMilk, "🥛", 0.90f, "BHI: 90%"),
        BeverageItem("electrolytes", strings.beverageElectrolytes, "⚡", 1.0f, "BHI: 100%")
    )
}

val SCIENTIFIC_BEVERAGES = getScientificBeverages(EnglishStrings)

@Composable
fun BeverageSelectorBar(
    selectedBeverage: BeverageItem,
    onSelectBeverage: (BeverageItem) -> Unit,
    strings: Strings = EnglishStrings,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val defaultBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val beverages = getScientificBeverages(strings)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        beverages.forEach { beverage ->
            val isSelected = beverage.id == selectedBeverage.id
            val bgColor = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else defaultBg
            val borderColor = if (isSelected) CyanPrimary else Color(0x22FFFFFF)

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectBeverage(beverage) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = beverage.icon, fontSize = 14.sp)
                Text(
                    text = beverage.name,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
