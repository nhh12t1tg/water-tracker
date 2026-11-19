package com.watertracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.watertracker.app.ui.screens.HomeScreen
import com.watertracker.app.ui.theme.WaterTrackerTheme
import java.util.Collections

class MainActivity : ComponentActivity() {

    private lateinit var consentInformation: ConsentInformation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Google UMP (GDPR / European User Consent)
        requestUMPUserConsent()

        // 2. Set Compose Content
        setContent {
            WaterTrackerTheme {
                HomeScreen()
            }
        }
    }

    private fun requestUMPUserConsent() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { formError ->
                    if (consentInformation.canRequestAds()) {
                        initializeAdMobSdk()
                    }
                }
            },
            { requestConsentError ->
                // Even on consent update error, initialize SDK if allowed
                if (consentInformation.canRequestAds()) {
                    initializeAdMobSdk()
                }
            }
        )

        // Check if ads can already be requested
        if (consentInformation.canRequestAds()) {
            initializeAdMobSdk()
        }
    }

    private fun initializeAdMobSdk() {
        // Enforce test device configuration for development safety
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("EMULATOR"))
            .build()
        MobileAds.setRequestConfiguration(configuration)
        MobileAds.initialize(this) {}
    }
}
