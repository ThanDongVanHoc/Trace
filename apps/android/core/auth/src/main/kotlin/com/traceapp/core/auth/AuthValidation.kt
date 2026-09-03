package com.traceapp.core.auth

data class RegistrationValidation(
    val displayNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmationError: String? = null,
) {
    val isValid: Boolean
        get() = displayNameError == null && emailError == null &&
            passwordError == null && confirmationError == null
}

object AuthValidation {
    const val MINIMUM_PASSWORD_LENGTH = 8

    fun registration(
        displayName: String,
        email: String,
        password: String,
        confirmation: String? = null,
    ): RegistrationValidation = RegistrationValidation(
        displayNameError = when (displayName.trim().length) {
            0, 1 -> "Nhập tên có ít nhất 2 ký tự"
            in 2..80 -> null
            else -> "Tên không được quá 80 ký tự"
        },
        emailError = emailError(email),
        passwordError = when {
            password.length < MINIMUM_PASSWORD_LENGTH ->
                "Mật khẩu cần ít nhất $MINIMUM_PASSWORD_LENGTH ký tự"
            password.isBlank() -> "Nhập mật khẩu"
            else -> null
        },
        confirmationError = when {
            confirmation == null -> null
            confirmation.isEmpty() -> "Nhập lại mật khẩu"
            confirmation != password -> "Mật khẩu nhập lại chưa khớp"
            else -> null
        },
    )

    fun emailError(email: String): String? {
        val value = email.trim()
        return when {
            value.isEmpty() -> "Nhập email"
            value.length > 254 || !EMAIL.matches(value) -> "Email không hợp lệ"
            else -> null
        }
    }

    private val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
}
