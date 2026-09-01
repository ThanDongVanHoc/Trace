package com.trace.playground.enrollment

import com.trace.playground.contracts.EnrollmentRequest
import com.trace.playground.contracts.ImageInput
import com.trace.playground.contracts.Roi
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.sqrt
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EnrollmentAlgorithmTest {
    private val algorithm = EnrollmentAlgorithm()

    @Test
    fun `enrolls one tagged ROI with 576-dim vector`() = runBlocking {
        val result = algorithm.enroll(
            EnrollmentRequest("balo", ImageInput(colorfulTestJpeg()), Roi(0f, 0f, 1f, 1f)),
        )
        assertEquals("balo", result.tag)
        assertEquals(EnrollmentAlgorithm.EMBEDDING_DIM, result.embedding.values.size)
        assertEquals(EnrollmentAlgorithm.MODEL_NAME, result.embedding.modelName)
        assertEquals(EnrollmentAlgorithm.MODEL_VERSION, result.embedding.modelVersion)
    }

    @Test
    fun `embedding is L2 normalized`() = runBlocking {
        val result = algorithm.enroll(
            EnrollmentRequest("test", ImageInput(colorfulTestJpeg()), Roi(0f, 0f, 1f, 1f)),
        )
        val sumSquares = result.embedding.values.fold(0.0) { acc, v -> acc + (v.toDouble() * v.toDouble()) }
        val norm = sqrt(sumSquares)
        assertTrue(abs(norm - 1.0) < 0.01, "L2 norm should be ~1.0 but was $norm")
    }

    @Test
    fun `same image produces same embedding`() = runBlocking {
        val jpeg = colorfulTestJpeg()
        val result1 = algorithm.enroll(
            EnrollmentRequest("a", ImageInput(jpeg), Roi(0f, 0f, 1f, 1f)),
        )
        val result2 = algorithm.enroll(
            EnrollmentRequest("b", ImageInput(jpeg), Roi(0f, 0f, 1f, 1f)),
        )
        val similarity = EnrollmentAlgorithm.cosineSimilarity(
            result1.embedding.values.toFloatArray(),
            result2.embedding.values.toFloatArray(),
        )
        assertTrue(similarity > 0.999f, "Same image should produce identical vectors, got $similarity")
    }

    @Test
    fun `rejects invalid ROI`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            algorithm.enroll(
                EnrollmentRequest("balo", ImageInput(colorfulTestJpeg()), Roi(0.9f, 0f, 0.2f, 1f)),
            )
        }
    }

    @Test
    fun `rejects too-small ROI`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            // 128x128 image with ROI covering ~10% = ~12x12 pixels, below 32x32 minimum
            algorithm.enroll(
                EnrollmentRequest("tiny", ImageInput(colorfulTestJpeg(128)), Roi(0f, 0f, 0.1f, 0.1f)),
            )
        }
    }

    @Test
    fun `rejects black image`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            algorithm.enroll(
                EnrollmentRequest("dark", ImageInput(solidColorJpeg(Color.BLACK)), Roi(0f, 0f, 1f, 1f)),
            )
        }
    }

    @Test
    fun `rejects white image`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            algorithm.enroll(
                EnrollmentRequest("bright", ImageInput(solidColorJpeg(Color.WHITE)), Roi(0f, 0f, 1f, 1f)),
            )
        }
    }

    @Test
    fun `quality score rejects uniform image`() {
        val uniform = BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until 64) for (x in 0 until 64) uniform.setRGB(x, y, Color(128, 128, 128).rgb)
        val score = algorithm.qualityScore(uniform)
        assertTrue(score < 0.08f, "Uniform image should have low quality score, got $score")
    }

    @Test
    fun `imageToTensor produces correct size`() {
        val image = BufferedImage(100, 80, BufferedImage.TYPE_INT_RGB)
        val tensor = algorithm.imageToTensor(image)
        assertEquals(3 * 224 * 224, tensor.size, "Tensor should have 3*224*224 = 150528 elements")
    }

    // ── Test helpers ─────────────────────────────────────────────

    private fun colorfulTestJpeg(size: Int = 128): ByteArray {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until size) {
            for (x in 0 until size) {
                image.setRGB(x, y, ((x * 2) shl 16) or ((y * 2) shl 8) or (((x + y) % 256)))
            }
        }
        return ByteArrayOutputStream().use { output ->
            ImageIO.write(image, "jpg", output)
            output.toByteArray()
        }
    }

    private fun solidColorJpeg(color: Color, size: Int = 128): ByteArray {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until size) for (x in 0 until size) image.setRGB(x, y, color.rgb)
        return ByteArrayOutputStream().use { output ->
            ImageIO.write(image, "jpg", output)
            output.toByteArray()
        }
    }
}
