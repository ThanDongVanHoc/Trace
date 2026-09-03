package com.traceapp.core.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AuthValidationTest {
    @Test
    fun `valid registration accepts a practical password`() {
        val result = AuthValidation.registration(
            displayName = "Minh Nguyen",
            email = "minh@example.com",
            password = "trace123",
            confirmation = "trace123",
        )

        assertThat(result.isValid).isTrue()
    }

    @Test
    fun `invalid fields return specific errors`() {
        val result = AuthValidation.registration(
            displayName = "M",
            email = "not-an-email",
            password = "short",
            confirmation = "different",
        )

        assertThat(result.displayNameError).isNotNull()
        assertThat(result.emailError).isNotNull()
        assertThat(result.passwordError).isNotNull()
        assertThat(result.confirmationError).isNotNull()
        assertThat(result.isValid).isFalse()
    }

    @Test
    fun `email is trimmed before validation`() {
        assertThat(AuthValidation.emailError("  user@trace.app  ")).isNull()
    }
}
