package com.traceapp.android.ui.find

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.traceapp.android.ui.home.ObjectMemoryUiState
import com.traceapp.core.contracts.FindLastSeenResponse
import com.traceapp.core.contracts.GeoFix
import com.traceapp.core.contracts.NormalizedRect
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.Sighting
import com.traceapp.core.contracts.VisualEmbedding
import org.junit.Rule
import org.junit.Test

class FindScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectedObject_isNotDuplicated_andLocationOffersDirections() {
        val reference = ObjectReference(
            referenceId = "reference-1",
            objectId = "object-1",
            tag = "Ba lô đen",
            imageAssetId = "asset-1",
            roi = NormalizedRect.FullImage,
            embeddings = listOf(VisualEmbedding(floatArrayOf(1f), "test", "1")),
            qualityScore = 1f,
            createdAtEpochMillis = 1L,
        )
        val sighting = Sighting(
            sightingId = "sighting-1",
            objectId = reference.objectId,
            detectedAtEpochMillis = 1L,
            location = GeoFix(10.12345, 106.54321, 12f, 1L),
            confidence = 0.91f,
            evidenceAssetId = null,
        )
        composeRule.setContent {
            MaterialTheme {
                FindScreen(
                    state = ObjectMemoryUiState(
                        loading = false,
                        references = listOf(reference),
                        selected = FindLastSeenResponse(reference.objectId, reference.tag, sighting),
                    ),
                    onFind = {},
                    onDelete = {},
                    onUsageAxisChange = {},
                    onUsageStep = {},
                )
            }
        }

        composeRule.onAllNodesWithText("Ba lô đen").assertCountEquals(1)
        composeRule.onNodeWithTag("open_directions").assertIsDisplayed()
        composeRule.onAllNodesWithText("10.12345", substring = true).assertCountEquals(0)
    }
}
