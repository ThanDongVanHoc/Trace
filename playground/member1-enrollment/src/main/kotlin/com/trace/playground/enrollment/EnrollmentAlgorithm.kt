package com.trace.playground.enrollment

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.trace.playground.contracts.EnrollmentEngine
import com.trace.playground.contracts.EnrollmentRequest
import com.trace.playground.contracts.EnrollmentResult
import com.trace.playground.contracts.ReferenceVector
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.FloatBuffer
import java.nio.file.Path
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt

/**
 * One-shot Enrollment using MobileNetV3-Small (ONNX Runtime).
 *
 * Pipeline: JPEG bytes -> decode -> rotate -> crop ROI -> quality check
 *           -> resize 224x224 (bilinear) -> ImageNet normalize -> ONNX inference
 *           -> L2 normalize -> 576-dim embedding vector.
 *
 * Thành viên 1 chỉ thay đổi implementation này và test tương ứng.
 * HTTP, SQLite và Android không thuộc phạm vi của thuật toán enrollment.
 */
class EnrollmentAlgorithm(
    modelPath: Path = resolveDefaultModelPath(),
) : EnrollmentEngine, AutoCloseable {

    private val ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession: OrtSession = ortEnvironment.createSession(
        modelPath.toAbsolutePath().toString(),
        OrtSession.SessionOptions(),
    )

    override suspend fun enroll(request: EnrollmentRequest): EnrollmentResult {
        require(request.tag.trim().length in 1..80) { "tag must contain 1 to 80 characters" }
        require(request.roi.isValid) { "ROI must be normalized and non-empty" }

        val decoded = decodeJpeg(request.image.jpegBytes)
        val rotated = applyRotation(decoded, request.image.rotationDegrees)
        val crop = cropRoi(rotated, request)
        require(crop.width >= MIN_ROI_PIXELS && crop.height >= MIN_ROI_PIXELS) {
            "ROI must be at least $MIN_ROI_PIXELS x $MIN_ROI_PIXELS pixels"
        }

        val quality = qualityScore(crop)
        require(quality >= MIN_QUALITY_SCORE) {
            "ROI is too dark, too bright, or has too little visual information"
        }

        val tensor = imageToTensor(crop)
        val rawVector = runOnnxInference(tensor)
        val normalized = l2Normalize(rawVector)

        val objectId = UUID.randomUUID().toString()
        val referenceId = UUID.randomUUID().toString()
        val embedding = ReferenceVector(
            referenceId = referenceId,
            objectId = objectId,
            tag = request.tag.trim(),
            values = normalized.toList(),
            modelName = MODEL_NAME,
            modelVersion = MODEL_VERSION,
        )
        return EnrollmentResult(
            objectId = objectId,
            referenceId = referenceId,
            tag = request.tag.trim(),
            qualityScore = quality,
            embedding = embedding,
            warnings = emptyList(),
        )
    }

    override fun close() {
        ortSession.close()
        ortEnvironment.close()
    }

    // ──────────────────────────────────────────────────────────────
    // Image decoding
    // ──────────────────────────────────────────────────────────────

    private fun decodeJpeg(bytes: ByteArray): BufferedImage {
        require(bytes.size >= 4 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) {
            "image must be a valid JPEG"
        }
        return ImageIO.read(ByteArrayInputStream(bytes))
            ?: throw IllegalArgumentException("image cannot be decoded")
    }

    // ──────────────────────────────────────────────────────────────
    // Rotation handling (0, 90, 180, 270 degrees)
    // ──────────────────────────────────────────────────────────────

    private fun applyRotation(image: BufferedImage, degrees: Int): BufferedImage {
        val normalized = ((degrees % 360) + 360) % 360
        if (normalized == 0) return image

        val (outWidth, outHeight) = when (normalized) {
            90, 270 -> image.height to image.width
            else -> image.width to image.height
        }
        val rotated = BufferedImage(outWidth, outHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = rotated.createGraphics()
        val transform = AffineTransform()
        when (normalized) {
            90 -> {
                transform.translate(image.height.toDouble(), 0.0)
                transform.rotate(Math.toRadians(90.0))
            }
            180 -> {
                transform.translate(image.width.toDouble(), image.height.toDouble())
                transform.rotate(Math.toRadians(180.0))
            }
            270 -> {
                transform.translate(0.0, image.width.toDouble())
                transform.rotate(Math.toRadians(270.0))
            }
        }
        g2d.drawImage(image, transform, null)
        g2d.dispose()
        return rotated
    }

    // ──────────────────────────────────────────────────────────────
    // ROI cropping with normalized coordinates [0..1]
    // ──────────────────────────────────────────────────────────────

    private fun cropRoi(image: BufferedImage, request: EnrollmentRequest): BufferedImage {
        val left = floor(request.roi.left * image.width).toInt().coerceIn(0, image.width - 1)
        val top = floor(request.roi.top * image.height).toInt().coerceIn(0, image.height - 1)
        val right = floor(request.roi.right * image.width).toInt().coerceIn(left + 1, image.width)
        val bottom = floor(request.roi.bottom * image.height).toInt().coerceIn(top + 1, image.height)
        return image.getSubimage(left, top, right - left, bottom - top)
    }

    // ──────────────────────────────────────────────────────────────
    // Quality assessment (brightness + contrast, no Laplacian)
    // ──────────────────────────────────────────────────────────────

    /**
     * Evaluates image quality based on brightness and contrast.
     *
     * Args:
     *     image (BufferedImage): The cropped ROI image to evaluate.
     *
     * Returns:
     *     Float: Quality score between 0.0 and 1.0.
     *
     * Raises:
     *     Nothing — caller checks the returned score against MIN_QUALITY_SCORE.
     */
    internal fun qualityScore(image: BufferedImage): Float {
        val pixelCount = image.width * image.height
        var sumBrightness = 0.0
        val brightnesses = DoubleArray(pixelCount)
        var index = 0

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val rgb = image.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                val brightness = r * 0.299 + g * 0.587 + b * 0.114
                brightnesses[index++] = brightness
                sumBrightness += brightness
            }
        }

        val mean = sumBrightness / pixelCount
        val variance = brightnesses.sumOf { (it - mean) * (it - mean) } / pixelCount

        // Reject too dark (mean < 25) or too bright (mean > 230)
        if (mean < MIN_BRIGHTNESS || mean > MAX_BRIGHTNESS) return 0.0f
        // Reject flat/uniform images (variance < 100)
        if (variance < MIN_VARIANCE) return 0.0f

        val exposure = (1.0 - abs(mean - 127.5) / 127.5).coerceIn(0.0, 1.0)
        val detail = (variance / 4_000.0).coerceIn(0.0, 1.0)
        return (exposure * 0.4 + detail * 0.6).toFloat()
    }

    // ──────────────────────────────────────────────────────────────
    // Image to Tensor conversion (platform-specific: Desktop JVM)
    //
    // NOTE for Android migration:
    //   Replace this function with a Bitmap-based version using
    //   Bitmap.getPixels(). The ONNX inference logic below remains
    //   unchanged since it only operates on FloatArray tensors.
    // ──────────────────────────────────────────────────────────────

    /**
     * Converts a BufferedImage to a CHW float tensor normalized with ImageNet statistics.
     *
     * Args:
     *     image (BufferedImage): Source image (any size).
     *
     * Returns:
     *     FloatArray: Tensor of shape [1, 3, 224, 224] flattened to 1-D array (150528 floats).
     *         Channel order: CHW (Channel-Height-Width).
     *         Normalization: (pixel / 255.0 - mean) / std with ImageNet mean/std.
     */
    internal fun imageToTensor(image: BufferedImage): FloatArray {
        // Resize to 224x224 using bilinear interpolation (NOT nearest-neighbor)
        val resized = BufferedImage(INPUT_SIZE, INPUT_SIZE, BufferedImage.TYPE_INT_RGB)
        val g2d: Graphics2D = resized.createGraphics()
        g2d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
        )
        g2d.drawImage(image, 0, 0, INPUT_SIZE, INPUT_SIZE, null)
        g2d.dispose()

        // Convert to CHW tensor with ImageNet normalization
        val tensor = FloatArray(3 * INPUT_SIZE * INPUT_SIZE)
        val channelStride = INPUT_SIZE * INPUT_SIZE

        for (y in 0 until INPUT_SIZE) {
            for (x in 0 until INPUT_SIZE) {
                val rgb = resized.getRGB(x, y)
                val pixelIndex = y * INPUT_SIZE + x

                // Red channel
                val r = ((rgb shr 16) and 0xFF) / 255.0f
                tensor[pixelIndex] = (r - IMAGENET_MEAN_R) / IMAGENET_STD_R

                // Green channel
                val g = ((rgb shr 8) and 0xFF) / 255.0f
                tensor[channelStride + pixelIndex] = (g - IMAGENET_MEAN_G) / IMAGENET_STD_G

                // Blue channel
                val b = (rgb and 0xFF) / 255.0f
                tensor[2 * channelStride + pixelIndex] = (b - IMAGENET_MEAN_B) / IMAGENET_STD_B
            }
        }
        return tensor
    }

    // ──────────────────────────────────────────────────────────────
    // ONNX Runtime inference (platform-independent)
    // ──────────────────────────────────────────────────────────────

    /**
     * Runs MobileNetV3-Small inference on a pre-processed image tensor.
     *
     * Args:
     *     inputTensor (FloatArray): Image tensor of shape [1, 3, 224, 224] flattened.
     *
     * Returns:
     *     FloatArray: Raw embedding vector (576 dimensions before L2 normalization).
     *         Output is flattened from potential shapes like [1, 576] or [1, 576, 1, 1].
     */
    internal fun runOnnxInference(inputTensor: FloatArray): FloatArray {
        val shape = longArrayOf(1, 3, INPUT_SIZE.toLong(), INPUT_SIZE.toLong())
        OnnxTensor.createTensor(ortEnvironment, FloatBuffer.wrap(inputTensor), shape).use { tensor ->
            ortSession.run(mapOf("input" to tensor)).use { result ->
                val output = result[0].value
                // Handle potential shapes: [1, 576] or [1, 576, 1, 1]
                return flattenOutput(output)
            }
        }
    }

    /**
     * Flattens ONNX output from any nested array shape to a 1-D FloatArray.
     *
     * Handles output shapes like [1, 576], [1, 576, 1, 1], or plain float[][].
     */
    @Suppress("UNCHECKED_CAST")
    private fun flattenOutput(output: Any): FloatArray {
        return when (output) {
            is FloatArray -> output
            is Array<*> -> {
                // Recursively flatten nested arrays (e.g. Array<FloatArray> for [1, 576])
                val flat = mutableListOf<Float>()
                flattenRecursive(output, flat)
                flat.toFloatArray()
            }
            else -> throw IllegalStateException(
                "Unexpected ONNX output type: ${output::class.java.name}",
            )
        }
    }

    private fun flattenRecursive(array: Any, result: MutableList<Float>) {
        when (array) {
            is FloatArray -> array.forEach { result.add(it) }
            is Array<*> -> array.forEach { element ->
                if (element != null) flattenRecursive(element, result)
            }
            is Float -> result.add(array)
            else -> throw IllegalStateException(
                "Unexpected nested type in ONNX output: ${array::class.java.name}",
            )
        }
    }

    companion object {
        const val MODEL_NAME = "mobilenet-v3-small"
        const val MODEL_VERSION = "1"
        const val EMBEDDING_DIM = 576

        private const val INPUT_SIZE = 224
        private const val MIN_ROI_PIXELS = 32
        private const val MIN_QUALITY_SCORE = 0.08f
        private const val MIN_BRIGHTNESS = 25.0
        private const val MAX_BRIGHTNESS = 230.0
        private const val MIN_VARIANCE = 100.0

        // ImageNet normalization constants (RGB order)
        private const val IMAGENET_MEAN_R = 0.485f
        private const val IMAGENET_MEAN_G = 0.456f
        private const val IMAGENET_MEAN_B = 0.406f
        private const val IMAGENET_STD_R = 0.229f
        private const val IMAGENET_STD_G = 0.224f
        private const val IMAGENET_STD_B = 0.225f

        /**
         * Normalizes a vector to unit length (L2 norm = 1.0).
         *
         * After normalization, cosine similarity between two vectors
         * equals their dot product: cos(A, B) = A · B.
         */
        fun l2Normalize(values: FloatArray): FloatArray {
            val norm = sqrt(values.sumOf { (it * it).toDouble() }).toFloat()
            return if (norm == 0f) values else FloatArray(values.size) { values[it] / norm }
        }

        /**
         * Computes cosine similarity between two L2-normalized vectors.
         *
         * Since both vectors are L2-normalized, this is a simple dot product.
         */
        fun cosineSimilarity(left: FloatArray, right: FloatArray): Float {
            require(left.size == right.size && left.isNotEmpty()) {
                "vectors must have equal non-zero size"
            }
            var dot = 0.0
            for (i in left.indices) {
                dot += left[i] * right[i]
            }
            return dot.toFloat().coerceIn(-1f, 1f)
        }

        /**
         * Resolves the default model path relative to the playground data directory.
         *
         * Searches for the model file in this order:
         * 1. playground/data/models/mobilenet_v3_small.onnx
         * 2. data/models/mobilenet_v3_small.onnx (relative to CWD)
         */
        fun resolveDefaultModelPath(): Path {
            val fromProp = System.getProperty("trace.data.dir")?.let {
                Path.of(it, "models", "mobilenet_v3_small.onnx")
            }
            val candidates = listOfNotNull(
                fromProp,
                Path.of("playground", "data", "models", "mobilenet_v3_small.onnx"),
                Path.of("data", "models", "mobilenet_v3_small.onnx"),
                Path.of("..", "data", "models", "mobilenet_v3_small.onnx"),
                Path.of("..", "..", "playground", "data", "models", "mobilenet_v3_small.onnx"),
            )
            return candidates.firstOrNull { it.toFile().exists() }
                ?: throw IllegalStateException(
                    "Model file not found. Expected at: ${candidates.joinToString(" or ")}",
                )
        }
    }
}
