package com.traceapp.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.traceapp.android.ui.auth.AuthScreen
import com.traceapp.android.ui.auth.AuthViewModel
import com.traceapp.android.ui.shell.TraceShell

@Composable
fun TraceApp(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.initializing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.user == null -> AuthScreen(
            state = state,
            onLogin = viewModel::login,
            onRegister = viewModel::register,
            onClearError = viewModel::clearError,
        )
        else -> TraceShell(user = requireNotNull(state.user), onLogout = viewModel::logout)
    }
}
