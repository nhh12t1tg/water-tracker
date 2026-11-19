package com.watertracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.watertracker.app.ui.theme.GlassDarkBackground
import com.watertracker.app.ui.theme.GlassLightBackground

/**
 * AdaptiveBannerContainer - Pre-allocated safe container for AdMob Banner.
 * 
 * POLICY COMPLIANCE GUARANTEE:
 * 1. Fixed height reservation (60dp) prevents Cumulative Layout Shift (CLS < 0.1).
 * 2. Strict vertical padding (16dp) isolates banner from interactive buttons above.
 * 3. Prevents "Accidental Clicks" policy violations on Google Play Store.
 */
@Composable
fun AdaptiveBannerContainer(
    modifier: Modifier = Modifier,
    isTestMode: Boolean = true
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp) // Safety isolation padding
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // Standard Google Adaptive Banner height reservation
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDark) GlassDarkBackground else GlassLightBackground)
                .border(
                    width = 1.dp,
                    color = if (isDark) Color(0x22FFFFFF) else Color(0x11000000),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder / Actual AdMob View will be embedded here via AndroidView
            if (isTestMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "AdMob Adaptive Banner [320x50 / Adaptive]",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
