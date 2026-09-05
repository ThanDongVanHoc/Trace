package com.traceapp.android.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arms a daily (inexact) alarm for each configured reminder time. Alarms survive process death
 * but are re-armed on boot/time change and whenever the time list is edited.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settings: TraceNotificationSettings,
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /** Cancels previously armed alarms and arms one repeating alarm per stored time. */
    fun arm() {
        cancel()
        val now = System.currentTimeMillis()
        val times = settings.getReminderTimes()
        val newArmed = times.toSet()
        times.forEach { time ->
            val minuteOfDay = minuteOfDay(time) ?: return@forEach
            val triggerAt = nextTriggerMillis(time, now)
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                AlarmManager.INTERVAL_DAY,
                pendingIntent(minuteOfDay),
            )
        }
        preferences.edit().putStringSet(KEY_ARMED, newArmed).apply()
    }

    fun cancel() {
        val armed = preferences.getStringSet(KEY_ARMED, emptySet()).orEmpty()
        armed.forEach { time ->
            minuteOfDay(time)?.let { alarmManager.cancel(pendingIntent(it)) }
        }
        preferences.edit().remove(KEY_ARMED).apply()
    }

    private fun pendingIntent(minuteOfDay: Int): PendingIntent {
        val intent = Intent(context, TraceReminderReceiver::class.java).setAction(ACTION_REMINDER)
        return PendingIntent.getBroadcast(
            context,
            minuteOfDay,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun minuteOfDay(time: String): Int? {
        val parts = time.split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }

    private fun nextTriggerMillis(time: String, now: Long): Long {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DATE, 1)
        }
        return calendar.timeInMillis
    }

    companion object {
        const val ACTION_REMINDER = "com.traceapp.android.action.REMINDER"
        const val ACTION_REARM = "com.traceapp.android.action.REARM"
        private const val PREFERENCES_NAME = "trace_reminder_schedule"
        private const val KEY_ARMED = "armed_times"
    }
}
