package com.traceapp.android

import android.app.Application
import com.traceapp.android.notification.TraceNotifier
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TraceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TraceNotifier.createChannel(this)
    }
}
