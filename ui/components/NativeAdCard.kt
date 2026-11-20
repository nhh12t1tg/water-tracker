package com.watertracker.app.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.watertracker.app.ads.AdMobManager
import com.watertracker.app.ui.theme.*

/**
 * NativeAdCard - Seamlessly blends Google AdMob Native Ads into the application UI.
 * Strictly uses official Google Sample Test Ad Unit ID: ca-app-pub-3940256099942544/2247696110
 */
@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.NATIVE_TEST_ID,
    adSponsoredLabel: String = "Được tài trợ"
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isFailed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        AdMobManager.loadNativeAd(
            context = context,
            adUnitId = adUnitId,
            onAdLoaded = { ad ->
                nativeAd = ad
            },
            onAdFailed = {
                isFailed = true
            }
        )
        onDispose {
            nativeAd?.destroy()
        }
    }

    if (isFailed) {
        return
    }

    val cardBg = if (isDark) GlassDarkBackground else GlassLightBackground
    val cardBorder = if (isDark) GlassDarkBorder else GlassLightBorder

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        val currentAd = nativeAd
        if (currentAd != null) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                factory = { ctx ->
                    val nativeAdView = NativeAdView(ctx)

                    val rootLayout = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }

                    // 1. Top Bar: Ad Badge + "Được tài trợ" / "Sponsored"
                    val topRow = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val badge = TextView(ctx).apply {
                        text = "AD"
                        textSize = 10f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(AndroidColor.WHITE)
                        setPadding(14, 4, 14, 4)
                        val badgeBg = GradientDrawable().apply {
                            setColor(AndroidColor.parseColor("#06B6D4"))
                            cornerRadius = 8f
                        }
                        background = badgeBg
                    }

                    val sponsoredText = TextView(ctx).apply {
                        text = " $adSponsoredLabel"
                        textSize = 12f
                        setTextColor(if (isDark) AndroidColor.parseColor("#94A3B8") else AndroidColor.parseColor("#64748B"))
                        setPadding(10, 0, 0, 0)
                    }

                    topRow.addView(badge)
                    topRow.addView(sponsoredText)
                    rootLayout.addView(topRow)

                    val spacer1 = View(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            16
                        )
                    }
                    rootLayout.addView(spacer1)

                    // 2. Content Row: Icon + Headline + Body
                    val contentRow = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    val iconView = ImageView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(110, 110).apply {
                            setMargins(0, 0, 24, 0)
                        }
                        scaleType = ImageView.ScaleType.FIT_CENTER
                    }

                    val textCol = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    val headlineView = TextView(ctx).apply {
                        textSize = 15f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(if (isDark) AndroidColor.WHITE else AndroidColor.parseColor("#0F172A"))
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                    }

                    val bodyView = TextView(ctx).apply {
                        textSize = 12f
                        setTextColor(if (isDark) AndroidColor.parseColor("#94A3B8") else AndroidColor.parseColor("#475569"))
                        maxLines = 2
                        ellipsize = TextUtils.TruncateAt.END
                        setPadding(0, 4, 0, 0)
                    }

                    textCol.addView(headlineView)
                    textCol.addView(bodyView)

                    contentRow.addView(iconView)
                    contentRow.addView(textCol)
                    rootLayout.addView(contentRow)

                    val spacer2 = View(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            16
                        )
                    }
                    rootLayout.addView(spacer2)

                    // 3. CTA Action Button
                    val ctaButton = Button(ctx).apply {
                        textSize = 13f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(AndroidColor.WHITE)
                        val btnBg = GradientDrawable().apply {
                            setColor(AndroidColor.parseColor("#06B6D4"))
                            cornerRadius = 24f
                        }
                        background = btnBg
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            100
                        )
                    }
                    rootLayout.addView(ctaButton)

                    nativeAdView.addView(rootLayout)

                    nativeAdView.headlineView = headlineView
                    nativeAdView.bodyView = bodyView
                    nativeAdView.callToActionView = ctaButton
                    nativeAdView.iconView = iconView

                    nativeAdView
                },
                update = { view ->
                    val headline = view.headlineView as? TextView
                    val body = view.bodyView as? TextView
                    val cta = view.callToActionView as? Button
                    val icon = view.iconView as? ImageView

                    headline?.text = currentAd.headline ?: "Khám phá ứng dụng hay"
                    body?.text = currentAd.body ?: "Trải nghiệm ngay những tính năng tuyệt vời từ đối tác"
                    cta?.text = currentAd.callToAction ?: "Cài đặt ngay"

                    val iconDrawable = currentAd.icon?.drawable
                    if (iconDrawable != null && icon != null) {
                        icon.setImageDrawable(iconDrawable)
                        icon.visibility = View.VISIBLE
                    } else {
                        icon?.visibility = View.GONE
                    }

                    view.setNativeAd(currentAd)
                }
            )
        } else {
            // Elegant loading preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyanPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AD", fontWeight = FontWeight.Bold, color = CyanPrimary, fontSize = 12.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đối tác tài trợ của AquaWave",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Khám phá các sản phẩm và tiện ích nổi bật",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
