package com.traceapp.android.ui.profile

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import com.traceapp.android.notification.TraceNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileUiState(
    val notificationsEnabled: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notifier: TraceNotifier,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        ProfileUiState(notificationsEnabled = notificationsAllowed()),
    )
    val state: StateFlow<ProfileUiState> = mutableState.asStateFlow()

    fun enableNotifications() {
        val enabled = notificationsAllowed()
        mutableState.value = ProfileUiState(
            notificationsEnabled = enabled,
            message = if (enabled) "Thông báo local đã bật." else "Quyền thông báo chưa được cấp.",
            isError = !enabled,
        )
    }

    fun sendTestNotification() {
        if (!notificationsAllowed()) {
            enableNotifications()
            return
        }
        notifier.testNotification()
        mutableState.value = ProfileUiState(
            notificationsEnabled = true,
            message = "Đã gửi thông báo thử trên thiết bị.",
        )
    }

    private fun notificationsAllowed(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()
}
