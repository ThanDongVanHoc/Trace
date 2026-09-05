package com.traceapp.android.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyHome_exposesClearPrimaryActions() {
        var scanClicks = 0
        var findClicks = 0
        composeRule.setContent {
            MaterialTheme {
                HomeScreen(
                    state = ObjectMemoryUiState(loading = false),
                    displayName = "Minh Nguyen",
                    onScan = { scanClicks++ },
                    onFind = { findClicks++ },
                    onUsageAxisChange = {},
                    onUsageStep = {},
                )
            }
        }

        composeRule.onNodeWithText("Ghi nhớ đồ vật mới").performClick()
        composeRule.onNodeWithText("Tìm đồ").performClick()

        composeRule.runOnIdle {
            assertThat(scanClicks).isEqualTo(1)
            assertThat(findClicks).isEqualTo(1)
        }
    }
}
