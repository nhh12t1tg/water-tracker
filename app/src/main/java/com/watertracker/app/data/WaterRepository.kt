package com.watertracker.app.data

import android.content.Context
import android.content.SharedPreferences
import com.watertracker.app.ui.screens.DayLogItem
import java.text.SimpleDateFormat
import java.util.*

class WaterRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("water_tracker_store", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayNameFormat = SimpleDateFormat("EEE", Locale("vi", "VN"))

    init {
        checkAndPerformMidnightRollover()
    }

    private fun getTodayDateStr(): String {
        return dateFormat.format(Date())
    }

    /**
     * Check if day has changed since last session. If yes, evaluate streak and reset today's intake.
     */
    fun checkAndPerformMidnightRollover() {
        val today = getTodayDateStr()
        val lastDate = prefs.getString("last_active_date", null)

        if (lastDate == null) {
            // First run
            prefs.edit().putString("last_active_date", today).apply()
            return
        }

        if (lastDate != today) {
            // Day rollover occurred!
            val yesterdayConsumed = prefs.getInt("today_consumed_ml", 0)
            val dailyGoal = getDailyGoalMl()
            val currentStreak = getStreakDays()

            // Archive yesterday's progress
            prefs.edit().putInt("history_$lastDate", yesterdayConsumed).apply()

            // Evaluate streak: Did user achieve goal yesterday?
            if (yesterdayConsumed >= dailyGoal) {
                // Streak maintained
            } else {
                // Missed goal! Check if streak freeze is available
                val freezes = getAvailableFreezes()
                if (freezes > 0 && currentStreak > 0) {
                    // Consume 1 freeze
                    prefs.edit()
                        .putInt("available_freezes", freezes - 1)
                        .apply()
                } else {
                    // Broken streak
                    prefs.edit().putInt("current_streak", 0).apply()
                }
            }

            // Reset today's volume and update date
            prefs.edit()
                .putString("last_active_date", today)
                .putInt("today_consumed_ml", 0)
                .putBoolean("goal_dialog_shown_today", false)
                .apply()
        }
    }

    fun isOnboarded(): Boolean {
        return prefs.getBoolean("is_onboarded", false)
    }

    fun saveOnboardingProfile(
        weightKg: Float,
        gender: String,
        activity: String,
        customGoalMl: Int? = null
    ) {
        val multiplier = if (gender == "male") 35 else 31
        val activityOffset = when (activity) {
            "moderate" -> 350
            "active" -> 700
            else -> 0
        }
        val calculatedGoal = customGoalMl ?: Math.round(weightKg * multiplier + activityOffset)

        prefs.edit()
            .putBoolean("is_onboarded", true)
            .putFloat("user_weight_kg", weightKg)
            .putString("user_gender", gender)
            .putString("user_activity", activity)
            .putInt("daily_goal_ml", calculatedGoal)
            .putInt("current_streak", 0)
            .putInt("today_consumed_ml", 0)
            .putInt("available_freezes", 1)
            .putString("last_active_date", getTodayDateStr())
            .apply()
    }

    fun getAppLanguage(): String {
        return prefs.getString("app_language", "en") ?: "en"
    }

    fun setAppLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
    }

    fun isGoalDialogShownToday(): Boolean {
        return prefs.getBoolean("goal_dialog_shown_today", false)
    }

    fun setGoalDialogShownToday(shown: Boolean = true) {
        prefs.edit().putBoolean("goal_dialog_shown_today", shown).apply()
    }

    fun getReminderIntervalMinutes(): Int {
        return prefs.getInt("reminder_interval_minutes", 90)
    }

    fun setReminderIntervalMinutes(minutes: Int) {
        prefs.edit().putInt("reminder_interval_minutes", minutes).apply()
    }

    fun isDndEnabled(): Boolean {
        return prefs.getBoolean("is_dnd_enabled", true)
    }

    fun setDndEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("is_dnd_enabled", enabled).apply()
    }

    fun getWakeUpTime(): String {
        return prefs.getString("dnd_wake_up_time", "07:00") ?: "07:00"
    }

    fun setWakeUpTime(time: String) {
        prefs.edit().putString("dnd_wake_up_time", time).apply()
    }

    fun getBedTime(): String {
        return prefs.getString("dnd_bed_time", "22:00") ?: "22:00"
    }

    fun setBedTime(time: String) {
        prefs.edit().putString("dnd_bed_time", time).apply()
    }

    fun getDailyGoalMl(): Int {
        return prefs.getInt("daily_goal_ml", 2500)
    }

    fun getTodayConsumedMl(): Int {
        checkAndPerformMidnightRollover()
        return prefs.getInt("today_consumed_ml", 0)
    }

    fun addWaterLog(rawMl: Int, effectiveMl: Int): Int {
        checkAndPerformMidnightRollover()
        val current = getTodayConsumedMl()
        val newTotal = current + effectiveMl
        val goal = getDailyGoalMl()
        val streak = getStreakDays()

        val editor = prefs.edit()
        editor.putInt("today_consumed_ml", newTotal)

        // Increment streak if goal reached for the first time today
        val alreadyAchievedToday = prefs.getBoolean("achieved_today", false)
        if (newTotal >= goal && !alreadyAchievedToday) {
            editor.putInt("current_streak", streak + 1)
            editor.putBoolean("achieved_today", true)
        }

        editor.apply()
        return newTotal
    }

    fun getStreakDays(): Int {
        return prefs.getInt("current_streak", 0)
    }

    fun getAvailableFreezes(): Int {
        return prefs.getInt("available_freezes", 1)
    }

    fun addStreakFreeze() {
        val current = getAvailableFreezes()
        prefs.edit().putInt("available_freezes", current + 1).apply()
    }

    fun getActiveThemeIndex(): Int {
        return prefs.getInt("active_theme_index", 0)
    }

    fun setActiveThemeIndex(index: Int) {
        prefs.edit().putInt("active_theme_index", index).apply()
    }

    fun getWeeklyHistory(daysOfWeek: List<String>? = null): List<DayLogItem> {
        val calendar = Calendar.getInstance()
        val goal = getDailyGoalMl()
        val result = mutableListOf<DayLogItem>()
        val defaultDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val labels = if (daysOfWeek != null && daysOfWeek.size == 7) daysOfWeek else defaultDays

        for (i in 6 downTo 0) {
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(cal.time)
            val dayOfWeekIndex = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            val dayName = labels[dayOfWeekIndex]

            val consumed = if (i == 0) {
                getTodayConsumedMl()
            } else {
                prefs.getInt("history_$dateStr", 0)
            }

            result.add(
                DayLogItem(
                    dayName = dayName,
                    intakeMl = consumed,
                    targetMl = goal,
                    isToday = (i == 0)
                )
            )
        }
        return result
    }

    fun resetForTesting() {
        prefs.edit().clear().apply()
    }
}
