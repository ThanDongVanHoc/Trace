package com.traceapp.android.ui.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class AuthScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun registrationForm_validInput_submitsAllFields() {
        var submitted: Triple<String, String, String>? = null
        composeRule.setContent {
            MaterialTheme {
                AuthScreen(
                    state = AuthUiState(initializing = false),
                    onLogin = { _, _ -> },
                    onRegister = { name, email, password -> submitted = Triple(name, email, password) },
                    onClearError = {},
                )
            }
        }

        composeRule.onNodeWithTag("mode_register").performClick()
        composeRule.onNodeWithTag("display_name").performTextInput("Minh Nguyen")
        composeRule.onNodeWithTag("email").performTextInput("minh@example.com")
        composeRule.onNodeWithTag("password").performTextInput("trace123")
        composeRule.onNodeWithTag("password_confirmation").performTextInput("trace123")
        composeRule.onNodeWithTag("auth_submit").performScrollTo().performClick()

        composeRule.runOnIdle {
            assertThat(submitted).isEqualTo(Triple("Minh Nguyen", "minh@example.com", "trace123"))
        }
    }

    @Test
    fun registrationForm_mismatchedPassword_showsUsefulError() {
        composeRule.setContent {
            MaterialTheme {
                AuthScreen(
                    state = AuthUiState(initializing = false),
                    onLogin = { _, _ -> },
                    onRegister = { _, _, _ -> error("Must not submit invalid form") },
                    onClearError = {},
                )
            }
        }

        composeRule.onNodeWithTag("mode_register").performClick()
        composeRule.onNodeWithTag("display_name").performTextInput("Minh Nguyen")
        composeRule.onNodeWithTag("email").performTextInput("minh@example.com")
        composeRule.onNodeWithTag("password").performTextInput("trace123")
        composeRule.onNodeWithTag("password_confirmation").performTextInput("different")
        composeRule.onNodeWithTag("auth_submit").performScrollTo().performClick()

        composeRule.onNodeWithTag("password_confirmation").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Mật khẩu nhập lại chưa khớp").assertIsDisplayed()
    }
}
