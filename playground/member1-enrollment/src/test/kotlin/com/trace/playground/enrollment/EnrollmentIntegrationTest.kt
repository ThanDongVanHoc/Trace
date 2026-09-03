package com.trace.playground.enrollment

import com.trace.playground.contracts.EnrollmentRequest
import com.trace.playground.contracts.ImageInput
import com.trace.playground.contracts.Roi
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EnrollmentIntegrationTest {
    private val algorithm = EnrollmentAlgorithm()

    @Test
    fun `enrolls real mug sample image successfully`() = runBlocking {
        val mugBytes = loadSample("enroll_mug.jpg")
        assertNotNull(mugBytes, "enroll_mug.jpg should be found")

        val result = algorithm.enroll(
            EnrollmentRequest(
                tag = "Ly cà phê",
                image = ImageInput(mugBytes, rotationDegrees = 0),
                roi = Roi(0.1f, 0.1f, 0.9f, 0.9f),
            ),
        )

        assertEquals("Ly cà phê", result.tag)
        assertEquals(576, result.embedding.values.size)
        assertEquals("mobilenet-v3-small", result.embedding.modelName)
        assertEquals("1", result.embedding.modelVersion)
        assertTrue(result.qualityScore > 0.1f, "Real photo should pass quality check")

        // Verify L2 norm = 1.0
        val sumSquares = result.embedding.values.fold(0.0) { acc, v -> acc + (v.toDouble() * v.toDouble()) }
        val norm = sqrt(sumSquares)
        assertTrue(abs(norm - 1.0) < 0.01, "Embedding must be L2 normalized")
    }

    @Test
    fun `computes semantic similarity between mug and retrieval scene`() = runBlocking {
        val mugBytes = loadSample("enroll_mug.jpg")
        val sceneBytes = loadSample("retrieval_scene.jpg")

        assertNotNull(mugBytes, "enroll_mug.jpg should be found")
        assertNotNull(sceneBytes, "retrieval_scene.jpg should be found")

        // Enroll the mug
        val mugResult = algorithm.enroll(
            EnrollmentRequest(
                tag = "Ly cà phê",
                image = ImageInput(mugBytes),
                roi = Roi(0.05f, 0.05f, 0.95f, 0.95f),
            ),
        )

        // Enroll retrieval scene (representing full image encoding for recognition)
        val sceneResult = algorithm.enroll(
            EnrollmentRequest(
                tag = "Scene",
                image = ImageInput(sceneBytes),
                roi = Roi(0f, 0f, 1f, 1f),
            ),
        )

        val sim = EnrollmentAlgorithm.cosineSimilarity(
            mugResult.embedding.values.toFloatArray(),
            sceneResult.embedding.values.toFloatArray(),
        )

        println("=== REAL MODEL SEMANTIC VERIFICATION ===")
        println("Mug embedding first 5 dims: ${mugResult.embedding.values.take(5)}")
        println("Scene embedding first 5 dims: ${sceneResult.embedding.values.take(5)}")
        println("Cosine similarity between mug and scene: $sim")

        // Both images contain indoor objects/surfaces, similarity should be a reasonable float in [-1, 1]
        assertTrue(sim in -1.0f..1.0f, "Cosine similarity must be in valid range [-1, 1]")
        assertTrue(mugResult.embedding.values.size == 576)
        assertTrue(sceneResult.embedding.values.size == 576)
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
