package com.traceapp.android.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** User preference is separate from the operating system notification permission. */
@Singleton
class TraceNotificationSettings @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = preferences.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    /** Daily reminder times as "HH:mm", in insertion order. */
    fun getReminderTimes(): List<String> {
        val stored = preferences.getStringSet(KEY_TIMES, null)
            ?: return DEFAULT_TIMES.toList()
        val sorted = stored.sortedWith(compareBy({ it.substring(0, 2) }, { it.substring(3, 5) }))
        return sorted
    }

    fun setReminderTimes(times: List<String>) {
        val cleaned = times.map { it.trim() }.filter { it.matches(TIME_PATTERN) }.distinct()
        preferences.edit().putStringSet(KEY_TIMES, cleaned.toSet()).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "trace_notification_settings"
        const val KEY_ENABLED = "notifications_enabled"
        const val KEY_TIMES = "reminder_times"
        val DEFAULT_TIMES = listOf("07:00", "13:00")
        val TIME_PATTERN = Regex("^([01]?\\d|2[0-3]):([0-5]\\d)$")
    }
}
