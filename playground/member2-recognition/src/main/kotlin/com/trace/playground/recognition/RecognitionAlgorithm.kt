package com.trace.playground.recognition

import com.trace.playground.contracts.Detection
import com.trace.playground.contracts.MatchStatus
import com.trace.playground.contracts.RecognitionEngine
import com.trace.playground.contracts.RecognitionRequest
import com.trace.playground.contracts.RecognitionResult
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

/** Thành viên 2 thay detection, embedding và matching trong class này. */
class RecognitionAlgorithm : RecognitionEngine {
    override suspend fun recognize(request: RecognitionRequest): RecognitionResult {
        require(request.minimumSimilarity in 0f..1f) { "minimumSimilarity must be between 0 and 1" }
        require(request.maximumResults in 1..50) { "maximumResults must be between 1 and 50" }
        var detections: List<Detection> = emptyList()
        val elapsed = measureTimeMillis {
            val image = ImageIO.read(ByteArrayInputStream(request.image.jpegBytes))
                ?: throw IllegalArgumentException("image must be a valid JPEG")
            val query = buildList(64) {
                repeat(8) { y ->
                    repeat(8) { x ->
                        val pixelX = ((x + 0.5) * image.width / 8).toInt().coerceIn(0, image.width - 1)
                        val pixelY = ((y + 0.5) * image.height / 8).toInt().coerceIn(0, image.height - 1)
                        val rgb = image.getRGB(pixelX, pixelY)
                        add(((rgb shr 16 and 0xFF) * 0.299f + (rgb shr 8 and 0xFF) * 0.587f +
                            (rgb and 0xFF) * 0.114f) / 255f)
                    }
                }
            }
            detections = request.references
                .filter { it.values.size == query.size }
                .map { reference ->
                    val similarity = cosine(query, reference.values)
                    Detection(
                        objectId = reference.objectId.takeIf { similarity >= request.minimumSimilarity },
                        tag = reference.tag.takeIf { similarity >= request.minimumSimilarity },
                        similarity = similarity,
                        status = if (similarity >= request.minimumSimilarity) {
                            MatchStatus.MATCHED
                        } else {
                            MatchStatus.UNKNOWN
                        },
                    )
                }
                .sortedByDescending(Detection::similarity)
                .take(request.maximumResults)
            if (detections.isEmpty()) {
                detections = listOf(Detection(similarity = 0f, status = MatchStatus.UNKNOWN))
            }
        }
        return RecognitionResult(
            detections = detections,
            processingTimeMillis = elapsed,
            modelVersion = "trace-prototype-grid:1",
            warnings = listOf("Prototype full-image matcher: replace with the member 2 solution"),
        )
    }

    internal fun cosine(left: List<Float>, right: List<Float>): Float {
        require(left.size == right.size && left.isNotEmpty()) { "vectors must have equal non-zero size" }
        var dot = 0.0
        var leftNorm = 0.0
        var rightNorm = 0.0
        left.indices.forEach { index ->
            dot += left[index] * right[index]
            leftNorm += left[index] * left[index]
            rightNorm += right[index] * right[index]
        }
        if (leftNorm == 0.0 || rightNorm == 0.0) return 0f
        return (dot / (sqrt(leftNorm) * sqrt(rightNorm))).toFloat().coerceIn(-1f, 1f)
    }
}
