package com.trace.playground.contracts

import kotlinx.serialization.Serializable

@Serializable
data class Roi(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val isValid: Boolean
        get() = left in 0f..1f && top in 0f..1f && right in 0f..1f &&
            bottom in 0f..1f && left < right && top < bottom
}

data class ImageInput(
    val jpegBytes: ByteArray,
    val rotationDegrees: Int = 0,
)

@Serializable
data class ReferenceVector(
    val referenceId: String,
    val objectId: String,
    val tag: String,
    val values: List<Float>,
    val modelName: String,
    val modelVersion: String,
)

@Serializable
data class EnrollmentResult(
    val objectId: String,
    val referenceId: String,
    val tag: String,
    val qualityScore: Float,
    val embedding: ReferenceVector,
    val warnings: List<String> = emptyList(),
)

data class EnrollmentRequest(
    val tag: String,
    val image: ImageInput,
    val roi: Roi,
)

@Serializable
enum class MatchStatus { MATCHED, UNKNOWN }

@Serializable
data class Detection(
    val objectId: String? = null,
    val tag: String? = null,
    val similarity: Float,
    val status: MatchStatus,
    val boundingBox: Roi? = null,
)

data class RecognitionRequest(
    val image: ImageInput,
    val references: List<ReferenceVector>,
    val minimumSimilarity: Float = 0.75f,
    val maximumResults: Int = 5,
)

@Serializable
data class RecognitionResult(
    val detections: List<Detection>,
    val processingTimeMillis: Long,
    val modelVersion: String,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class LocationInput(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
)

@Serializable
data class RecordSightingRequest(
    val objectId: String,
    val detectedAtEpochMillis: Long,
    val confidence: Float,
    val location: LocationInput? = null,
)

@Serializable
data class Sighting(
    val sightingId: String,
    val objectId: String,
    val tag: String,
    val detectedAtEpochMillis: Long,
    val confidence: Float,
    val location: LocationInput? = null,
)

@Serializable
data class FindRequest(val query: String)

@Serializable
data class MemoryResult(
    val objectId: String,
    val tag: String,
    val lastSeen: Sighting? = null,
)

@Serializable
data class SealRequest(
    val plainText: String,
    val aad: String? = null,
)

@Serializable
data class SealedPayload(
    val algorithm: String,
    val keyId: String,
    val nonceBase64: String,
    val cipherTextBase64: String,
    val aad: String? = null,
)

@Serializable
data class OpenResult(val plainText: String)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
)
