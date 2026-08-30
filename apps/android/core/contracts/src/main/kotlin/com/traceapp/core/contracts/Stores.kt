package com.traceapp.core.contracts

data class ObjectDraft(
    val objectId: String,
    val tag: String,
    val reference: ObjectReference,
)

interface ObjectStore {
    suspend fun create(draft: ObjectDraft): TraceResult<ObjectReference>
    suspend fun get(objectId: String): TraceResult<ObjectReference>
    suspend fun getAllReferences(): TraceResult<List<ObjectReference>>
    suspend fun delete(objectId: String): TraceResult<Unit>
}

interface SightingStore {
    suspend fun insert(sighting: Sighting): TraceResult<Sighting>
    suspend fun update(sighting: Sighting): TraceResult<Sighting>
    suspend fun getLatest(objectId: String): TraceResult<Sighting?>
    suspend fun getTimeline(objectId: String, limit: Int = 50): TraceResult<List<Sighting>>
    suspend fun getPending(limit: Int = 100): TraceResult<List<Sighting>>
}

enum class SecureAssetType { REFERENCE_IMAGE, SIGHTING_EVIDENCE }

data class SecureAsset(
    val assetId: String,
    val type: SecureAssetType,
    val mimeType: String,
    val createdAtEpochMillis: Long,
)

interface SecureAssetStore {
    suspend fun write(
        type: SecureAssetType,
        plaintext: ByteArray,
        mimeType: String,
    ): TraceResult<SecureAsset>

    suspend fun read(assetId: String): TraceResult<ByteArray>
    suspend fun delete(assetId: String): TraceResult<Unit>
}
