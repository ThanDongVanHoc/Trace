package com.traceapp.feature.recognition

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.traceapp.core.contracts.ImageInput
import com.traceapp.core.contracts.MatchStatus
import com.traceapp.core.contracts.NormalizedRect
import com.traceapp.core.contracts.ObjectDetection
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.RecognizeRequest
import com.traceapp.core.contracts.RecognizeResponse
import com.traceapp.core.contracts.RecognitionApi
import com.traceapp.core.contracts.TraceError
import com.traceapp.core.contracts.TraceErrorCode
import com.traceapp.core.contracts.TraceResult
import com.traceapp.core.contracts.VisualEmbedding
import com.traceapp.core.contracts.VisualEncoder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** MobileNetV3 embedding plus SSD MobileNet v2 detection, fully on device. */
@Singleton
class OnDeviceVisualEngine @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : VisualEncoder, RecognitionApi {
    private val runtime by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { createRuntime() }

    override suspend fun encode(
        image: ImageInput,
        roi: NormalizedRect?,
    ): TraceResult<VisualEmbedding> = withContext(Dispatchers.Default) {
        try {
            val decoded = decodeAndRotate(image)
            val crop = roi?.let { cropNormalized(decoded, it) } ?: decoded
            if (crop.width < MIN_CROP_SIZE || crop.height < MIN_CROP_SIZE) {
                return@withContext failure(TraceErrorCode.ROI_TOO_SMALL, "Vùng chọn quá nhỏ để nhận diện.")
            }
            val quality = assessQuality(crop)
            if (quality.reason != null) {
                return@withContext failure(quality.reason, quality.message)
            }
            TraceResult.Success(
                VisualEmbedding(
                    values = extractEmbedding(crop),
                    modelName = MODEL_NAME,
                    modelVersion = MODEL_VERSION,
                    qualityScore = quality.score,
                ),
            )
        } catch (failure: Exception) {
            modelFailure(failure)
        }
    }

    override suspend fun recognize(request: RecognizeRequest): TraceResult<RecognizeResponse> =
        withContext(Dispatchers.Default) {
            if (request.minimumSimilarity !in 0f..1f || request.maximumResults !in 1..20) {
                return@withContext failure(TraceErrorCode.INVALID_INPUT, "Ngưỡng nhận diện không hợp lệ.")
            }
            try {
                val references = request.references.filter { reference ->
                    reference.embeddings.any {
                        it.modelName == MODEL_NAME &&
                            it.modelVersion == MODEL_VERSION &&
                            it.values.size == EMBEDDING_DIMENSIONS
                    }
                }
                if (references.isEmpty()) {
                    return@withContext failure(
                        TraceErrorCode.MODEL_MISMATCH,
                        "Dữ liệu đã lưu không tương thích với model trên thiết bị.",
                    )
                }
                val startedAt = System.nanoTime()
                val image = decodeAndRotate(request.image)
                val candidates = mutableListOf<ObjectDetection>()
                candidates += match(
                    extractEmbedding(image),
                    references,
                    request.minimumSimilarity,
                    NormalizedRect.FullImage,
                )

                // Detection is an enhancement. Full-image matching still works if SSD cannot produce boxes.
                runCatching { detectObjects(image) }.getOrDefault(emptyList()).forEach { box ->
                    val crop = cropWithPadding(image, box)
                    if (crop.width >= MIN_CROP_SIZE && crop.height >= MIN_CROP_SIZE) {
                        candidates += match(
                            extractEmbedding(crop),
                            references,
                            request.minimumSimilarity,
                            box.toRect(),
                        )
                    }
                }

                val detections = merge(candidates, request.maximumResults)
                TraceResult.Success(
                    RecognizeResponse(
                        detections = detections.ifEmpty {
                            listOf(ObjectDetection(null, null, 0f, MatchStatus.UNKNOWN))
                        },
                        processingTimeMillis = (System.nanoTime() - startedAt) / 1_000_000,
                        modelVersion = "$MODEL_NAME:$MODEL_VERSION",
                    ),
                )
            } catch (failure: Exception) {
                modelFailure(failure)
            }
        }

    private fun createRuntime(): ModelRuntime {
        val environment = OrtEnvironment.getEnvironment()
        val embeddingModel = installModel(EMBEDDING_ASSET, "mobilenet_v3_small_v1.onnx")
        val detectionModel = installModel(DETECTION_ASSET, "ssd_mobilenet_v2_v1.onnx")
        val options = OrtSession.SessionOptions().apply {
            setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
        }
        return ModelRuntime(
            environment = environment,
            embedding = environment.createSession(embeddingModel.absolutePath, options),
            detection = environment.createSession(detectionModel.absolutePath, options),
        )
    }

    private fun installModel(assetPath: String, installedName: String): File {
        val directory = File(context.noBackupFilesDir, "models-v1").apply { mkdirs() }
        val target = File(directory, installedName)
        if (target.isFile && target.length() > 0) return target
        val temporary = File.createTempFile("model-", ".tmp", directory)
        try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(temporary).use { output ->
                    input.copyTo(output)
                    output.fd.sync()
                }
            }
            check(temporary.renameTo(target)) { "Could not install on-device model" }
        } finally {
            temporary.delete()
        }
        return target
    }

    private fun decodeAndRotate(image: ImageInput): Bitmap {
        require(image.jpegBytes.isNotEmpty())
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(image.jpegBytes, 0, image.jpegBytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0)
        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_DECODE_DIMENSION) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = BitmapFactory.decodeByteArray(image.jpegBytes, 0, image.jpegBytes.size, options)
            ?: throw IllegalArgumentException("Ảnh JPEG không đọc được.")
        val degrees = ((image.rotationDegrees % 360) + 360) % 360
        if (degrees == 0) return decoded
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    }

    private fun cropNormalized(image: Bitmap, roi: NormalizedRect): Bitmap {
        require(roi.isValid)
        val left = floor(roi.left * image.width).toInt().coerceIn(0, image.width - 1)
        val top = floor(roi.top * image.height).toInt().coerceIn(0, image.height - 1)
        val right = floor(roi.right * image.width).toInt().coerceIn(left + 1, image.width)
        val bottom = floor(roi.bottom * image.height).toInt().coerceIn(top + 1, image.height)
        return Bitmap.createBitmap(image, left, top, right - left, bottom - top)
    }

    private fun assessQuality(image: Bitmap): Quality {
        val sample = Bitmap.createScaledBitmap(image, QUALITY_SAMPLE_SIZE, QUALITY_SAMPLE_SIZE, true)
        val pixels = IntArray(QUALITY_SAMPLE_SIZE * QUALITY_SAMPLE_SIZE)
        sample.getPixels(pixels, 0, QUALITY_SAMPLE_SIZE, 0, 0, QUALITY_SAMPLE_SIZE, QUALITY_SAMPLE_SIZE)
        val brightness = DoubleArray(pixels.size)
        var sum = 0.0
        pixels.forEachIndexed { index, pixel ->
            val value = ((pixel shr 16 and 0xFF) * 0.299) +
                ((pixel shr 8 and 0xFF) * 0.587) + (pixel and 0xFF) * 0.114
            brightness[index] = value
            sum += value
        }
        val mean = sum / brightness.size
        if (mean < MIN_BRIGHTNESS) {
            return Quality(0f, TraceErrorCode.IMAGE_TOO_DARK, "Vùng chọn quá tối.")
        }
        if (mean > MAX_BRIGHTNESS) {
            return Quality(0f, TraceErrorCode.IMAGE_TOO_DARK, "Vùng chọn quá sáng.")
        }
        val variance = brightness.sumOf { (it - mean) * (it - mean) } / brightness.size
        if (variance < MIN_VARIANCE) {
            return Quality(0f, TraceErrorCode.IMAGE_TOO_BLURRY, "Vùng chọn có quá ít chi tiết.")
        }
        val exposure = (1.0 - abs(mean - 127.5) / 127.5).coerceIn(0.0, 1.0)
        val detail = (variance / 4_000.0).coerceIn(0.0, 1.0)
        return Quality((exposure * 0.4 + detail * 0.6).toFloat(), null, "")
    }

    private fun extractEmbedding(image: Bitmap): FloatArray {
        val tensorValues = imageToEmbeddingTensor(image)
        val shape = longArrayOf(1, 3, EMBEDDING_SIZE.toLong(), EMBEDDING_SIZE.toLong())
        OnnxTensor.createTensor(runtime.environment, FloatBuffer.wrap(tensorValues), shape).use { tensor ->
            val inputName = runtime.embedding.inputNames.first()
            runtime.embedding.run(mapOf(inputName to tensor)).use { result ->
                return l2Normalize(flatten(result[0].value))
            }
        }
    }

    private fun imageToEmbeddingTensor(image: Bitmap): FloatArray {
        val resized = Bitmap.createScaledBitmap(image, EMBEDDING_SIZE, EMBEDDING_SIZE, true)
        val pixels = IntArray(EMBEDDING_SIZE * EMBEDDING_SIZE)
        resized.getPixels(pixels, 0, EMBEDDING_SIZE, 0, 0, EMBEDDING_SIZE, EMBEDDING_SIZE)
        val tensor = FloatArray(3 * pixels.size)
        val stride = pixels.size
        pixels.forEachIndexed { index, pixel ->
            val red = (pixel shr 16 and 0xFF) / 255f
            val green = (pixel shr 8 and 0xFF) / 255f
            val blue = (pixel and 0xFF) / 255f
            tensor[index] = (red - 0.485f) / 0.229f
            tensor[stride + index] = (green - 0.456f) / 0.224f
            tensor[2 * stride + index] = (blue - 0.406f) / 0.225f
        }
        return tensor
    }

    private fun detectObjects(image: Bitmap): List<BoundingBox> {
        val resized = Bitmap.createScaledBitmap(image, DETECTION_SIZE, DETECTION_SIZE, true)
        val pixels = IntArray(DETECTION_SIZE * DETECTION_SIZE)
        resized.getPixels(pixels, 0, DETECTION_SIZE, 0, 0, DETECTION_SIZE, DETECTION_SIZE)
        val bytes = ByteArray(pixels.size * 3)
        pixels.forEachIndexed { index, pixel ->
            bytes[index * 3] = (pixel shr 16 and 0xFF).toByte()
            bytes[index * 3 + 1] = (pixel shr 8 and 0xFF).toByte()
            bytes[index * 3 + 2] = (pixel and 0xFF).toByte()
        }
        val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
        buffer.put(bytes).rewind()
        val shape = longArrayOf(1, DETECTION_SIZE.toLong(), DETECTION_SIZE.toLong(), 3)
        OnnxTensor.createTensor(
            runtime.environment,
            buffer,
            shape,
            OnnxJavaType.UINT8,
        ).use { tensor ->
            runtime.detection.run(mapOf(runtime.detection.inputNames.first() to tensor)).use { result ->
                return parseDetections(result)
            }
        }
    }

    private fun parseDetections(result: OrtSession.Result): List<BoundingBox> {
        val boxesValue = findOutput(result, "detection_boxes", 0) ?: return emptyList()
        val scoresValue = findOutput(result, "detection_scores", 1) ?: return emptyList()
        val boxes = parseBoxes(boxesValue)
        val scores = parseScores(scoresValue)
        return (0 until minOf(boxes.size, scores.size, MAX_DETECTIONS))
            .filter { scores[it] >= DETECTION_THRESHOLD }
            .map { index ->
                BoundingBox(
                    ymin = boxes[index][0],
                    xmin = boxes[index][1],
                    ymax = boxes[index][2],
                    xmax = boxes[index][3],
                    confidence = scores[index],
                )
            }
            .filter { it.isValid }
    }

    private fun findOutput(result: OrtSession.Result, hint: String, fallback: Int): Any? {
        result.forEach { entry ->
            if (entry.key.contains(hint, ignoreCase = true)) return entry.value.value
        }
        return result.toList().getOrNull(fallback)?.value?.value
    }

    private fun parseBoxes(value: Any): Array<FloatArray> = when (value) {
        is Array<*> -> when (val first = value.firstOrNull()) {
            is Array<*> -> first.mapNotNull { it as? FloatArray }.toTypedArray()
            is FloatArray -> Array(first.size / 4) { index ->
                FloatArray(4) { coordinate -> first[index * 4 + coordinate] }
            }
            else -> emptyArray()
        }
        else -> emptyArray()
    }

    private fun parseScores(value: Any): FloatArray = when (value) {
        is FloatArray -> value
        is Array<*> -> value.firstOrNull() as? FloatArray ?: FloatArray(0)
        else -> FloatArray(0)
    }

    private fun cropWithPadding(image: Bitmap, box: BoundingBox): Bitmap {
        val padX = (box.xmax - box.xmin) * CROP_PADDING
        val padY = (box.ymax - box.ymin) * CROP_PADDING
        val left = ((box.xmin - padX) * image.width).toInt().coerceIn(0, image.width - 1)
        val top = ((box.ymin - padY) * image.height).toInt().coerceIn(0, image.height - 1)
        val right = ((box.xmax + padX) * image.width).toInt().coerceIn(left + 1, image.width)
        val bottom = ((box.ymax + padY) * image.height).toInt().coerceIn(top + 1, image.height)
        return Bitmap.createBitmap(image, left, top, right - left, bottom - top)
    }

    private fun match(
        candidate: FloatArray,
        references: List<ObjectReference>,
        threshold: Float,
        box: NormalizedRect,
    ): List<ObjectDetection> = references.mapNotNull { reference ->
        val score = reference.embeddings
            .asSequence()
            .filter {
                it.modelName == MODEL_NAME &&
                    it.modelVersion == MODEL_VERSION &&
                    it.values.size == candidate.size
            }
            .maxOfOrNull { CosineSimilarity.score(candidate, it.values) }
            ?: return@mapNotNull null
        ObjectDetection(
            objectId = reference.objectId.takeIf { score >= threshold },
            boundingBox = box.takeIf { score >= threshold },
            similarity = score,
            status = if (score >= threshold) MatchStatus.MATCHED else MatchStatus.UNKNOWN,
        )
    }

    private fun merge(values: List<ObjectDetection>, limit: Int): List<ObjectDetection> {
        val matched = values.asSequence()
            .filter { it.status == MatchStatus.MATCHED && it.objectId != null }
            .sortedByDescending { it.similarity }
            .distinctBy { it.objectId }
            .take(limit)
            .toList()
        if (matched.isNotEmpty()) return matched
        return values.maxByOrNull { it.similarity }?.let {
            listOf(it.copy(objectId = null, boundingBox = null, status = MatchStatus.UNKNOWN))
        }.orEmpty()
    }

    private fun flatten(value: Any): FloatArray {
        val result = ArrayList<Float>(EMBEDDING_DIMENSIONS)
        fun visit(current: Any?) {
            when (current) {
                is Float -> result += current
                is FloatArray -> current.forEach { result += it }
                is Array<*> -> current.forEach(::visit)
                else -> if (current != null) error("Unexpected ONNX output type")
            }
        }
        visit(value)
        return result.toFloatArray()
    }

    private fun l2Normalize(values: FloatArray): FloatArray {
        val norm = sqrt(values.sumOf { (it * it).toDouble() }).toFloat()
        require(norm > 0f && values.size == EMBEDDING_DIMENSIONS)
        return FloatArray(values.size) { values[it] / norm }
    }

    private fun modelFailure(cause: Exception) = TraceResult.Failure(
        TraceError(TraceErrorCode.INTERNAL_FAILURE, "Model nhận diện trên thiết bị gặp lỗi.", cause),
    )

    private fun failure(code: TraceErrorCode, message: String) =
        TraceResult.Failure(TraceError(code, message))

    private data class ModelRuntime(
        val environment: OrtEnvironment,
        val embedding: OrtSession,
        val detection: OrtSession,
    )

    private data class Quality(
        val score: Float,
        val reason: TraceErrorCode?,
        val message: String,
    )

    private data class BoundingBox(
        val ymin: Float,
        val xmin: Float,
        val ymax: Float,
        val xmax: Float,
        val confidence: Float,
    ) {
        val isValid: Boolean
            get() = xmin in 0f..1f && xmax in 0f..1f && ymin in 0f..1f && ymax in 0f..1f &&
                xmin < xmax && ymin < ymax

        fun toRect() = NormalizedRect(xmin, ymin, xmax, ymax)
    }

    private companion object {
        const val MODEL_NAME = "mobilenet-v3-small"
        const val MODEL_VERSION = "1"
        const val EMBEDDING_DIMENSIONS = 576
        const val EMBEDDING_SIZE = 224
        const val DETECTION_SIZE = 300
        const val DETECTION_THRESHOLD = 0.3f
        const val MAX_DETECTIONS = 20
        const val CROP_PADDING = 0.10f
        const val MIN_CROP_SIZE = 32
        const val MAX_DECODE_DIMENSION = 2_048
        const val QUALITY_SAMPLE_SIZE = 96
        const val MIN_BRIGHTNESS = 25.0
        const val MAX_BRIGHTNESS = 230.0
        const val MIN_VARIANCE = 100.0
        const val EMBEDDING_ASSET = "models/mobilenet_v3_small.onnx"
        const val DETECTION_ASSET = "models/ssd_mobilenet_v2.onnx"
    }
}
