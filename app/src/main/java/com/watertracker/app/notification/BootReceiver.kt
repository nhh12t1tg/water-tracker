package com.watertracker.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            ReminderManager.createNotificationChannel(context)
            ReminderManager.scheduleNextReminder(context)
        }
    }
}
