package com.watertracker.app.data

import android.content.Context
import android.content.SharedPreferences
import com.watertracker.app.ui.screens.DayLogItem
import java.text.SimpleDateFormat
import java.util.*

class WaterRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("water_tracker_store", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
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
            val lastConsumed = prefs.getInt("today_consumed_ml", 0)
            val dailyGoal = getDailyGoalMl()
            val currentStreak = getStreakDays()

            // Archive last active date's progress
            prefs.edit().putInt("history_$lastDate", lastConsumed).apply()

            // Calculate calendar days elapsed
            val daysDiff = try {
                val lastDateObj = dateFormat.parse(lastDate)
                val todayDateObj = dateFormat.parse(today)
                if (lastDateObj != null && todayDateObj != null) {
                    val diffTime = todayDateObj.time - lastDateObj.time
                    (diffTime / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                } else 1
            } catch (_: Exception) {
                1
            }

            // Calculate how many days were missed
            val missedDays = if (lastConsumed >= dailyGoal) {
                // Goal was achieved on last active date
                (daysDiff - 1).coerceAtLeast(0)
            } else {
                // Goal was missed on last active date as well
                daysDiff
            }

            // Evaluate streak protection via Freeze Shields
            if (missedDays > 0) {
                val freezes = getAvailableFreezes()
                if (currentStreak > 0 && freezes >= missedDays) {
                    // Consume shields to protect streak
                    prefs.edit()
                        .putInt("available_freezes", freezes - missedDays)
                        .apply()
                } else {
                    // Consume any available shields used before the streak broke, then reset streak
                    val remainingFreezes = if (currentStreak > 0) {
                        (freezes - missedDays).coerceAtLeast(0)
                    } else {
                        freezes
                    }
                    prefs.edit()
                        .putInt("available_freezes", remainingFreezes)
                        .putInt("current_streak", 0)
                        .apply()
                }
            }

            // Reset today's volume and update date
            prefs.edit()
                .putString("last_active_date", today)
                .putInt("today_consumed_ml", 0)
                .putBoolean("goal_dialog_shown_today", false)
                .putBoolean("half_goal_ad_shown_today", false)
                .putBoolean("achieved_today", false)
                .putInt("post_goal_adds_count", 0)
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

    fun isHalfGoalAdShownToday(): Boolean {
        return prefs.getBoolean("half_goal_ad_shown_today", false)
    }

    fun setHalfGoalAdShownToday(shown: Boolean = true) {
        prefs.edit().putBoolean("half_goal_ad_shown_today", shown).apply()
    }

    fun getPostGoalAddsCount(): Int {
        return prefs.getInt("post_goal_adds_count", 0)
    }

    fun incrementPostGoalAddsCount(): Int {
        val current = getPostGoalAddsCount() + 1
        prefs.edit().putInt("post_goal_adds_count", current).apply()
        return current
    }

    fun resetPostGoalAddsCount() {
        prefs.edit().putInt("post_goal_adds_count", 0).apply()
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

    fun isPersonalizedAdsEnabled(): Boolean {
        return prefs.getBoolean("personalized_ads_enabled", true)
    }

    fun setPersonalizedAdsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("personalized_ads_enabled", enabled).apply()
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

    fun getMonthlyHistory(): List<DayLogItem> {
        val calendar = Calendar.getInstance()
        val goal = getDailyGoalMl()
        val result = mutableListOf<DayLogItem>()

        for (i in 29 downTo 0) {
            val cal = calendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(cal.time)
            val dayLabel = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"

            val consumed = if (i == 0) {
                getTodayConsumedMl()
            } else {
                prefs.getInt("history_$dateStr", 0)
            }

            result.add(
                DayLogItem(
                    dayName = dayLabel,
                    intakeMl = consumed,
                    targetMl = goal,
                    isToday = (i == 0)
                )
            )
        }
        return result
    }

    fun calculateAnalytics(data: List<DayLogItem>): DetailedAnalytics {
        val totalMl = data.sumOf { it.intakeMl }
        val targetTotalMl = data.sumOf { it.targetMl }
        val completionRate = if (targetTotalMl > 0) {
            Math.round(totalMl.toFloat() * 100f / targetTotalMl.toFloat())
        } else 0

        var peakMl = 0
        var peakName = ""
        var achievedDays = 0

        data.forEach { item ->
            if (item.intakeMl > peakMl) {
                peakMl = item.intakeMl
                peakName = item.dayName
            }
            if (item.intakeMl >= item.targetMl && item.targetMl > 0) {
                achievedDays++
            }
        }

        val avg = if (data.isNotEmpty()) totalMl / data.size else 0

        return DetailedAnalytics(
            totalMl = totalMl,
            targetTotalMl = targetTotalMl,
            completionRatePercent = completionRate,
            peakDayMl = peakMl,
            peakDayName = peakName,
            achievedDaysCount = achievedDays,
            totalDaysCount = data.size,
            averageDailyMl = avg
        )
    }

    fun resetForTesting() {
        prefs.edit().clear().apply()
    }
}

data class DetailedAnalytics(
    val totalMl: Int,
    val targetTotalMl: Int,
    val completionRatePercent: Int,
    val peakDayMl: Int,
    val peakDayName: String,
    val achievedDaysCount: Int,
    val totalDaysCount: Int,
    val averageDailyMl: Int
)
