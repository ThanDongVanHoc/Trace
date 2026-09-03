package com.trace.playground.recognition

import com.trace.playground.contracts.ImageInput
import com.trace.playground.contracts.MatchStatus
import com.trace.playground.contracts.RecognitionRequest
import com.trace.playground.contracts.ReferenceVector
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class RecognitionIntegrationTest {

    private val algorithm = RecognitionAlgorithm()

    @Test
    fun `recognizes mug in real retrieval scene image`() = runBlocking {
        val mugBytes = loadSample("enroll_mug.jpg")
        val sceneBytes = loadSample("retrieval_scene.jpg")

        assertNotNull(mugBytes, "enroll_mug.jpg should exist")
        assertNotNull(sceneBytes, "retrieval_scene.jpg should exist")

        // 1. Trích xuất embedding của enroll_mug.jpg làm reference "Ly cà phê"
        val mugImage = ImageIO.read(ByteArrayInputStream(mugBytes))
        val mugEmbedding = algorithm.extractEmbedding(mugImage)

        val reference = ReferenceVector(
            referenceId = "ref-mug-001",
            objectId = "obj-mug-001",
            tag = "Ly cà phê",
            values = mugEmbedding.toList(),
            modelName = RecognitionAlgorithm.MODEL_NAME,
            modelVersion = RecognitionAlgorithm.MODEL_VERSION,
        )

        // 2. Chạy recognize trên retrieval_scene.jpg với threshold 0.30
        val result = algorithm.recognize(
            RecognitionRequest(
                image = ImageInput(sceneBytes),
                references = listOf(reference),
                minimumSimilarity = 0.30f,
                maximumResults = 5,
            ),
        )

        println("=== REAL SCENE RECOGNITION RESULTS ===")
        println("Model version: ${result.modelVersion}")
        println("Processing time: ${result.processingTimeMillis} ms")
        println("Total detections: ${result.detections.size}")
        result.detections.forEachIndexed { i, d ->
            println("  [$i] status=${d.status}, tag=${d.tag}, sim=${d.similarity}, box=${d.boundingBox}")
        }

        assertTrue(result.detections.isNotEmpty(), "Should have at least one detection")
        val top = result.detections[0]
        assertEquals(MatchStatus.MATCHED, top.status, "Should match 'Ly cà phê'")
        assertEquals("Ly cà phê", top.tag)
        assertTrue(top.similarity >= 0.30f, "Similarity should meet threshold")
    }

    private fun loadSample(name: String): ByteArray? {
        val candidates = listOf(
            Path.of("playground", "data", "samples", name),
            Path.of("data", "samples", name),
            Path.of("..", "data", "samples", name),
            Path.of("..", "..", "playground", "data", "samples", name),
        )
        val path = candidates.firstOrNull { Files.exists(it) } ?: return null
        return Files.readAllBytes(path)
    }
}
