package com.traceapp.android.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.traceapp.android.MainActivity
import com.traceapp.android.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraceNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settings: TraceNotificationSettings,
) {
    fun sightingRecorded(tag: String) {
        notify("TRACE đã ghi nhớ", "Đã lưu lần xuất hiện mới của $tag.", tag.hashCode())
    }

    fun sightingsRecorded(tags: List<String>) {
        val distinctTags = tags.distinct()
        if (distinctTags.isEmpty()) return
        if (distinctTags.size == 1) {
            sightingRecorded(distinctTags.single())
        } else {
            notify(
                "TRACE đã nhận diện ${distinctTags.size} đồ vật",
                distinctTags.joinToString(limit = 3, truncated = "…"),
                MULTI_SIGHTING_NOTIFICATION_ID,
            )
        }
    }

    fun testNotification() {
        notify("TRACE hoạt động độc lập", "Thông báo local đã sẵn sàng.", 1)
    }

    fun cancelAll() = NotificationManagerCompat.from(context).cancelAll()

    private fun notify(title: String, body: String, id: Int) {
        if (!settings.isEnabled()) return
        createChannel(context)
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_trace_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    companion object {
        private const val CHANNEL_ID = "trace_local_reminders"
        private const val MULTI_SIGHTING_NOTIFICATION_ID = 2

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc nhớ TRACE",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = "Thông báo khi TRACE ghi nhớ một lần xuất hiện mới"
                }
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
        }
    }
}
