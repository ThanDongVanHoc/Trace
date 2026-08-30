package com.traceapp.android.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.traceapp.core.network.DeviceRequestDto
import com.traceapp.core.network.TraceBackendApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val busy: Boolean = false,
    val notificationsEnabled: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: TraceBackendApi,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = mutableState.asStateFlow()

    @Suppress("DEPRECATION")
    fun enableNotifications() {
        if (mutableState.value.busy) return
        viewModelScope.launch {
            mutableState.update { it.copy(busy = true, message = null) }
            runCatching {
                val token = FirebaseMessaging.getInstance().token.await()
                api.upsertDevice(
                    installationId = installationId(),
                    body = DeviceRequestDto(
                        pushToken = token,
                        locale = Locale.getDefault().toLanguageTag(),
                        notificationsEnabled = true,
                    ),
                )
            }.onSuccess {
                mutableState.value = ProfileUiState(
                    notificationsEnabled = true,
                    message = "Đã đăng ký thông báo cho thiết bị này.",
                )
            }.onFailure {
                mutableState.value = ProfileUiState(
                    message = "Chưa bật được FCM. Kiểm tra google-services.json và backend.",
                    isError = true,
                )
            }
        }
    }

    fun sendTestNotification() {
        if (mutableState.value.busy) return
        viewModelScope.launch {
            mutableState.update { it.copy(busy = true, message = null) }
            runCatching { api.sendTestNotification() }
                .onSuccess {
                    mutableState.update {
                        it.copy(busy = false, message = "Đã đưa thông báo thử vào hàng đợi.", isError = false)
                    }
                }
                .onFailure {
                    mutableState.update {
                        it.copy(busy = false, message = "Không gửi được thông báo thử.", isError = true)
                    }
                }
        }
    }

    private fun installationId(): String {
        val preferences = context.getSharedPreferences("trace_installation", Context.MODE_PRIVATE)
        preferences.getString("installation_id", null)?.let { return it }
        val generated = UUID.randomUUID().toString()
        check(preferences.edit().putString("installation_id", generated).commit())
        return generated
    }
}
