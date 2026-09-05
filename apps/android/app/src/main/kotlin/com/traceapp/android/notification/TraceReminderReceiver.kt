package com.traceapp.android.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.traceapp.core.contracts.MemoryApi
import com.traceapp.core.contracts.TraceResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TraceReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var memoryApi: MemoryApi
    @Inject lateinit var notifier: TraceNotifier
    @Inject lateinit var scheduler: ReminderScheduler
    @Inject @ApplicationScope lateinit var scope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ReminderScheduler.ACTION_REMINDER -> {
                val pending = goAsync()
                scope.launch {
                    try {
                        val tag = when (val result = memoryApi.mostMatchedItem(System.currentTimeMillis())) {
                            is TraceResult.Success -> result.value?.tag
                            is TraceResult.Failure -> null
                        }
                        notifier.announceMostMatched(tag)
                    } finally {
                        pending.finish()
                    }
                }
            }
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ReminderScheduler.ACTION_REARM,
            -> {
                val pending = goAsync()
                scope.launch {
                    try {
                        scheduler.arm()
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}
