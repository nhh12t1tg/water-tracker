package com.watertracker.app

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.watertracker.app.data.WaterRepository
import com.watertracker.app.notification.ReminderManager
import com.watertracker.app.notification.ReminderReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationInstrumentedTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager
    private lateinit var repository: WaterRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        repository = WaterRepository(context)
        repository.saveOnboardingProfile(70f, "male", "moderate", 2500)
        repository.setAppLanguage("vi")
        // Disable DND for deterministic testing regardless of wall-clock time
        repository.setDndEnabled(false)
    }

    @Test
    fun testNotificationChannelAndPopupOnDevice() {
        ReminderManager.createNotificationChannel(context)
        val channel = notificationManager.getNotificationChannel(ReminderManager.CHANNEL_ID)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)

        // Log 500ml (20% of 2500ml)
        repository.addWaterLog(500, 500)

        // Simulate AlarmManager firing ReminderReceiver
        val receiver = ReminderReceiver()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderManager.ACTION_REMIND_HYDRATION
        }
        receiver.onReceive(context, intent)

        // Verify notification is actively displayed on the device status bar / heads-up
        val active = notificationManager.activeNotifications
        val posted = active.find { it.id == ReminderManager.NOTIFICATION_ID }
        assertTrue("Notification must be posted on the device", posted != null)
        assertEquals(ReminderManager.CHANNEL_ID, posted?.notification?.channelId)

        val extras = posted?.notification?.extras
        val title = extras?.getString("android.title")
        val text = extras?.getString("android.text")
        assertEquals("💧 Đến giờ uống nước rồi!", title)
        assertTrue("Body must contain 20%", text?.contains("20%") == true)
    }

    @Test
    fun testNotificationSuppressedAt100PercentGoal() {
        notificationManager.cancelAll()
        // Reach 100% goal (2500ml / 2500ml)
        repository.addWaterLog(2500, 2500)

        val receiver = ReminderReceiver()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderManager.ACTION_REMIND_HYDRATION
        }
        receiver.onReceive(context, intent)

        val active = notificationManager.activeNotifications
        val posted = active.find { it.id == ReminderManager.NOTIFICATION_ID }
        assertFalse("Notification must NOT be posted after reaching 100% daily goal", posted != null)
    }
}
