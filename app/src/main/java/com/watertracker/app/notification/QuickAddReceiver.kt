package com.watertracker.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.watertracker.app.data.WaterRepository

class QuickAddReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ReminderManager.ACTION_QUICK_ADD_250) {
            val repository = WaterRepository(context)
            val newTotal = repository.addWaterLog(250, 250)
            val target = repository.getDailyGoalMl()

            // Dismiss the active notification
            try {
                NotificationManagerCompat.from(context).cancel(ReminderManager.NOTIFICATION_ID)
            } catch (_: Exception) {
            }

            // If 100% target reached, cancel reminders for the day; otherwise reschedule smart delay
            if (newTotal >= target && target > 0) {
                ReminderManager.cancelAllReminders(context)
            } else {
                ReminderManager.scheduleNextReminder(context)
            }
        }
    }
}
