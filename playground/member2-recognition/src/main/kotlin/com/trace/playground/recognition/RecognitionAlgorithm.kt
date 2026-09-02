package com.trace.playground.recognition

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.trace.playground.contracts.Detection
import com.trace.playground.contracts.MatchStatus
import com.trace.playground.contracts.RecognitionEngine
import com.trace.playground.contracts.RecognitionRequest
import com.trace.playground.contracts.RecognitionResult
import com.trace.playground.contracts.ReferenceVector
import com.trace.playground.contracts.Roi
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

/**
 * One-shot Recognition using a dual-pipeline approach:
 *
 * Pipeline A (Full-image): Embed the entire scene image and match against references.
 * Pipeline B (Detect+Crop): Use MobileNet SSD v2 to detect objects, crop each bounding box,
 *   embed the crop with MobileNetV3-Small, and match against references.
 *
 * The final result merges detections from both pipelines, deduplicates by objectId
 * (keeping the highest similarity), and returns the top results.
 *
 * Thành viên 2 — phạm vi: chỉ sửa file trong playground/member2-recognition.
 */
class RecognitionAlgorithm(
    embeddingModelPath: Path = resolveModelPath(EMBEDDING_MODEL_FILE),
    detectionModelPath: Path = resolveModelPath(DETECTION_MODEL_FILE),
) : RecognitionEngine, AutoCloseable {

    private val ortEnvironment: OrtEnvironment = OrtEnvironment.getEnvironment()

    // Session #1: MobileNetV3-Small — feature extraction (576-dim embedding)
    private val embeddingSession: OrtSession = ortEnvironment.createSession(
        embeddingModelPath.toAbsolutePath().toString(),
        OrtSession.SessionOptions(),
    )

    // Session #2: SSD MobileNet v2 — object detection (bounding boxes)
    private val detectionSession: OrtSession = ortEnvironment.createSession(
        detectionModelPath.toAbsolutePath().toString(),
        OrtSession.SessionOptions(),
    )

    // Cache the actual I/O names from the detection model for robustness
    private val detectionInputName: String = detectionSession.inputNames.first()

    // ──────────────────────────────────────────────────────────────
    // Public API — implements RecognitionEngine
    // ──────────────────────────────────────────────────────────────

    override suspend fun recognize(request: RecognitionRequest): RecognitionResult {
        require(request.minimumSimilarity in 0f..1f) { "minimumSimilarity must be between 0 and 1" }
        require(request.maximumResults in 1..50) { "maximumResults must be between 1 and 50" }

        var detections: List<Detection>
        val elapsed = measureTimeMillis {
            val image = decodeJpeg(request.image.jpegBytes)

            // Filter references: only keep those with matching model and dimension
            val validReferences = request.references.filter {
                it.modelName == MODEL_NAME && it.values.size == EMBEDDING_DIM
            }

            // Pipeline A: full-image embedding
            val fullImageDetections = runFullImagePipeline(
                image, validReferences, request.minimumSimilarity,
            )

            // Pipeline B: SSD detection → crop → embed → match
            val cropDetections = runDetectionCropPipeline(
                image, validReferences, request.minimumSimilarity,
            )

            // Merge results from both pipelines
            detections = mergeDetections(
                fullImageDetections + cropDetections,
                request.maximumResults,
            )

            if (detections.isEmpty()) {
                detections = listOf(Detection(similarity = 0f, status = MatchStatus.UNKNOWN))
            }
        }

        return RecognitionResult(
            detections = detections,
            processingTimeMillis = elapsed,
            modelVersion = "$MODEL_NAME:$MODEL_VERSION",
            warnings = emptyList(),
        )
    }

    override fun close() {
        embeddingSession.close()
        detectionSession.close()
        ortEnvironment.close()
    }

    // ──────────────────────────────────────────────────────────────
    // Pipeline A: Full-Image Embedding
    // ──────────────────────────────────────────────────────────────

    private fun runFullImagePipeline(
        image: BufferedImage,
        references: List<ReferenceVector>,
        threshold: Float,
    ): List<Detection> {
        val embedding = extractEmbedding(image)
        return matchAgainstReferences(embedding, references, threshold, boundingBox = null)
    }

    // ──────────────────────────────────────────────────────────────
    // Pipeline B: SSD Detection → Crop → Embed → Match
    // ──────────────────────────────────────────────────────────────

    private fun runDetectionCropPipeline(
        image: BufferedImage,
        references: List<ReferenceVector>,
        threshold: Float,
    ): List<Detection> {
        val boxes = detectObjects(image)
        if (boxes.isEmpty()) return emptyList()

        return boxes.flatMap { box ->
            val crop = cropWithPadding(image, box)
            if (crop.width < MIN_CROP_SIZE || crop.height < MIN_CROP_SIZE) {
                return@flatMap emptyList<Detection>()
            }
            val embedding = extractEmbedding(crop)
            val roi = Roi(
                left = box.xmin.coerceIn(0f, 1f),
                top = box.ymin.coerceIn(0f, 1f),
                right = box.xmax.coerceIn(0f, 1f),
                bottom = box.ymax.coerceIn(0f, 1f),
            )
            matchAgainstReferences(embedding, references, threshold, boundingBox = roi)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // SSD MobileNet v2 — Object Detection
    // ──────────────────────────────────────────────────────────────

    /**
     * Runs SSD MobileNet v2 inference on the image.
     * Returns a list of bounding boxes with confidence >= DETECTION_THRESHOLD.
     *
     * SSD v2 expects input shape [1, 300, 300, 3] NHWC, pixel values in [0, 1].
     * Outputs: detection_boxes [1, N, 4] (ymin, xmin, ymax, xmax normalized),
     *          detection_scores [1, N], detection_classes [1, N], num_detections [1].
     */
    internal fun detectObjects(image: BufferedImage): List<BoundingBox> {
        val bytes = imageToDetectionBytes(image)
        val shape = longArrayOf(1, DETECTION_INPUT_SIZE.toLong(), DETECTION_INPUT_SIZE.toLong(), 3)

        OnnxTensor.createTensor(ortEnvironment, ByteBuffer.wrap(bytes), shape, OnnxJavaType.UINT8).use { inputTensor ->
            detectionSession.run(mapOf(detectionInputName to inputTensor)).use { result ->
                return parseDetectionOutput(result)
            }
        }
    }

    /**
     * Parses the SSD output tensors into a list of BoundingBox objects.
     * Handles both common output formats from tf2onnx conversion.
     */
    private fun parseDetectionOutput(result: OrtSession.Result): List<BoundingBox> {
        val outputNames = result.map { it.key }.toSet()

        // Standard TF detection output format
        val boxes: Array<FloatArray>
        val scores: FloatArray
        val numDetections: Int

        // Try to find outputs by common name patterns
        val boxesOutput = findOutput(result, "detection_boxes", 0)
        val scoresOutput = findOutput(result, "detection_scores", 1)
        val numDetOutput = findOutput(result, "num_detections", 2)

        // Parse boxes — expected shape [1, N, 4] → Array<FloatArray>
        boxes = when (val v = boxesOutput) {
            is Array<*> -> {
                @Suppress("UNCHECKED_CAST")
                when (val inner = v[0]) {
                    is Array<*> -> (inner as Array<FloatArray>)
                    is FloatArray -> {
                        // Flat [N*4], reshape to [N, 4]
                        val n = inner.size / 4
                        Array(n) { i -> FloatArray(4) { j -> inner[i * 4 + j] } }
                    }
                    else -> return emptyList()
                }
            }
            else -> return emptyList()
        }

        // Parse scores — expected shape [1, N] → FloatArray
        scores = when (val v = scoresOutput) {
            is Array<*> -> {
                when (val inner = v[0]) {
                    is FloatArray -> inner
                    else -> return emptyList()
                }
            }
            is FloatArray -> v
            else -> return emptyList()
        }

        // Parse num_detections
        numDetections = when (val v = numDetOutput) {
            is FloatArray -> v[0].toInt()
            is Array<*> -> {
                when (val inner = v[0]) {
                    is Float -> inner.toInt()
                    is FloatArray -> inner[0].toInt()
                    else -> scores.size
                }
            }
            else -> scores.size
        }

        val count = minOf(numDetections, boxes.size, scores.size, MAX_DETECTIONS_PER_IMAGE)
        return (0 until count)
            .filter { scores[it] >= DETECTION_THRESHOLD }
            .map { i ->
                BoundingBox(
                    ymin = boxes[i][0],
                    xmin = boxes[i][1],
                    ymax = boxes[i][2],
                    xmax = boxes[i][3],
                    confidence = scores[i],
                )
            }
    }

    /**
     * Finds an output tensor by name pattern or falls back to positional index.
     */
    private fun findOutput(result: OrtSession.Result, nameHint: String, fallbackIndex: Int): Any? {
        // Try by name first
        for (entry in result) {
            if (entry.key.contains(nameHint, ignoreCase = true)) {
                return entry.value.value
            }
        }
        // Fallback to positional index
        val entries = result.toList()
        return if (fallbackIndex < entries.size) entries[fallbackIndex].value.value else null
    }

    // ──────────────────────────────────────────────────────────────
    // MobileNetV3-Small — Feature Extraction (576-dim embedding)
    // ──────────────────────────────────────────────────────────────

    /**
     * Extracts a 576-dim L2-normalized embedding from an image.
     *
     * Pipeline: image → resize 224×224 (bilinear) → ImageNet normalize (CHW)
     *           → ONNX inference → flatten → L2 normalize.
     */
    internal fun extractEmbedding(image: BufferedImage): FloatArray {
        val tensor = imageToEmbeddingTensor(image)
        val raw = runEmbeddingInference(tensor)
        return l2Normalize(raw)
    }

    private fun runEmbeddingInference(tensor: FloatArray): FloatArray {
        val shape = longArrayOf(1, 3, EMBEDDING_INPUT_SIZE.toLong(), EMBEDDING_INPUT_SIZE.toLong())
        OnnxTensor.createTensor(ortEnvironment, FloatBuffer.wrap(tensor), shape).use { inputTensor ->
            embeddingSession.run(mapOf("input" to inputTensor)).use { result ->
                return flattenOutput(result[0].value)
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Tensor Preprocessing
    // ──────────────────────────────────────────────────────────────

    /**
     * Converts an image to SSD MobileNet v2 input byte array.
     * Output: NHWC uint8 [1, 300, 300, 3], pixel values in [0, 255].
     */
    internal fun imageToDetectionBytes(image: BufferedImage): ByteArray {
        val resized = resizeBilinear(image, DETECTION_INPUT_SIZE, DETECTION_INPUT_SIZE)
        val bytes = ByteArray(DETECTION_INPUT_SIZE * DETECTION_INPUT_SIZE * 3)
        var idx = 0
        for (y in 0 until DETECTION_INPUT_SIZE) {
            for (x in 0 until DETECTION_INPUT_SIZE) {
                val rgb = resized.getRGB(x, y)
                bytes[idx++] = ((rgb shr 16) and 0xFF).toByte()  // R
                bytes[idx++] = ((rgb shr 8) and 0xFF).toByte()   // G
                bytes[idx++] = (rgb and 0xFF).toByte()           // B
            }
        }
        return bytes
    }

    /**
     * Converts an image to MobileNetV3-Small input tensor.
     * Output: CHW Float32 [1, 3, 224, 224], ImageNet normalized.
     */
    internal fun imageToEmbeddingTensor(image: BufferedImage): FloatArray {
        val resized = resizeBilinear(image, EMBEDDING_INPUT_SIZE, EMBEDDING_INPUT_SIZE)
        val tensor = FloatArray(3 * EMBEDDING_INPUT_SIZE * EMBEDDING_INPUT_SIZE)
        val stride = EMBEDDING_INPUT_SIZE * EMBEDDING_INPUT_SIZE

        for (y in 0 until EMBEDDING_INPUT_SIZE) {
            for (x in 0 until EMBEDDING_INPUT_SIZE) {
                val rgb = resized.getRGB(x, y)
                val idx = y * EMBEDDING_INPUT_SIZE + x
                val r = ((rgb shr 16) and 0xFF) / 255.0f
                val g = ((rgb shr 8) and 0xFF) / 255.0f
                val b = (rgb and 0xFF) / 255.0f
                tensor[idx] = (r - IMAGENET_MEAN_R) / IMAGENET_STD_R
                tensor[stride + idx] = (g - IMAGENET_MEAN_G) / IMAGENET_STD_G
                tensor[2 * stride + idx] = (b - IMAGENET_MEAN_B) / IMAGENET_STD_B
            }
        }
        return tensor
    }

    // ──────────────────────────────────────────────────────────────
    // Image Utilities
    // ──────────────────────────────────────────────────────────────

    private fun decodeJpeg(bytes: ByteArray): BufferedImage {
        return ImageIO.read(ByteArrayInputStream(bytes))
            ?: throw IllegalArgumentException("image must be a valid JPEG")
    }

    /**
     * Resizes image using bilinear interpolation.
     */
    private fun resizeBilinear(image: BufferedImage, width: Int, height: Int): BufferedImage {
        val resized = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d: Graphics2D = resized.createGraphics()
        g2d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
        )
        g2d.drawImage(image, 0, 0, width, height, null)
        g2d.dispose()
        return resized
    }

    /**
     * Crops a region from the image based on a bounding box in normalized coordinates.
     * Adds padding around the box to capture context (default 10%).
     */
    internal fun cropWithPadding(
        image: BufferedImage,
        box: BoundingBox,
        paddingFraction: Float = CROP_PADDING,
    ): BufferedImage {
        val boxWidth = box.xmax - box.xmin
        val boxHeight = box.ymax - box.ymin
        val padX = boxWidth * paddingFraction
        val padY = boxHeight * paddingFraction

        val left = ((box.xmin - padX) * image.width).toInt().coerceIn(0, image.width - 1)
        val top = ((box.ymin - padY) * image.height).toInt().coerceIn(0, image.height - 1)
        val right = ((box.xmax + padX) * image.width).toInt().coerceIn(left + 1, image.width)
        val bottom = ((box.ymax + padY) * image.height).toInt().coerceIn(top + 1, image.height)

        return image.getSubimage(left, top, right - left, bottom - top)
    }

    // ──────────────────────────────────────────────────────────────
    // Matching Logic
    // ──────────────────────────────────────────────────────────────

    /**
     * Matches a single embedding against all valid references.
     * Returns a Detection for each reference, labeled MATCHED or UNKNOWN.
     */
    private fun matchAgainstReferences(
        embedding: FloatArray,
        references: List<ReferenceVector>,
        threshold: Float,
        boundingBox: Roi?,
    ): List<Detection> {
        return references.map { ref ->
            val similarity = dotProduct(embedding, ref.values.toFloatArray())
            val matched = similarity >= threshold
            Detection(
                objectId = if (matched) ref.objectId else null,
                tag = if (matched) ref.tag else null,
                similarity = similarity,
                status = if (matched) MatchStatus.MATCHED else MatchStatus.UNKNOWN,
                boundingBox = boundingBox,
            )
        }
    }

    /**
     * Merges detections from both pipelines:
     * 1. Sort by similarity descending
     * 2. Deduplicate by objectId (keep highest similarity)
     * 3. Take top N results
     */
    private fun mergeDetections(
        detections: List<Detection>,
        maxResults: Int,
    ): List<Detection> {
        val sorted = detections.sortedByDescending { it.similarity }

        // Deduplicate: for MATCHED detections with same objectId, keep only the best
        val seen = mutableSetOf<String>()
        val merged = mutableListOf<Detection>()
        for (d in sorted) {
            val objectId = d.objectId
            if (d.status == MatchStatus.MATCHED && objectId != null) {
                if (seen.add(objectId)) {
                    merged.add(d)
                }
            } else if (d.status == MatchStatus.UNKNOWN) {
                // Only keep the best UNKNOWN
                if (merged.none { it.status == MatchStatus.UNKNOWN }) {
                    merged.add(d)
                }
            }
        }
        return merged.take(maxResults)
    }

    // ──────────────────────────────────────────────────────────────
    // Math Utilities
    // ──────────────────────────────────────────────────────────────

    /**
     * Flattens ONNX output from nested array shapes to a 1-D FloatArray.
     * Handles shapes like [1, 576], [1, 576, 1, 1], or plain FloatArray.
     */
    @Suppress("UNCHECKED_CAST")
    private fun flattenOutput(output: Any): FloatArray {
        return when (output) {
            is FloatArray -> output
            is Array<*> -> {
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
        // ── Embedding model (MobileNetV3-Small) ─────────────────
        const val MODEL_NAME = "mobilenet-v3-small"
        const val MODEL_VERSION = "1"
        const val EMBEDDING_DIM = 576
        internal const val EMBEDDING_INPUT_SIZE = 224
        internal const val EMBEDDING_MODEL_FILE = "mobilenet_v3_small.onnx"

        // ImageNet normalization constants
        private const val IMAGENET_MEAN_R = 0.485f
        private const val IMAGENET_MEAN_G = 0.456f
        private const val IMAGENET_MEAN_B = 0.406f
        private const val IMAGENET_STD_R = 0.229f
        private const val IMAGENET_STD_G = 0.224f
        private const val IMAGENET_STD_B = 0.225f

        // ── Detection model (SSD MobileNet v2) ─────────────────
        internal const val DETECTION_INPUT_SIZE = 300
        internal const val DETECTION_MODEL_FILE = "ssd_mobilenet_v2.onnx"
        internal const val DETECTION_THRESHOLD = 0.3f
        internal const val MAX_DETECTIONS_PER_IMAGE = 20

        // ── Crop settings ───────────────────────────────────────
        internal const val CROP_PADDING = 0.10f
        internal const val MIN_CROP_SIZE = 32

        /**
         * Dot product of two L2-normalized vectors = cosine similarity.
         */
        fun dotProduct(a: FloatArray, b: FloatArray): Float {
            if (a.size != b.size || a.isEmpty()) return 0f
            var sum = 0.0f
            for (i in a.indices) sum += a[i] * b[i]
            return sum.coerceIn(-1f, 1f)
        }

        /**
         * L2-normalizes a vector to unit length.
         */
        fun l2Normalize(vec: FloatArray): FloatArray {
            val norm = sqrt(vec.sumOf { (it * it).toDouble() }).toFloat()
            return if (norm == 0f) vec else FloatArray(vec.size) { vec[it] / norm }
        }

        /**
         * Resolves a model file path, searching common locations relative to CWD.
         */
        fun resolveModelPath(filename: String): Path {
            val fromProp = System.getProperty("trace.data.dir")?.let {
                Path.of(it, "models", filename)
            }
            val candidates = listOfNotNull(
                fromProp,
                Path.of("playground", "data", "models", filename),
                Path.of("data", "models", filename),
                Path.of("..", "data", "models", filename),
                Path.of("..", "..", "playground", "data", "models", filename),
            )
            return candidates.firstOrNull { it.toFile().exists() }
                ?: throw IllegalStateException(
                    "Model file '$filename' not found. Expected at: ${candidates.joinToString(" or ")}",
                )
        }
    }
}

/**
 * Represents a detected bounding box from SSD MobileNet v2.
 * Coordinates are normalized to [0, 1] range.
 */
data class BoundingBox(
    val ymin: Float,
    val xmin: Float,
    val ymax: Float,
    val xmax: Float,
    val confidence: Float,
)
