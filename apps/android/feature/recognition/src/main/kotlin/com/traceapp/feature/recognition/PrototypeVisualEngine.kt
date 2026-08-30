package com.traceapp.feature.recognition

import com.traceapp.core.contracts.ImageInput
import com.traceapp.core.contracts.MatchStatus
import com.traceapp.core.contracts.NormalizedRect
import com.traceapp.core.contracts.ObjectDetection
import com.traceapp.core.contracts.RecognizeRequest
import com.traceapp.core.contracts.RecognizeResponse
import com.traceapp.core.contracts.RecognitionApi
import com.traceapp.core.contracts.TraceError
import com.traceapp.core.contracts.TraceErrorCode
import com.traceapp.core.contracts.TraceResult
import com.traceapp.core.contracts.VisualEmbedding
import com.traceapp.core.contracts.VisualEncoder
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Integration baseline. Member 2 replaces this deterministic fingerprint with a LiteRT model. */
@Singleton
class PrototypeVisualEngine @Inject constructor() : VisualEncoder, RecognitionApi {
    override suspend fun encode(
        image: ImageInput,
        roi: NormalizedRect?,
    ): TraceResult<VisualEmbedding> = withContext(Dispatchers.Default) {
        if (image.jpegBytes.isEmpty() || image.width <= 0 || image.height <= 0) {
            return@withContext TraceResult.Failure(
                TraceError(TraceErrorCode.INVALID_INPUT, "Image is empty"),
            )
        }
        TraceResult.Success(
            VisualEmbedding(
                values = fingerprint(image.jpegBytes),
                modelName = MODEL_NAME,
                modelVersion = MODEL_VERSION,
            ),
        )
    }

    override suspend fun recognize(request: RecognizeRequest): TraceResult<RecognizeResponse> =
        withContext(Dispatchers.Default) {
            if (request.minimumSimilarity !in 0f..1f || request.maximumResults !in 1..20) {
                return@withContext TraceResult.Failure(
                    TraceError(TraceErrorCode.INVALID_INPUT, "Invalid recognition limits"),
                )
            }
            val startedAt = System.nanoTime()
            val candidate = when (val result = encode(request.image)) {
                is TraceResult.Success -> result.value
                is TraceResult.Failure -> return@withContext result
            }
            val ranked = request.references.mapNotNull { reference ->
                reference.embeddings
                    .filter { it.modelName == MODEL_NAME && it.modelVersion == MODEL_VERSION }
                    .maxOfOrNull { cosineSimilarity(candidate.values, it.values) }
                    ?.let { reference to it }
            }.sortedByDescending { it.second }

            val best = ranked.firstOrNull()
            val detection = if (best != null && best.second >= request.minimumSimilarity) {
                ObjectDetection(
                    objectId = best.first.objectId,
                    boundingBox = NormalizedRect.FullImage,
                    similarity = best.second,
                    status = MatchStatus.MATCHED,
                )
            } else {
                ObjectDetection(
                    objectId = null,
                    boundingBox = null,
                    similarity = best?.second ?: 0f,
                    status = MatchStatus.UNKNOWN,
                )
            }
            TraceResult.Success(
                RecognizeResponse(
                    detections = listOf(detection).take(request.maximumResults),
                    processingTimeMillis = (System.nanoTime() - startedAt) / 1_000_000,
                    modelVersion = MODEL_VERSION,
                ),
            )
        }

    private fun fingerprint(bytes: ByteArray): FloatArray {
        val values = FloatArray(64)
        var seed = bytes
        var offset = 0
        while (offset < values.size) {
            seed = MessageDigest.getInstance("SHA-256").digest(seed)
            seed.forEach { byte ->
                if (offset < values.size) values[offset++] = byte.toInt() / 128f
            }
        }
        return l2Normalize(values)
    }

    companion object {
        const val MODEL_NAME = "prototype-image-fingerprint"
        const val MODEL_VERSION = "1"

        fun cosineSimilarity(left: FloatArray, right: FloatArray): Float {
            if (left.size != right.size || left.isEmpty()) return 0f
            var dot = 0.0
            var leftNorm = 0.0
            var rightNorm = 0.0
            for (index in left.indices) {
                dot += left[index] * right[index]
                leftNorm += left[index] * left[index]
                rightNorm += right[index] * right[index]
            }
            if (leftNorm == 0.0 || rightNorm == 0.0) return 0f
            return (dot / kotlin.math.sqrt(leftNorm * rightNorm)).toFloat().coerceIn(-1f, 1f)
        }

        private fun l2Normalize(values: FloatArray): FloatArray {
            val norm = kotlin.math.sqrt(values.sumOf { (it * it).toDouble() }).toFloat()
            return if (norm == 0f) values else FloatArray(values.size) { values[it] / norm }
        }
    }
}
