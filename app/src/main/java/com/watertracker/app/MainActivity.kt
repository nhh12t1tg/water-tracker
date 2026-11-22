package com.watertracker.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.watertracker.app.ads.AdMobManager
import com.watertracker.app.notification.ReminderManager
import com.watertracker.app.ui.screens.HomeScreen
import com.watertracker.app.ui.theme.WaterTrackerTheme
import java.util.Collections

class MainActivity : ComponentActivity() {

    private lateinit var consentInformation: ConsentInformation

    companion object {
        private const val REQUEST_CODE_NOTIF_PERMISSION = 3001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Notification Channel & Reminders Initialization
        ReminderManager.createNotificationChannel(this)
        checkAndRequestNotificationPermission()

        // 2. Initialize Google UMP (GDPR / European User Consent)
        requestUMPUserConsent()

        // 3. Set Compose Content
        setContent {
            WaterTrackerTheme {
                HomeScreen()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIF_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ReminderManager.scheduleNextReminder(this)
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                ReminderManager.scheduleNextReminder(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIF_PERMISSION
                )
            }
        } else {
            ReminderManager.scheduleNextReminder(this)
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
        MobileAds.initialize(this) {
            AdMobManager.initialize(this)
        }
    }
}
