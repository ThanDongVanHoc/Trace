package com.traceapp.android.ui.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.traceapp.core.contracts.NormalizedRect
import org.junit.Rule
import org.junit.Test

class RoiOverlayGestureTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun longContinuousCornerDrag_isNotCancelledByRecomposition() {
        var rect by mutableStateOf(NormalizedRect(0.2f, 0.2f, 0.8f, 0.8f))
        composeRule.setContent {
            Box(Modifier.size(300.dp)) {
                RoiOverlay(
                    rect = rect,
                    onRectChange = { rect = it },
                    modifier = Modifier.fillMaxSize().testTag("roi_test"),
                )
            }
        }

        composeRule.onNodeWithTag("roi_test").performTouchInput {
            swipe(
                start = Offset(width * 0.8f, height * 0.8f),
                end = Offset(width * 0.95f, height * 0.92f),
                durationMillis = 900,
            )
        }

        composeRule.runOnIdle {
            assertThat(rect.right).isGreaterThan(0.93f)
            assertThat(rect.bottom).isGreaterThan(0.90f)
        }
    }

    @Test
    fun draggingCenter_movesRectangleAndPreservesSize() {
        var rect by mutableStateOf(NormalizedRect(0.2f, 0.2f, 0.6f, 0.6f))
        composeRule.setContent {
            RoiOverlay(
                rect = rect,
                onRectChange = { rect = it },
                modifier = Modifier.size(300.dp).testTag("roi_test"),
            )
        }

        composeRule.onNodeWithTag("roi_test").performTouchInput {
            swipe(
                start = Offset(width * 0.4f, height * 0.4f),
                end = Offset(width * 0.6f, height * 0.55f),
                durationMillis = 700,
            )
        }

        composeRule.runOnIdle {
            assertThat(rect.left).isWithin(0.03f).of(0.4f)
            assertThat(rect.top).isWithin(0.03f).of(0.35f)
            assertThat(rect.right - rect.left).isWithin(0.01f).of(0.4f)
            assertThat(rect.bottom - rect.top).isWithin(0.01f).of(0.4f)
        }
    }
}
