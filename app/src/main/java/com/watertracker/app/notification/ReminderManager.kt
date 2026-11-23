package com.watertracker.app.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.watertracker.app.MainActivity
import com.watertracker.app.R
import com.watertracker.app.data.WaterRepository
import com.watertracker.app.ui.i18n.LocalizationManager
import java.util.Calendar
import java.util.Locale

object ReminderManager {
    const val CHANNEL_ID = "hydration_reminders_channel"
    const val NOTIFICATION_ID = 1001
    const val ACTION_REMIND_HYDRATION = "com.watertracker.app.ACTION_REMIND_HYDRATION"
    private const val REQUEST_CODE_ALARM = 2001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Scheduled hydration reminder notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks whether currentTime (HH:mm) is within the quiet hours (bedTime -> wakeUpTime).
     */
    fun isQuietHours(currentTime: String, wakeUp: String, bed: String): Boolean {
        val currentMinutes = timeToMinutes(currentTime)
        val wakeUpMinutes = timeToMinutes(wakeUp)
        val bedMinutes = timeToMinutes(bed)

        return if (bedMinutes > wakeUpMinutes) {
            // Overnight quiet hours, e.g. 22:00 (1320m) to 07:00 (420m)
            currentMinutes >= bedMinutes || currentMinutes < wakeUpMinutes
        } else {
            // Daytime quiet hours
            currentMinutes in bedMinutes until wakeUpMinutes
        }
    }

    private fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    /**
     * Calculates timestamp in millis for the next wake-up time.
     */
    fun getNextWakeUpMillis(wakeUpStr: String): Long {
        val parts = wakeUpStr.split(":")
        val targetH = parts.getOrNull(0)?.toIntOrNull() ?: 7
        val targetM = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetH)
            set(Calendar.MINUTE, targetM)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }

    /**
     * Schedules the next reminder alarm based on:
     * 1. If 100% daily goal is already achieved -> Mutes/cancels reminders for the rest of today!
     * 2. If currently in DND quiet hours -> Schedules for next wake-up time.
     * 3. Otherwise -> Schedules after intervalMinutes (e.g. 60, 90, 120m).
     */
    fun scheduleNextReminder(context: Context) {
        val repository = WaterRepository(context)
        if (!repository.isOnboarded()) return

        val consumed = repository.getTodayConsumedMl()
        val goal = repository.getDailyGoalMl()

        // 1. If user already reached 100% daily target, cancel reminders for today!
        if (consumed >= goal && goal > 0) {
            cancelAllReminders(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND_HYDRATION
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM, intent, flags)

        val cal = Calendar.getInstance()
        val currentH = String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY))
        val currentM = String.format(Locale.US, "%02d", cal.get(Calendar.MINUTE))
        val currentTimeStr = "$currentH:$currentM"

        val wakeUp = repository.getWakeUpTime()
        val bed = repository.getBedTime()
        val isDnd = repository.isDndEnabled()

        val triggerAtMillis: Long

        if (isDnd && isQuietHours(currentTimeStr, wakeUp, bed)) {
            // During quiet hours -> Schedule for wake up
            triggerAtMillis = getNextWakeUpMillis(wakeUp)
        } else {
            val intervalMinutes = repository.getReminderIntervalMinutes().coerceAtLeast(30)
            val candidateMillis = System.currentTimeMillis() + intervalMinutes * 60 * 1000L

            // Check if candidate time falls into DND
            val candidateCal = Calendar.getInstance().apply { timeInMillis = candidateMillis }
            val candidateH = String.format(Locale.US, "%02d", candidateCal.get(Calendar.HOUR_OF_DAY))
            val candidateM = String.format(Locale.US, "%02d", candidateCal.get(Calendar.MINUTE))
            val candidateTimeStr = "$candidateH:$candidateM"

            if (isDnd && isQuietHours(candidateTimeStr, wakeUp, bed)) {
                triggerAtMillis = getNextWakeUpMillis(wakeUp)
            } else {
                triggerAtMillis = candidateMillis
            }
        }

        try {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /**
     * Schedules the next reminder only if no active alarm is currently pending.
     * Useful when resuming the app on a new day without pushing back an already-running timer.
     */
    fun ensureReminderScheduled(context: Context) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND_HYDRATION
        }
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val existing = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM, intent, flags)
        if (existing == null) {
            scheduleNextReminder(context)
        }
    }

    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMIND_HYDRATION
        }
        val flags = PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_ALARM, intent, flags)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    /**
     * Presents the reminder notification with high importance and interactive "+250ml" action.
     */
    fun showReminderNotification(context: Context) {
        val repository = WaterRepository(context)
        val consumed = repository.getTodayConsumedMl()
        val goal = repository.getDailyGoalMl()

        // Double check: Never notify if already reached 100%
        if (consumed >= goal && goal > 0) return

        // Double check: Never notify during DND quiet hours
        val cal = Calendar.getInstance()
        val currentH = String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY))
        val currentM = String.format(Locale.US, "%02d", cal.get(Calendar.MINUTE))
        val currentTimeStr = "$currentH:$currentM"
        if (repository.isDndEnabled() && isQuietHours(currentTimeStr, repository.getWakeUpTime(), repository.getBedTime())) {
            return
        }

        val percent = if (goal > 0) Math.round(consumed.toFloat() * 100f / goal.toFloat()) else 0
        val strings = LocalizationManager.getStrings(repository.getAppLanguage())

        // Content intent: open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_water)
            .setContentTitle(strings.notificationTitle)
            .setContentText(strings.notificationBody(percent))
            .setStyle(NotificationCompat.BigTextStyle().bigText(strings.notificationBody(percent)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Post notification permission not yet granted
        }
    }
}
