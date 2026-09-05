package com.traceapp.android.ui.profile

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.traceapp.android.notification.ReminderScheduler
import com.traceapp.android.notification.TraceNotificationSettings
import com.traceapp.android.notification.TraceNotifier
import com.traceapp.core.contracts.MemoryApi
import com.traceapp.core.contracts.TraceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val notificationsEnabled: Boolean = false,
    val reminderTimes: List<String> = emptyList(),
    val message: String? = null,
    val isError: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notifier: TraceNotifier,
    private val settings: TraceNotificationSettings,
    private val scheduler: ReminderScheduler,
    private val memoryApi: MemoryApi,
) : ViewModel() {
    private val mutableState = MutableStateFlow(
        ProfileUiState(
            notificationsEnabled = settings.isEnabled() && notificationsAllowed(),
            reminderTimes = if (settings.isEnabled()) settings.getReminderTimes() else emptyList(),
        ),
    )
    val state: StateFlow<ProfileUiState> = mutableState.asStateFlow()

    fun enableNotifications() {
        val enabled = notificationsAllowed()
        settings.setEnabled(enabled)
        if (enabled) {
            settings.setReminderTimes(settings.getReminderTimes())
            scheduler.arm()
        }
        mutableState.value = ProfileUiState(
            notificationsEnabled = enabled,
            reminderTimes = if (enabled) settings.getReminderTimes() else emptyList(),
            message = if (enabled) "Thông báo local đã bật." else "Quyền thông báo chưa được cấp.",
            isError = !enabled,
        )
    }

    fun disableNotifications() {
        settings.setEnabled(false)
        scheduler.cancel()
        notifier.cancelAll()
        mutableState.value = ProfileUiState(
            notificationsEnabled = false,
            reminderTimes = emptyList(),
            message = "Thông báo local đã tắt.",
        )
    }

    fun addReminderTime(time: String) {
        if (!TIME_PATTERN.matches(time.trim())) {
            mutableState.value = state.value.copy(
                message = "Định dạng giờ phải là HH:mm (ví dụ 07:30).",
                isError = true,
            )
            return
        }
        val normalized = normalize(time)
        val updated = (settings.getReminderTimes() + normalized).distinct()
        applyReminderTimes(updated)
    }

    fun removeReminderTime(time: String) {
        applyReminderTimes(settings.getReminderTimes().filterNot { it == time })
    }

    private fun applyReminderTimes(times: List<String>) {
        settings.setReminderTimes(times)
        scheduler.arm()
        mutableState.value = state.value.copy(
            reminderTimes = settings.getReminderTimes(),
            message = null,
            isError = false,
        )
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            enableNotifications()
        } else {
            settings.setEnabled(false)
            scheduler.cancel()
            mutableState.value = ProfileUiState(
                notificationsEnabled = false,
                reminderTimes = emptyList(),
                message = "Quyền thông báo chưa được cấp.",
                isError = true,
            )
        }
    }

    fun sendTestNotification() {
        if (!settings.isEnabled() || !notificationsAllowed()) {
            mutableState.value = ProfileUiState(
                notificationsEnabled = false,
                reminderTimes = emptyList(),
                message = "Hãy bật thông báo trước khi gửi thử.",
                isError = true,
            )
            return
        }
        viewModelScope.launch {
            val tag = when (val result = memoryApi.mostMatchedItem(System.currentTimeMillis())) {
                is TraceResult.Success -> result.value?.tag
                is TraceResult.Failure -> null
            }
            notifier.announceMostMatched(tag)
            mutableState.value = ProfileUiState(
                notificationsEnabled = true,
                reminderTimes = settings.getReminderTimes(),
                message = "Đã gửi thông báo thử với đồ vật phù hợp nhất.",
            )
        }
    }

    private fun normalize(time: String): String {
        val parts = time.trim().split(":")
        return "%02d:%02d".format(parts[0].toInt(), parts[1].toInt())
    }

    private fun notificationsAllowed(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    private companion object {
        val TIME_PATTERN = Regex("^([01]?\\d|2[0-3]):([0-5]\\d)$")
    }
}
