package com.trace.playground.memory

import com.trace.playground.contracts.MemoryEngine
import com.trace.playground.contracts.MemoryResult
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.Sighting
import com.trace.playground.contracts.TraceRepository
import com.trace.playground.contracts.LocationInput
import kotlin.math.abs
import kotlin.math.*
import kotlin.math.pow


private const val DEDUPLICATION_TIME_WINDOW = 120_000L
private const val DEDUPLICATION_DISTANCE_WINDOW = 30.0
private const val EARTH_RADIUS_M = 64_728_00L
// Credit: https://gist.github.com/jferrao/cb44d09da234698a7feee68ca895f491
/** Thành viên 3 triển khai retrieval, deduplication và timeline tại đây. */
class MemoryAlgorithm(
    private val repository: TraceRepository,
) : MemoryEngine {

    private fun haversineDistance(start: LocationInput, end: LocationInput): Double {
        val dLat = Math.toRadians(start.latitude - end.latitude);
        val dLon = Math.toRadians(start.longitude - end.longitude);
        val originLat = Math.toRadians(start.latitude);
        val destinationLat = Math.toRadians(end.latitude);

        val a = sin(dLat / 2.0).pow(2) +
                sin(dLon / 2.0).pow(2) *
                cos(originLat) *
                cos(destinationLat);
        val c = 2 * asin(sqrt(a));
        return EARTH_RADIUS_M * c + (start.accuracyMeters ?: 0).toDouble() + (end.accuracyMeters ?: 0.0).toDouble();
    }


    private fun isDuplicated(prev: Sighting, request: RecordSightingRequest): Boolean {


        var isCloseInTime = false;
        var isCloseInSpace = false
        if (abs(prev.detectedAtEpochMillis - request.detectedAtEpochMillis) < DEDUPLICATION_TIME_WINDOW) {
            println("Duplicated: Previous time is ${prev.detectedAtEpochMillis}, current time is ${request.detectedAtEpochMillis}")
            isCloseInTime = true
        }
        println("Is previous null? ${prev.location ?: "true"}")
        println("Is current null? ${request.location ?: "true"}")
        val prevLoc = prev.location;
        val requestLoc = request.location;

        if (prevLoc != null && requestLoc != null) {
            val distance = haversineDistance(prevLoc, requestLoc) <= DEDUPLICATION_DISTANCE_WINDOW
            println("Non-null locations. Distance is ${distance}")
            if (distance) {
                println("Close in space")
                isCloseInSpace = true
            }
        } else {
            println("Either locations are null")
            isCloseInSpace = true
        }

        return isCloseInSpace && isCloseInTime
    }

    override suspend fun record(request: RecordSightingRequest): Sighting {
        var normalizedRequest = RecordSightingRequest(
            objectId = request.objectId.lowercase(),
            detectedAtEpochMillis = request.detectedAtEpochMillis,
            confidence = request.confidence,
            location = request.location
        );
        val lastSightingHistory: Sighting =
            repository.timeline(request.objectId, 1).firstOrNull() ?: run {
                println("recording since no previous sighting")
                return repository.recordSighting(normalizedRequest)
            };
        if (isDuplicated(lastSightingHistory, request)) run {
            println("Duplicated, no writing")
            return lastSightingHistory
        }
        println("Writing, no duplication, having previous sighting")
        return repository.recordSighting(normalizedRequest)
    }

    override suspend fun find(query: String): List<MemoryResult> {
        require(query.isNotBlank()) { "query must not be blank" }
        var resultList = repository.findObjects(query.lowercase())
        println(resultList.map { it.objectId })
        resultList = resultList.sortedWith(compareByDescending<MemoryResult> { it.lastSeen?.detectedAtEpochMillis }
            .thenBy { it.tag })
        resultList.map { it -> println(it) }
        return resultList
    }

    override suspend fun timeline(
        objectId: String,
        limit: Int,
    ): List<Sighting> {
        var resultTimeline =
            repository.timeline(objectId, limit).sortedWith(compareByDescending<Sighting> { it.detectedAtEpochMillis })
        return resultTimeline
    }
}
