package com.traceapp.feature.securevault

import com.traceapp.core.contracts.AccountSession
import com.traceapp.core.contracts.ObjectDraft
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.ObjectStore
import com.traceapp.core.contracts.SecureAssetStore
import com.traceapp.core.contracts.Sighting
import com.traceapp.core.contracts.SightingStore
import com.traceapp.core.contracts.SyncStatus
import com.traceapp.core.contracts.TraceError
import com.traceapp.core.contracts.TraceErrorCode
import com.traceapp.core.contracts.TraceResult
import com.traceapp.core.contracts.VisualEmbedding
import com.traceapp.core.database.LocalObjectEntity
import com.traceapp.core.database.LocalObjectReferenceEntity
import com.traceapp.core.database.LocalReferenceEmbeddingEntity
import com.traceapp.core.database.LocalSightingEntity
import com.traceapp.core.database.ObjectDao
import com.traceapp.core.database.SightingDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomObjectStore @Inject constructor(
    private val accountSession: AccountSession,
    private val objectDao: ObjectDao,
    private val sightingDao: SightingDao,
    private val assetStore: SecureAssetStore,
    private val vault: CryptoVault,
) : ObjectStore {
    override suspend fun create(draft: ObjectDraft): TraceResult<ObjectReference> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        val reference = draft.reference
        if (
            draft.objectId != reference.objectId || draft.tag.isBlank() ||
            reference.referenceId.isBlank() || reference.embeddings.isEmpty()
        ) {
            return@withContext invalid("Object reference is incomplete")
        }
        try {
            val encryptedTag = sealValue(
                draft.tag.trim().toByteArray(Charsets.UTF_8),
                valueContext(accountId, draft.objectId, FIELD_OBJECT_TAG),
            )
            val embeddings = reference.embeddings.mapIndexed { ordinal, embedding ->
                val encoded = FloatVectorCodec.encode(embedding.values)
                LocalReferenceEmbeddingEntity(
                    referenceId = reference.referenceId,
                    ordinal = ordinal,
                    encryptedValues = sealValue(
                        encoded,
                        embeddingContext(accountId, reference.referenceId, ordinal, embedding),
                    ),
                    dimensions = embedding.values.size,
                    modelName = embedding.modelName,
                    modelVersion = embedding.modelVersion,
                )
            }
            objectDao.insertGraph(
                objectEntity = LocalObjectEntity(
                    objectId = draft.objectId,
                    accountId = accountId,
                    encryptedTag = encryptedTag,
                    referenceRevision = 1,
                    createdAtEpochMillis = reference.createdAtEpochMillis,
                    updatedAtEpochMillis = reference.createdAtEpochMillis,
                ),
                referenceEntity = LocalObjectReferenceEntity(
                    referenceId = reference.referenceId,
                    objectId = reference.objectId,
                    imageAssetId = reference.imageAssetId,
                    roiLeft = reference.roi.left,
                    roiTop = reference.roi.top,
                    roiRight = reference.roi.right,
                    roiBottom = reference.roi.bottom,
                    qualityScore = reference.qualityScore,
                    createdAtEpochMillis = reference.createdAtEpochMillis,
                ),
                embeddings = embeddings,
            )
            TraceResult.Success(reference.copy(tag = draft.tag.trim()))
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not store encrypted object reference")
        }
    }

    override suspend fun get(objectId: String): TraceResult<ObjectReference> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        try {
            val objectEntity = objectDao.getObject(accountId, objectId)
                ?: return@withContext notFound("Object was not found")
            val reference = objectDao.getReferences(objectId).firstOrNull()
                ?: return@withContext TraceResult.Failure(
                    TraceError(TraceErrorCode.STORAGE_FAILURE, "Object reference is missing"),
                )
            TraceResult.Success(toReference(accountId, objectEntity, reference))
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not authenticate object data")
        }
    }

    override suspend fun getAllReferences(): TraceResult<List<ObjectReference>> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        try {
            val references = objectDao.getAllReferences(accountId).map { reference ->
                val objectEntity = objectDao.getObject(accountId, reference.objectId)
                    ?: throw IllegalStateException("Reference owner is missing")
                toReference(accountId, objectEntity, reference)
            }
            TraceResult.Success(references)
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not authenticate stored objects")
        }
    }

    override suspend fun delete(objectId: String): TraceResult<Unit> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        try {
            val objectEntity = objectDao.getObject(accountId, objectId)
                ?: return@withContext notFound("Object was not found")
            val referenceAssets = objectDao.getReferences(objectId).map { it.imageAssetId }
            val evidenceAssets = sightingDao.getTimeline(accountId, objectId, Int.MAX_VALUE)
                .mapNotNull { it.evidenceAssetId }
            val deleted = objectDao.deleteObject(accountId, objectEntity.objectId)
            if (deleted == 0) return@withContext notFound("Object was not found")
            (referenceAssets + evidenceAssets).distinct().forEach { assetStore.delete(it) }
            TraceResult.Success(Unit)
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not delete object")
        }
    }

    private suspend fun toReference(
        accountId: String,
        objectEntity: LocalObjectEntity,
        reference: LocalObjectReferenceEntity,
    ): ObjectReference {
        val tag = String(
            openValue(
                objectEntity.encryptedTag,
                valueContext(accountId, objectEntity.objectId, FIELD_OBJECT_TAG),
            ),
            Charsets.UTF_8,
        )
        val embeddings = objectDao.getEmbeddings(reference.referenceId).map { entity ->
            val descriptor = VisualEmbedding(FloatArray(0), entity.modelName, entity.modelVersion)
            val values = FloatVectorCodec.decode(
                openValue(
                    entity.encryptedValues,
                    embeddingContext(accountId, entity.referenceId, entity.ordinal, descriptor),
                ),
                entity.dimensions,
            )
            descriptor.copy(values = values)
        }
        return ObjectReference(
            referenceId = reference.referenceId,
            objectId = reference.objectId,
            tag = tag,
            imageAssetId = reference.imageAssetId,
            roi = com.traceapp.core.contracts.NormalizedRect(
                reference.roiLeft,
                reference.roiTop,
                reference.roiRight,
                reference.roiBottom,
            ),
            embeddings = embeddings,
            qualityScore = reference.qualityScore,
            createdAtEpochMillis = reference.createdAtEpochMillis,
        )
    }

    private fun embeddingContext(
        accountId: String,
        referenceId: String,
        ordinal: Int,
        embedding: VisualEmbedding,
    ) = valueContext(
        accountId,
        "$referenceId:$ordinal",
        "$FIELD_EMBEDDING:${embedding.modelName}:${embedding.modelVersion}",
    )

    private fun sealValue(value: ByteArray, context: VaultContext): ByteArray =
        VaultEnvelopeCodec.encode(vault.seal(value, context))

    private fun openValue(value: ByteArray, context: VaultContext): ByteArray =
        vault.open(VaultEnvelopeCodec.decode(value), context)

    private companion object {
        const val FIELD_OBJECT_TAG = "OBJECT_TAG"
        const val FIELD_EMBEDDING = "EMBEDDING"
    }
}

@Singleton
class RoomSightingStore @Inject constructor(
    private val accountSession: AccountSession,
    private val objectDao: ObjectDao,
    private val sightingDao: SightingDao,
    private val vault: CryptoVault,
) : SightingStore {
    override suspend fun insert(sighting: Sighting): TraceResult<Sighting> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        if (objectDao.getObject(accountId, sighting.objectId) == null) {
            return@withContext notFound("Object was not found")
        }
        try {
            sightingDao.insert(sighting.toEntity(accountId))
            TraceResult.Success(sighting)
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not store sighting")
        }
    }

    override suspend fun update(sighting: Sighting): TraceResult<Sighting> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        if (objectDao.getObject(accountId, sighting.objectId) == null) {
            return@withContext notFound("Object was not found")
        }
        try {
            sightingDao.update(sighting.toEntity(accountId))
            TraceResult.Success(sighting)
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not update sighting")
        }
    }

    override suspend fun getLatest(objectId: String): TraceResult<Sighting?> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        try {
            TraceResult.Success(sightingDao.getLatest(accountId, objectId)?.toDomain(accountId))
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not authenticate sighting")
        }
    }

    override suspend fun getTimeline(objectId: String, limit: Int): TraceResult<List<Sighting>> =
        withContext(Dispatchers.IO) {
            val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
            try {
                TraceResult.Success(
                    sightingDao.getTimeline(accountId, objectId, limit.coerceIn(1, 100))
                        .map { it.toDomain(accountId) },
                )
            } catch (failure: Exception) {
                failure.toTraceFailure("Could not authenticate timeline")
            }
        }

    override suspend fun getPending(limit: Int): TraceResult<List<Sighting>> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        try {
            TraceResult.Success(
                sightingDao.getPending(accountId, limit.coerceIn(1, 100)).map { it.toDomain(accountId) },
            )
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not authenticate pending sightings")
        }
    }

    override suspend fun getAllTimestamps(
        fromEpochMillis: Long,
        toEpochMillis: Long,
    ): TraceResult<List<Long>> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        try {
            TraceResult.Success(sightingDao.getAllTimestamps(accountId, fromEpochMillis, toEpochMillis))
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not read usage timestamps")
        }
    }

    override suspend fun getObjectTimestamps(
        objectId: String,
        fromEpochMillis: Long,
        toEpochMillis: Long,
    ): TraceResult<List<Long>> = withContext(Dispatchers.IO) {
        val accountId = accountSession.currentAccountId() ?: return@withContext unauthorized()
        try {
            TraceResult.Success(
                sightingDao.getObjectTimestamps(accountId, objectId, fromEpochMillis, toEpochMillis),
            )
        } catch (failure: Exception) {
            failure.toTraceFailure("Could not read usage timestamps")
        }
    }

    private fun Sighting.toEntity(accountId: String): LocalSightingEntity = LocalSightingEntity(
        sightingId = sightingId,
        objectId = objectId,
        detectedAtEpochMillis = detectedAtEpochMillis,
        encryptedLocation = location?.let {
            VaultEnvelopeCodec.encode(
                vault.seal(GeoFixCodec.encode(it), locationContext(accountId, sightingId)),
            )
        },
        confidence = confidence,
        evidenceAssetId = evidenceAssetId,
        syncStatus = syncStatus.name,
    )

    private fun LocalSightingEntity.toDomain(accountId: String): Sighting = Sighting(
        sightingId = sightingId,
        objectId = objectId,
        detectedAtEpochMillis = detectedAtEpochMillis,
        location = encryptedLocation?.let {
            GeoFixCodec.decode(
                vault.open(VaultEnvelopeCodec.decode(it), locationContext(accountId, sightingId)),
            )
        },
        confidence = confidence,
        evidenceAssetId = evidenceAssetId,
        syncStatus = runCatching { SyncStatus.valueOf(syncStatus) }.getOrDefault(SyncStatus.PENDING),
    )

    private fun locationContext(accountId: String, sightingId: String) =
        valueContext(accountId, sightingId, "SIGHTING_LOCATION")
}

private fun valueContext(accountId: String, recordId: String, field: String) =
    VaultContext(accountId = accountId, recordId = recordId, field = field)
