package com.traceapp.android

import android.app.Application
import com.traceapp.android.notification.TraceNotifier
import com.traceapp.core.contracts.VisualEncoder
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class TraceApplication : Application() {
    @Inject lateinit var visualEncoder: Lazy<VisualEncoder>

    private val startupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        TraceNotifier.createChannel(this)
        startupScope.launch { visualEncoder.get().warmUp() }
    }
}
