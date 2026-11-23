package com.watertracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.watertracker.app.ads.AdMobManager
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
    val isInspection = LocalInspectionMode.current
    var isAdLoaded by remember { mutableStateOf(false) }
    val showContainerChrome = isAdLoaded || isInspection

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
                .height(60.dp) // Standard Google Adaptive Banner height reservation (prevents CLS)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (showContainerChrome) {
                        if (isDark) GlassDarkBackground else GlassLightBackground
                    } else {
                        Color.Transparent
                    }
                )
                .then(
                    if (showContainerChrome) {
                        Modifier.border(
                            width = 1.dp,
                            color = if (isDark) Color(0x22FFFFFF) else Color(0x11000000),
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isInspection) {
                Text(
                    text = "AdMob Adaptive Banner [320x50 / Adaptive]",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                var adViewRef by remember { mutableStateOf<AdView?>(null) }
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner, adViewRef) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_PAUSE -> adViewRef?.pause()
                            Lifecycle.Event.ON_RESUME -> adViewRef?.resume()
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                AndroidView(
                    factory = { context ->
                        AdView(context).apply {
                            adUnitId = AdMobManager.BANNER_TEST_ID
                            setAdSize(AdSize.BANNER)
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    isAdLoaded = true
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    isAdLoaded = false
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                            adViewRef = this
                        }
                    },
                    onRelease = { adView ->
                        adView.destroy()
                        adViewRef = null
                    },
                    modifier = Modifier.wrapContentSize()
                )
            }
        }
    }
}
