package com.watertracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.watertracker.app.ui.theme.*

/**
 * GoalCompletedDialog - Celebration dialog when 100% hydration is reached.
 * 
 * ADMOB POLICY COMPLIANCE:
 * This screen is an authorized NATURAL BREAK POINT.
 * When the user taps the primary CTA button "Tuyệt vời, tiếp tục!",
 * the Interstitial Ad is presented without surprising or interrupting the user.
 */
@Composable
fun GoalCompletedDialog(
    totalMl: Int,
    onDismissAndShowInterstitial: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val dialogBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val dialogBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    Dialog(onDismissRequest = onDismissAndShowInterstitial) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(dialogBg)
                .border(1.5.dp, dialogBorder, RoundedCornerShape(28.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trophy / Celebration Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(12.dp, CircleShape, ambientColor = GoldReward.copy(alpha = 0.5f))
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFBBF24), GoldReward))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Goal reached",
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "Hoàn Thành Mục Tiêu!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = "Bạn đã uống đủ $totalMl ml nước hôm nay. Cơ thể của bạn đang cảm ơn bạn đấy!",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Primary CTA Button (Triggers natural break Interstitial Ad)
                Button(
                    onClick = onDismissAndShowInterstitial,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "Tuyệt vời, tiếp tục!",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}
