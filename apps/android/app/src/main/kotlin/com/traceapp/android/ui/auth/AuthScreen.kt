package com.traceapp.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traceapp.android.R
import com.traceapp.core.auth.AuthValidation

@Composable
fun AuthScreen(
    state: AuthUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onClearError: () -> Unit,
) {
    var registering by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var registrationEmail by remember { mutableStateOf("") }
    var registrationPassword by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var revealPassword by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val email = if (registering) registrationEmail else loginEmail
    val password = if (registering) registrationPassword else loginPassword
    val validation = AuthValidation.registration(displayName, registrationEmail, registrationPassword, confirmation)
    val emailError = AuthValidation.emailError(email)
    val passwordError = if (password.isBlank()) "Nhập mật khẩu" else null
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmationFocus = remember { FocusRequester() }

    fun submit() {
        submitted = true
        val valid = if (registering) validation.isValid else emailError == null && passwordError == null
        if (!valid || state.loading) return
        keyboard?.hide()
        focusManager.clearFocus()
        if (registering) onRegister(displayName, email, password) else onLogin(email, password)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Box(
            Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .padding(top = 12.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f), CircleShape),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trace_logo),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(12.dp).size(52.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "TRACE",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
            )
            Text(
                if (registering) "Tạo không gian ghi nhớ riêng của bạn" else "Mọi thứ quan trọng, luôn tìm thấy",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AuthModeSelector(
                        registering = registering,
                        onModeChange = { next ->
                            registering = next
                            submitted = false
                            onClearError()
                        },
                    )

                    if (registering) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = {
                                displayName = it
                                if (state.error != null) onClearError()
                            },
                            label = { Text("Tên hiển thị") },
                            placeholder = { Text("Ví dụ: Minh Nguyễn") },
                            isError = submitted && validation.displayNameError != null,
                            supportingText = errorText(submitted, validation.displayNameError),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().testTag("display_name"),
                        )
                    }
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            if (registering) registrationEmail = it else loginEmail = it
                            if (state.error != null) onClearError()
                        },
                        label = { Text("Email") },
                        placeholder = { Text("ban@example.com") },
                        isError = submitted && emailError != null,
                        supportingText = errorText(submitted, emailError),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().focusRequester(emailFocus).testTag("email"),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            if (registering) registrationPassword = it else loginPassword = it
                            if (state.error != null) onClearError()
                        },
                        label = { Text("Mật khẩu") },
                        visualTransformation = if (revealPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (registering) ImeAction.Next else ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { confirmationFocus.requestFocus() },
                            onDone = { submit() },
                        ),
                        isError = submitted && if (registering) validation.passwordError != null else passwordError != null,
                        supportingText = if (registering) {
                            errorText(submitted, validation.passwordError) ?: {
                                Text("Ít nhất ${AuthValidation.MINIMUM_PASSWORD_LENGTH} ký tự")
                            }
                        } else {
                            errorText(submitted, passwordError)
                        },
                        trailingIcon = {
                            IconButton(onClick = { revealPassword = !revealPassword }) {
                                Icon(
                                    imageVector = if (revealPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (revealPassword) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().focusRequester(passwordFocus).testTag("password"),
                    )
                    if (registering) {
                        OutlinedTextField(
                            value = confirmation,
                            onValueChange = { confirmation = it },
                            label = { Text("Nhập lại mật khẩu") },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = submitted && validation.confirmationError != null,
                            supportingText = errorText(submitted, validation.confirmationError),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().focusRequester(confirmationFocus).testTag("password_confirmation"),
                        )
                    }

                    state.error?.let { message ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_error"),
                        ) {
                            Text(message, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Button(
                        onClick = { submit() },
                        enabled = !state.loading,
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("auth_submit"),
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(
                                if (registering) "Tạo tài khoản" else "Đăng nhập",
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "Tài khoản và dữ liệu chỉ lưu trên thiết bị này",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AuthModeSelector(registering: Boolean, onModeChange: (Boolean) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(4.dp)) {
            AuthMode(
                label = "Đăng nhập",
                selected = !registering,
                onClick = { onModeChange(false) },
                modifier = Modifier.weight(1f).testTag("mode_login"),
            )
            AuthMode(
                label = "Đăng ký",
                selected = registering,
                onClick = { onModeChange(true) },
                modifier = Modifier.weight(1f).testTag("mode_register"),
            )
        }
    }
}

@Composable
private fun AuthMode(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(13.dp),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier.clickable(role = Role.Tab, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 11.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.size(6.dp))
            }
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun errorText(
    submitted: Boolean,
    message: String?,
): (@Composable () -> Unit)? = if (submitted && message != null) {
    { Text(message) }
} else {
    null
}
