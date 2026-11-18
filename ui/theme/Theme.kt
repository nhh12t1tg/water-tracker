package com.watertracker.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CyanLight,
    onPrimary = DeepBlueDark,
    secondary = MintLight,
    onSecondary = DeepBlueDark,
    background = DeepBlueDark,
    onBackground = Color(0xFFE2E8F0),
    surface = DeepBlueCard,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    tertiary = GoldReward
)

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.White,
    secondary = MintAccent,
    onSecondary = Color.White,
    background = SkyBlueLight,
    onBackground = Color(0xFF0F172A),
    surface = WhiteCard,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE0F2FE),
    onSurfaceVariant = Color(0xFF475569),
    tertiary = GoldReward
)

@Composable
fun WaterTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
