package com.traceapp.core.contracts

data class EnrollRequest(
    val tag: String,
    val image: ImageInput,
    val roi: NormalizedRect,
)

data class EnrollResponse(
    val objectId: String,
    val referenceId: String,
    val qualityScore: Float,
    val embeddingCount: Int,
    val warnings: List<String>,
)

interface EnrollmentApi {
    suspend fun enroll(request: EnrollRequest): TraceResult<EnrollResponse>
}

interface VisualEncoder {
    /** Loads the on-device model before the first user operation. */
    suspend fun warmUp(): TraceResult<Unit> = TraceResult.Success(Unit)

    suspend fun encode(
        image: ImageInput,
        roi: NormalizedRect? = null,
    ): TraceResult<VisualEmbedding>
}

data class RecognizeRequest(
    val image: ImageInput,
    val references: List<ObjectReference>,
    val minimumSimilarity: Float = 0.75f,
    val maximumResults: Int = 5,
)

data class RecognizeResponse(
    val detections: List<ObjectDetection>,
    val processingTimeMillis: Long,
    val modelVersion: String,
)

interface RecognitionApi {
    suspend fun recognize(request: RecognizeRequest): TraceResult<RecognizeResponse>
}

data class RecordSightingRequest(
    val objectId: String,
    val detectedAtEpochMillis: Long,
    val confidence: Float,
    val boundingBox: NormalizedRect?,
    val location: GeoFix?,
    val evidenceImage: ImageInput?,
)

data class RecordSightingResponse(
    val sightingId: String,
    val created: Boolean,
    val deduplicatedWith: String?,
)

data class FindLastSeenResponse(
    val objectId: String,
    val tag: String,
    val lastSeen: Sighting?,
)

interface MemoryApi {
    suspend fun recordSighting(
        request: RecordSightingRequest,
    ): TraceResult<RecordSightingResponse>

    suspend fun findLastSeen(objectId: String): TraceResult<FindLastSeenResponse>

    suspend fun getTimeline(
        objectId: String,
        limit: Int = 50,
    ): TraceResult<List<Sighting>>
}
