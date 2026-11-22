package com.watertracker.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ReminderManager.ACTION_REMIND_HYDRATION) {
            ReminderManager.showReminderNotification(context)
            // Schedule the next reminder
            ReminderManager.scheduleNextReminder(context)
        }
    }
}
