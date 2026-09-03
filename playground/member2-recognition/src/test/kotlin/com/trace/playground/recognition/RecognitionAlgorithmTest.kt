package com.trace.playground.recognition

import com.trace.playground.contracts.Detection
import com.trace.playground.contracts.ImageInput
import com.trace.playground.contracts.MatchStatus
import com.trace.playground.contracts.RecognitionRequest
import com.trace.playground.contracts.ReferenceVector
import com.trace.playground.contracts.Roi
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class RecognitionAlgorithmTest {

    private val algorithm by lazy { RecognitionAlgorithm() }

    // ── Math utilities ───────────────────────────────────────────

    @Test
    fun `dotProduct returns 1 for identical L2-normalized vectors`() {
        val v = RecognitionAlgorithm.l2Normalize(floatArrayOf(1f, 2f, 3f))
        assertEquals(1f, RecognitionAlgorithm.dotProduct(v, v), 0.0001f)
    }

    @Test
    fun `dotProduct returns approximately 0 for orthogonal vectors`() {
        val a = RecognitionAlgorithm.l2Normalize(floatArrayOf(1f, 0f, 0f))
        val b = RecognitionAlgorithm.l2Normalize(floatArrayOf(0f, 1f, 0f))
        assertEquals(0f, RecognitionAlgorithm.dotProduct(a, b), 0.0001f)
    }

    @Test
    fun `dotProduct returns 0 for empty vectors`() {
        assertEquals(0f, RecognitionAlgorithm.dotProduct(floatArrayOf(), floatArrayOf()))
    }

    @Test
    fun `dotProduct returns 0 for mismatched sizes`() {
        assertEquals(0f, RecognitionAlgorithm.dotProduct(floatArrayOf(1f), floatArrayOf(1f, 2f)))
    }

    @Test
    fun `l2Normalize produces unit-length vector`() {
        val v = RecognitionAlgorithm.l2Normalize(floatArrayOf(3f, 4f))
        val norm = sqrt(v.sumOf { (it * it).toDouble() }).toFloat()
        assertTrue(abs(norm - 1.0f) < 0.001f, "L2 norm should be ~1.0 but was $norm")
    }

    @Test
    fun `l2Normalize handles zero vector`() {
        val v = RecognitionAlgorithm.l2Normalize(floatArrayOf(0f, 0f, 0f))
        // Should return the zero vector unchanged
        assertTrue(v.all { it == 0f })
    }

    // ── Embedding pipeline ───────────────────────────────────────

    @Test
    fun `extractEmbedding produces 576-dim L2-normalized vector`() {
        val image = colorfulTestImage(128)
        val embedding = algorithm.extractEmbedding(image)
        assertEquals(RecognitionAlgorithm.EMBEDDING_DIM, embedding.size)

        val norm = sqrt(embedding.sumOf { (it * it).toDouble() }).toFloat()
        assertTrue(abs(norm - 1.0f) < 0.01f, "Embedding L2 norm should be ~1.0 but was $norm")
    }

    @Test
    fun `same image produces deterministic embedding`() {
        val image = colorfulTestImage(128)
        val emb1 = algorithm.extractEmbedding(image)
        val emb2 = algorithm.extractEmbedding(image)
        val similarity = RecognitionAlgorithm.dotProduct(emb1, emb2)
        assertTrue(similarity > 0.999f, "Same image should produce identical embeddings, got $similarity")
    }

    @Test
    fun `imageToEmbeddingTensor produces correct size`() {
        val image = colorfulTestImage(100)
        val tensor = algorithm.imageToEmbeddingTensor(image)
        val expected = 3 * RecognitionAlgorithm.EMBEDDING_INPUT_SIZE * RecognitionAlgorithm.EMBEDDING_INPUT_SIZE
        assertEquals(expected, tensor.size, "Tensor should have 3*224*224 = $expected elements")
    }

    // ── Detection pipeline ───────────────────────────────────────

    @Test
    fun `detectObjects returns bounding boxes for scene image`() {
        // Use the real retrieval_scene image if available
        val sceneFile = findSampleFile("retrieval_scene.jpg")
        if (sceneFile != null) {
            val image = ImageIO.read(sceneFile)
            val boxes = algorithm.detectObjects(image)
            assertTrue(boxes.isNotEmpty(), "SSD v2 should detect objects in a scene image")
            boxes.forEach { box ->
                assertTrue(box.confidence >= RecognitionAlgorithm.DETECTION_THRESHOLD)
                assertTrue(box.xmin < box.xmax, "xmin should be < xmax")
                assertTrue(box.ymin < box.ymax, "ymin should be < ymax")
            }
        }
    }

    @Test
    fun `detectObjects returns list for blank image`() {
        // A blank white image — SSD may or may not detect anything, but should not crash
        val blank = BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB).apply {
            val g = createGraphics()
            g.color = Color.WHITE
            g.fillRect(0, 0, 300, 300)
            g.dispose()
        }
        val boxes = algorithm.detectObjects(blank)
        // Should not throw — result may be empty or have low-confidence detections
        assertNotNull(boxes)
    }

    @Test
    fun `imageToDetectionBytes produces correct NHWC size`() {
        val image = colorfulTestImage(200)
        val bytes = algorithm.imageToDetectionBytes(image)
        val expected = RecognitionAlgorithm.DETECTION_INPUT_SIZE *
            RecognitionAlgorithm.DETECTION_INPUT_SIZE * 3
        assertEquals(expected, bytes.size, "Detection bytes should have 300*300*3 elements")
    }

    // ── Crop logic ───────────────────────────────────────────────

    @Test
    fun `cropWithPadding produces valid sub-image`() {
        val image = colorfulTestImage(300)
        val box = BoundingBox(ymin = 0.2f, xmin = 0.3f, ymax = 0.6f, xmax = 0.7f, confidence = 0.9f)
        val crop = algorithm.cropWithPadding(image, box)
        assertTrue(crop.width > 0 && crop.height > 0, "Crop should have positive dimensions")
        // With padding, the crop should be larger than the raw box
        val rawWidth = ((box.xmax - box.xmin) * image.width).toInt()
        assertTrue(crop.width >= rawWidth, "Crop with padding should be >= raw box width")
    }

    // ── Validation & edge cases ──────────────────────────────────

    @Test
    fun `filters references with wrong model name`() = runBlocking {
        val jpeg = colorfulTestJpeg()
        val wrongModelRef = ReferenceVector(
            referenceId = "ref-1",
            objectId = "obj-1",
            tag = "wrong-model",
            values = List(576) { 0.01f },
            modelName = "some-other-model",
            modelVersion = "1",
        )
        val result = algorithm.recognize(
            RecognitionRequest(
                image = ImageInput(jpeg),
                references = listOf(wrongModelRef),
                minimumSimilarity = 0.5f,
            ),
        )
        // Should not match because model name doesn't match
        assertTrue(
            result.detections.all { it.status == MatchStatus.UNKNOWN },
            "Should return UNKNOWN for wrong model name",
        )
    }

    @Test
    fun `filters references with wrong embedding dimension`() = runBlocking {
        val jpeg = colorfulTestJpeg()
        val wrongDimRef = ReferenceVector(
            referenceId = "ref-1",
            objectId = "obj-1",
            tag = "wrong-dim",
            values = List(128) { 0.01f },  // 128 instead of 576
            modelName = RecognitionAlgorithm.MODEL_NAME,
            modelVersion = "1",
        )
        val result = algorithm.recognize(
            RecognitionRequest(
                image = ImageInput(jpeg),
                references = listOf(wrongDimRef),
                minimumSimilarity = 0.5f,
            ),
        )
        assertTrue(
            result.detections.all { it.status == MatchStatus.UNKNOWN },
            "Should return UNKNOWN for wrong dimension",
        )
    }

    @Test
    fun `returns UNKNOWN when no reference matches threshold`() = runBlocking {
        val jpeg = colorfulTestJpeg()
        // Create a reference with a very different vector
        val ref = ReferenceVector(
            referenceId = "ref-1",
            objectId = "obj-1",
            tag = "something",
            values = List(576) { if (it == 0) 1f else 0f },
            modelName = RecognitionAlgorithm.MODEL_NAME,
            modelVersion = "1",
        )
        val result = algorithm.recognize(
            RecognitionRequest(
                image = ImageInput(jpeg),
                references = listOf(ref),
                minimumSimilarity = 0.99f,  // Very high threshold
            ),
        )
        assertTrue(
            result.detections.any { it.status == MatchStatus.UNKNOWN },
            "Should have at least one UNKNOWN detection",
        )
    }

    @Test
    fun `returns UNKNOWN for empty reference list`() = runBlocking {
        val jpeg = colorfulTestJpeg()
        val result = algorithm.recognize(
            RecognitionRequest(
                image = ImageInput(jpeg),
                references = emptyList(),
                minimumSimilarity = 0.5f,
            ),
        )
        assertEquals(1, result.detections.size)
        assertEquals(MatchStatus.UNKNOWN, result.detections[0].status)
    }

    @Test
    fun `rejects invalid JPEG bytes`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            algorithm.recognize(
                RecognitionRequest(
                    image = ImageInput(byteArrayOf(0x00, 0x01, 0x02)),
                    references = emptyList(),
                ),
            )
        }
    }

    @Test
    fun `rejects minimumSimilarity out of range`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            algorithm.recognize(
                RecognitionRequest(
                    image = ImageInput(colorfulTestJpeg()),
                    references = emptyList(),
                    minimumSimilarity = 1.5f,
                ),
            )
        }
    }

    @Test
    fun `rejects maximumResults out of range`() = runBlocking {
        assertFailsWith<IllegalArgumentException> {
            algorithm.recognize(
                RecognitionRequest(
                    image = ImageInput(colorfulTestJpeg()),
                    references = emptyList(),
                    maximumResults = 0,
                ),
            )
        }
    }

    // ── Integration: dual pipeline ───────────────────────────────

    @Test
    fun `full recognition pipeline produces valid result`() = runBlocking {
        val jpeg = colorfulTestJpeg()
        // Create a reference from the same image to guarantee a match
        val image = ImageIO.read(ByteArrayInputStream(jpeg))
        val embedding = algorithm.extractEmbedding(image)

        val ref = ReferenceVector(
            referenceId = "ref-self",
            objectId = "obj-self",
            tag = "test-object",
            values = embedding.toList(),
            modelName = RecognitionAlgorithm.MODEL_NAME,
            modelVersion = RecognitionAlgorithm.MODEL_VERSION,
        )

        val result = algorithm.recognize(
            RecognitionRequest(
                image = ImageInput(jpeg),
                references = listOf(ref),
                minimumSimilarity = 0.5f,
            ),
        )

        assertTrue(result.detections.isNotEmpty(), "Should have detections")
        assertTrue(result.processingTimeMillis >= 0, "Processing time should be non-negative")
        assertEquals("${RecognitionAlgorithm.MODEL_NAME}:${RecognitionAlgorithm.MODEL_VERSION}", result.modelVersion)

        // The full-image pipeline should match the self-reference at high similarity
        val matched = result.detections.filter { it.status == MatchStatus.MATCHED }
        assertTrue(matched.isNotEmpty(), "Should have at least one MATCHED detection from self-reference")
        assertTrue(
            matched[0].similarity > 0.95f,
            "Self-reference should have high similarity, got ${matched[0].similarity}",
        )
    }

    // ── Helpers ───────────────────────────────────────────────────

    private fun colorfulTestImage(size: Int = 128): BufferedImage {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until size) {
            for (x in 0 until size) {
                image.setRGB(x, y, ((x * 2) shl 16) or ((y * 2) shl 8) or (((x + y) % 256)))
            }
        }
        return image
    }

    private fun colorfulTestJpeg(size: Int = 128): ByteArray {
        return ByteArrayOutputStream().use { output ->
            ImageIO.write(colorfulTestImage(size), "jpg", output)
            output.toByteArray()
        }
    }

    private fun findSampleFile(name: String): java.io.File? {
        val candidates = listOf(
            java.io.File("playground/data/samples/$name"),
            java.io.File("data/samples/$name"),
            java.io.File("../data/samples/$name"),
            java.io.File("../../playground/data/samples/$name"),
        )
        return candidates.firstOrNull { it.exists() }
    }

    private fun ByteArrayInputStream(bytes: ByteArray) = java.io.ByteArrayInputStream(bytes)
}
