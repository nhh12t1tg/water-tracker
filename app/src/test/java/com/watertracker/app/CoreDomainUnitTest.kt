package com.watertracker.app

import com.watertracker.app.ads.AdMobManager
import com.watertracker.app.notification.ReminderManager
import com.watertracker.app.ui.i18n.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreDomainUnitTest {

    @Test
    fun `isQuietHours handles overnight DND boundaries accurately`() {
        assertTrue(ReminderManager.isQuietHours("22:00", "07:00", "22:00"))
        assertTrue(ReminderManager.isQuietHours("23:30", "07:00", "22:00"))
        assertTrue(ReminderManager.isQuietHours("00:00", "07:00", "22:00"))
        assertTrue(ReminderManager.isQuietHours("06:59", "07:00", "22:00"))

        assertFalse(ReminderManager.isQuietHours("07:00", "07:00", "22:00"))
        assertFalse(ReminderManager.isQuietHours("14:30", "07:00", "22:00"))
        assertFalse(ReminderManager.isQuietHours("21:59", "07:00", "22:00"))
    }

    @Test
    fun `isQuietHours handles daytime sleep schedule`() {
        assertTrue(ReminderManager.isQuietHours("09:00", "17:00", "09:00"))
        assertTrue(ReminderManager.isQuietHours("13:15", "17:00", "09:00"))
        assertFalse(ReminderManager.isQuietHours("17:00", "17:00", "09:00"))
        assertFalse(ReminderManager.isQuietHours("22:00", "17:00", "09:00"))
    }

    @Test
    fun `AdMobManager strictly enforces Google Official Test Ad Unit IDs`() {
        assertEquals("ca-app-pub-3940256099942544/6300978111", AdMobManager.BANNER_TEST_ID)
        assertEquals("ca-app-pub-3940256099942544/1033173712", AdMobManager.INTERSTITIAL_TEST_ID)
        assertEquals("ca-app-pub-3940256099942544/5224354917", AdMobManager.REWARDED_TEST_ID)
        assertEquals("ca-app-pub-3940256099942544/2247696110", AdMobManager.NATIVE_TEST_ID)
    }

    @Test
    fun `LocalizationManager returns valid strings for all 5 supported languages`() {
        val codes = listOf("en", "vi", "es", "ja", "de")
        codes.forEach { code ->
            val strings = LocalizationManager.getStrings(code)
            assertTrue("Missing notificationTitle for $code", strings.notificationTitle.isNotBlank())
            assertTrue("Missing notificationBody percent for $code", strings.notificationBody(50).contains("50%"))
            assertTrue("Missing settingsWeightLabel for $code", strings.settingsWeightLabel.isNotBlank())
            assertTrue("Missing settingsActivityLabel for $code", strings.settingsActivityLabel.isNotBlank())
            assertTrue("Missing settingsEditWeightPreview ml for $code", strings.settingsEditWeightPreview(2500).contains("2500"))
        }
    }
}
