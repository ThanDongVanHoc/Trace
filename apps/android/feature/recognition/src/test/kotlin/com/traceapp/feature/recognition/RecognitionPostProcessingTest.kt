package com.traceapp.feature.recognition

import com.google.common.truth.Truth.assertThat
import com.traceapp.core.contracts.MatchStatus
import com.traceapp.core.contracts.NormalizedRect
import com.traceapp.core.contracts.ObjectDetection
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.VisualEmbedding
import org.junit.Test

class RecognitionPostProcessingTest {
    @Test
    fun eachCandidate_isAssignedToOnlyItsBestReference() {
        val match = bestReferenceMatch(
            candidate = floatArrayOf(1f, 0f),
            references = listOf(
                reference("object-a", floatArrayOf(1f, 0f)),
                reference("object-b", floatArrayOf(0.9f, 0.1f)),
            ),
            threshold = 0.75f,
            box = NormalizedRect.FullImage,
            modelName = "test-model",
            modelVersion = "1",
        )

        assertThat(match?.objectId).isEqualTo("object-a")
        assertThat(match?.status).isEqualTo(MatchStatus.MATCHED)
    }

    @Test
    fun detections_keepSeveralDistinctObjects_butDeduplicateRepeatedBoxes() {
        val detections = mergeDetections(
            values = listOf(
                detection("object-a", 0.86f),
                detection("object-b", 0.92f),
                detection("object-a", 0.81f),
            ),
            limit = 5,
        )

        assertThat(detections.mapNotNull { it.objectId }).containsExactly("object-b", "object-a").inOrder()
    }

    private fun reference(objectId: String, values: FloatArray) = ObjectReference(
        referenceId = "reference-$objectId",
        objectId = objectId,
        tag = objectId,
        imageAssetId = "asset-$objectId",
        roi = NormalizedRect.FullImage,
        embeddings = listOf(VisualEmbedding(values, "test-model", "1")),
        qualityScore = 1f,
        createdAtEpochMillis = 1L,
    )

    private fun detection(objectId: String, similarity: Float) = ObjectDetection(
        objectId = objectId,
        boundingBox = NormalizedRect.FullImage,
        similarity = similarity,
        status = MatchStatus.MATCHED,
    )
}
