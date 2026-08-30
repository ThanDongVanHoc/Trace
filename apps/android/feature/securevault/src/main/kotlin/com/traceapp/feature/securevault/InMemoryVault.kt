package com.traceapp.feature.securevault

import com.traceapp.core.contracts.ObjectDraft
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.ObjectStore
import com.traceapp.core.contracts.SecureAsset
import com.traceapp.core.contracts.SecureAssetStore
import com.traceapp.core.contracts.SecureAssetType
import com.traceapp.core.contracts.Sighting
import com.traceapp.core.contracts.SightingStore
import com.traceapp.core.contracts.SyncStatus
import com.traceapp.core.contracts.TraceError
import com.traceapp.core.contracts.TraceErrorCode
import com.traceapp.core.contracts.TraceResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Shared process-memory state for integration only; no value survives an app restart. */
@Singleton
class InMemoryVaultState @Inject constructor() {
    internal val mutex = Mutex()
    internal val objects = linkedMapOf<String, ObjectReference>()
    internal val sightings = linkedMapOf<String, Sighting>()
    internal val assets = linkedMapOf<String, Pair<SecureAsset, ByteArray>>()
}

/** Integration baseline. Member 4 replaces this adapter with the encrypted Room implementation. */
@Singleton
class InMemoryObjectStore @Inject constructor(
    private val state: InMemoryVaultState,
) : ObjectStore {
    override suspend fun create(draft: ObjectDraft): TraceResult<ObjectReference> = state.mutex.withLock {
        if (state.objects.containsKey(draft.objectId)) {
            return@withLock TraceResult.Failure(
                TraceError(TraceErrorCode.INVALID_INPUT, "Object ID already exists"),
            )
        }
        state.objects[draft.objectId] = draft.reference
        TraceResult.Success(draft.reference)
    }

    override suspend fun get(objectId: String): TraceResult<ObjectReference> = state.mutex.withLock {
        val reference = state.objects[objectId]
        if (reference == null) {
            TraceResult.Failure(TraceError(TraceErrorCode.NOT_FOUND, "Object not found"))
        } else {
            TraceResult.Success(reference)
        }
    }

    override suspend fun getAllReferences(): TraceResult<List<ObjectReference>> = state.mutex.withLock {
        TraceResult.Success(state.objects.values.sortedByDescending { it.createdAtEpochMillis })
    }

    override suspend fun delete(objectId: String): TraceResult<Unit> = state.mutex.withLock {
        val reference = state.objects.remove(objectId)
            ?: return@withLock TraceResult.Failure(
                TraceError(TraceErrorCode.NOT_FOUND, "Object not found"),
            )
        state.assets.remove(reference.imageAssetId)
        val removedEvidence = state.sightings.values
            .filter { it.objectId == objectId }
            .mapNotNull { it.evidenceAssetId }
        state.sightings.entries.removeAll { it.value.objectId == objectId }
        removedEvidence.forEach(state.assets::remove)
        TraceResult.Success(Unit)
    }
}

@Singleton
class InMemorySightingStore @Inject constructor(
    private val state: InMemoryVaultState,
) : SightingStore {
    override suspend fun insert(sighting: Sighting): TraceResult<Sighting> = state.mutex.withLock {
        if (state.sightings.containsKey(sighting.sightingId)) {
            return@withLock TraceResult.Failure(
                TraceError(TraceErrorCode.INVALID_INPUT, "Sighting ID already exists"),
            )
        }
        state.sightings[sighting.sightingId] = sighting
        TraceResult.Success(sighting)
    }

    override suspend fun update(sighting: Sighting): TraceResult<Sighting> = state.mutex.withLock {
        if (!state.sightings.containsKey(sighting.sightingId)) {
            return@withLock TraceResult.Failure(
                TraceError(TraceErrorCode.NOT_FOUND, "Sighting not found"),
            )
        }
        state.sightings[sighting.sightingId] = sighting
        TraceResult.Success(sighting)
    }

    override suspend fun getLatest(objectId: String): TraceResult<Sighting?> = state.mutex.withLock {
        TraceResult.Success(
            state.sightings.values
                .asSequence()
                .filter { it.objectId == objectId }
                .maxByOrNull { it.detectedAtEpochMillis },
        )
    }

    override suspend fun getTimeline(objectId: String, limit: Int): TraceResult<List<Sighting>> =
        state.mutex.withLock {
            TraceResult.Success(
                state.sightings.values
                    .asSequence()
                    .filter { it.objectId == objectId }
                    .sortedByDescending { it.detectedAtEpochMillis }
                    .take(limit.coerceIn(1, 100))
                    .toList(),
            )
        }

    override suspend fun getPending(limit: Int): TraceResult<List<Sighting>> = state.mutex.withLock {
        TraceResult.Success(
            state.sightings.values
                .asSequence()
                .filter { it.syncStatus == SyncStatus.PENDING }
                .sortedBy { it.detectedAtEpochMillis }
                .take(limit.coerceIn(1, 100))
                .toList(),
        )
    }
}

@Singleton
class InMemoryAssetStore @Inject constructor(
    private val state: InMemoryVaultState,
) : SecureAssetStore {
    override suspend fun write(
        type: SecureAssetType,
        plaintext: ByteArray,
        mimeType: String,
    ): TraceResult<SecureAsset> = state.mutex.withLock {
        val asset = SecureAsset(
            assetId = UUID.randomUUID().toString(),
            type = type,
            mimeType = mimeType,
            createdAtEpochMillis = System.currentTimeMillis(),
        )
        state.assets[asset.assetId] = asset to plaintext.copyOf()
        TraceResult.Success(asset)
    }

    override suspend fun read(assetId: String): TraceResult<ByteArray> = state.mutex.withLock {
        val bytes = state.assets[assetId]?.second
        if (bytes == null) {
            TraceResult.Failure(TraceError(TraceErrorCode.NOT_FOUND, "Asset not found"))
        } else {
            TraceResult.Success(bytes.copyOf())
        }
    }

    override suspend fun delete(assetId: String): TraceResult<Unit> = state.mutex.withLock {
        state.assets.remove(assetId)
        TraceResult.Success(Unit)
    }
}
