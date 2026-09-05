package com.traceapp.feature.memory

import com.traceapp.core.contracts.FindLastSeenResponse
import com.traceapp.core.contracts.GeoFix
import com.traceapp.core.contracts.MemoryApi
import com.traceapp.core.contracts.MostMatchedItem
import com.traceapp.core.contracts.ObjectStore
import com.traceapp.core.contracts.RecordSightingRequest
import com.traceapp.core.contracts.RecordSightingResponse
import com.traceapp.core.contracts.SecureAssetStore
import com.traceapp.core.contracts.SecureAssetType
import com.traceapp.core.contracts.Sighting
import com.traceapp.core.contracts.SightingStore
import com.traceapp.core.contracts.TraceError
import com.traceapp.core.contracts.TraceErrorCode
import com.traceapp.core.contracts.TraceResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Singleton
class MemoryService @Inject constructor(
    private val objectStore: ObjectStore,
    private val sightingStore: SightingStore,
    private val assetStore: SecureAssetStore,
) : MemoryApi {
    override suspend fun recordSighting(
        request: RecordSightingRequest,
    ): TraceResult<RecordSightingResponse> {
        if (request.confidence !in 0f..1f) {
            return TraceResult.Failure(
                TraceError(TraceErrorCode.INVALID_INPUT, "Confidence must be between 0 and 1"),
            )
        }
        if (objectStore.get(request.objectId) is TraceResult.Failure) {
            return TraceResult.Failure(
                TraceError(TraceErrorCode.NOT_FOUND, "Object not found"),
            )
        }
        val latest = when (val result = sightingStore.getLatest(request.objectId)) {
            is TraceResult.Success -> result.value
            is TraceResult.Failure -> return result
        }
        if (latest != null && shouldDeduplicate(latest, request)) {
            val updated = latest.copy(
                detectedAtEpochMillis = request.detectedAtEpochMillis,
                location = request.location ?: latest.location,
                confidence = maxOf(latest.confidence, request.confidence),
            )
            return when (val result = sightingStore.update(updated)) {
                is TraceResult.Success -> TraceResult.Success(
                    RecordSightingResponse(latest.sightingId, false, latest.sightingId),
                )
                is TraceResult.Failure -> result
            }
        }

        val sightingId = UUID.randomUUID().toString()
        val evidenceAssetId = request.evidenceImage?.let { image ->
            when (
                val result = assetStore.write(
                    ownerRecordId = sightingId,
                    type = SecureAssetType.SIGHTING_EVIDENCE,
                    plaintext = image.jpegBytes,
                    mimeType = "image/jpeg",
                )
            ) {
                is TraceResult.Success -> result.value.assetId
                is TraceResult.Failure -> return result
            }
        }
        val sighting = Sighting(
            sightingId = sightingId,
            objectId = request.objectId,
            detectedAtEpochMillis = request.detectedAtEpochMillis,
            location = request.location,
            confidence = request.confidence,
            evidenceAssetId = evidenceAssetId,
        )
        return when (val result = sightingStore.insert(sighting)) {
            is TraceResult.Success -> TraceResult.Success(
                RecordSightingResponse(sighting.sightingId, true, null),
            )
            is TraceResult.Failure -> {
                evidenceAssetId?.let { assetStore.delete(it) }
                result
            }
        }
    }

    override suspend fun findLastSeen(objectId: String): TraceResult<FindLastSeenResponse> {
        val reference = when (val result = objectStore.get(objectId)) {
            is TraceResult.Success -> result.value
            is TraceResult.Failure -> return result
        }
        return when (val result = sightingStore.getLatest(objectId)) {
            is TraceResult.Success -> TraceResult.Success(
                FindLastSeenResponse(objectId, reference.tag, result.value),
            )
            is TraceResult.Failure -> result
        }
    }

    override suspend fun getTimeline(objectId: String, limit: Int): TraceResult<List<Sighting>> {
        if (limit !in 1..100) {
            return TraceResult.Failure(
                TraceError(TraceErrorCode.INVALID_INPUT, "Limit must be between 1 and 100"),
            )
        }
        return sightingStore.getTimeline(objectId, limit)
    }

    override suspend fun mostMatchedItem(atEpochMillis: Long): TraceResult<MostMatchedItem?> {
        val tagByObjectId = when (val result = objectStore.getAllReferences()) {
            is TraceResult.Success -> result.value.associate { it.objectId to it.tag }
            is TraceResult.Failure -> return result
        }
        val rows = when (val result = sightingStore.getAllSightingTimes()) {
            is TraceResult.Success -> result.value
            is TraceResult.Failure -> return result
        }
        return TraceResult.Success(
            UsageMatcher.topMatch(atEpochMillis, rows, tagByObjectId)?.let { match ->
                match.tag?.let { tag ->
                    MostMatchedItem(
                        objectId = match.objectId,
                        tag = tag,
                        similarity = match.similarity,
                        matchedEpochMillis = match.matchedEpochMillis,
                    )
                }
            },
        )
    }

    private fun shouldDeduplicate(latest: Sighting, request: RecordSightingRequest): Boolean {
        val timeDistance = request.detectedAtEpochMillis - latest.detectedAtEpochMillis
        if (timeDistance !in 0..DEDUPLICATION_WINDOW_MILLIS) return false
        val previousLocation = latest.location
        val currentLocation = request.location
        return if (previousLocation == null || currentLocation == null) {
            true
        } else {
            distanceMeters(previousLocation, currentLocation) < DEDUPLICATION_DISTANCE_METERS
        }
    }

    private fun distanceMeters(first: GeoFix, second: GeoFix): Double {
        val latitudeDelta = Math.toRadians(second.latitude - first.latitude)
        val longitudeDelta = Math.toRadians(second.longitude - first.longitude)
        val a = sin(latitudeDelta / 2) * sin(latitudeDelta / 2) +
            cos(Math.toRadians(first.latitude)) * cos(Math.toRadians(second.latitude)) *
            sin(longitudeDelta / 2) * sin(longitudeDelta / 2)
        return EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    private companion object {
        const val DEDUPLICATION_WINDOW_MILLIS = 120_000L
        const val DEDUPLICATION_DISTANCE_METERS = 30.0
        const val EARTH_RADIUS_METERS = 6_371_000.0
    }
}
