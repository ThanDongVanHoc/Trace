package com.trace.playground.contracts

interface EnrollmentEngine {
    suspend fun enroll(request: EnrollmentRequest): EnrollmentResult
}

interface RecognitionEngine {
    suspend fun recognize(request: RecognitionRequest): RecognitionResult
}

interface MemoryEngine {
    suspend fun record(request: RecordSightingRequest): Sighting?

    suspend fun find(query: String): List<MemoryResult>

    suspend fun timeline(objectId: String, limit: Int = 50): List<Sighting>

    suspend fun nearbyUsage(request: NearbyUsageRequest, objectFilter: List<String> = listOf()): NearbyUsageResult
}

interface VaultEngine {
    fun seal(request: SealRequest): SealedPayload

    fun open(payload: SealedPayload): OpenResult
}

interface TraceRepository {
    fun initialize()

    fun saveEnrollment(result: EnrollmentResult, assetPath: String)

    fun references(): List<ReferenceVector>

    fun recordSighting(request: RecordSightingRequest): Sighting

    fun findObjects(query: String): List<MemoryResult>

    fun timeline(objectId: String, limit: Int): List<Sighting>

    fun usageRows(): List<UsageRow>
}
