package com.traceapp.core.contracts

data class ImageInput(
    val jpegBytes: ByteArray,
    val width: Int,
    val height: Int,
    val rotationDegrees: Int,
    val capturedAtEpochMillis: Long,
)

data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val isValid: Boolean
        get() = left in 0f..1f &&
            top in 0f..1f &&
            right in 0f..1f &&
            bottom in 0f..1f &&
            left < right &&
            top < bottom

    val area: Float get() = (right - left) * (bottom - top)

    companion object {
        val FullImage = NormalizedRect(0f, 0f, 1f, 1f)
    }
}

data class VisualEmbedding(
    val values: FloatArray,
    val modelName: String,
    val modelVersion: String,
    val qualityScore: Float = 1f,
)

data class GeoFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val capturedAtEpochMillis: Long,
)

data class ObjectReference(
    val referenceId: String,
    val objectId: String,
    val tag: String,
    val imageAssetId: String,
    val roi: NormalizedRect,
    val embeddings: List<VisualEmbedding>,
    val qualityScore: Float,
    val createdAtEpochMillis: Long,
)

enum class MatchStatus { MATCHED, UNKNOWN }

data class ObjectDetection(
    val objectId: String?,
    val boundingBox: NormalizedRect?,
    val similarity: Float,
    val status: MatchStatus,
)

enum class SyncStatus { PENDING, SYNCED, FAILED }

data class Sighting(
    val sightingId: String,
    val objectId: String,
    val detectedAtEpochMillis: Long,
    val location: GeoFix?,
    val confidence: Float,
    val evidenceAssetId: String?,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
)

/** Lightweight (objectId, detection time) pair used by usage-pattern analysis (no decryption). */
data class SightingTime(
    val objectId: String,
    val detectedAtEpochMillis: Long,
)
