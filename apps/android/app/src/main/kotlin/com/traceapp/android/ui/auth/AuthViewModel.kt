package com.traceapp.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.traceapp.core.network.AuthRepository
import com.traceapp.core.network.AuthResult
import com.traceapp.core.network.AuthUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val initializing: Boolean = true,
    val loading: Boolean = false,
    val user: AuthUser? = null,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = repository.restore()) {
                is AuthResult.Success -> mutableState.value = AuthUiState(user = result.value, initializing = false)
                is AuthResult.Failure -> mutableState.value = AuthUiState(
                    initializing = false,
                    error = if (result.unauthorized) null else result.message,
                )
            }
        }
    }

    fun login(email: String, password: String) = authenticate {
        repository.login(email, password)
    }

    fun register(displayName: String, email: String, password: String) = authenticate {
        repository.register(displayName, email, password)
    }

    fun logout() {
        viewModelScope.launch {
            mutableState.update { it.copy(loading = true, error = null) }
            repository.logout()
            mutableState.value = AuthUiState(initializing = false)
        }
    }

    fun clearError() = mutableState.update { it.copy(error = null) }

    private fun authenticate(block: suspend () -> AuthResult<AuthUser>) {
        if (mutableState.value.loading) return
        viewModelScope.launch {
            mutableState.update { it.copy(loading = true, error = null) }
            when (val result = block()) {
                is AuthResult.Success -> mutableState.value = AuthUiState(
                    initializing = false,
                    user = result.value,
                )
                is AuthResult.Failure -> mutableState.update {
                    it.copy(loading = false, error = result.message)
                }
            }
        }
    }
}
