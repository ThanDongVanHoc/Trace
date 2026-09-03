package com.trace.playground.memory

import com.trace.playground.contracts.MemoryEngine
import com.trace.playground.contracts.MemoryResult
import com.trace.playground.contracts.NearbyUsageRequest
import com.trace.playground.contracts.NearbyUsageResult
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.Sighting
import com.trace.playground.contracts.TraceRepository
import com.trace.playground.contracts.LocationInput
import com.trace.playground.contracts.UsageMatch
import com.trace.playground.contracts.UsageRow
import com.trace.playground.contracts.UsageVector
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.*
import kotlin.math.pow


private const val TAU = 2.0 * PI
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

        val a = sin(dLat / 2.0).pow(2) + sin(dLon / 2.0).pow(2) * cos(originLat) * cos(destinationLat);
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
        val normalizedRequest = RecordSightingRequest(
            objectId = request.objectId.lowercase(),
            detectedAtEpochMillis = request.detectedAtEpochMillis,
            confidence = request.confidence,
            location = request.location
        );
        val lastSightingHistory: Sighting = repository.timeline(request.objectId, 1).firstOrNull() ?: run {
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
        resultList =
            resultList.sortedWith(compareByDescending<MemoryResult> { it.lastSeen?.detectedAtEpochMillis }.thenBy { it.tag })
        resultList.map { it -> println(it) }
        return resultList
    }

    override suspend fun timeline(
        objectId: String,
        limit: Int,
    ): List<Sighting> {
        val resultTimeline =
            repository.timeline(objectId, limit).sortedWith(compareByDescending<Sighting> { it.detectedAtEpochMillis })
        return resultTimeline
    }

     override suspend fun nearbyUsage(request: NearbyUsageRequest, objectFilter: List<String>): NearbyUsageResult {
        val k = request.k.coerceAtLeast(0)
        val target = encode(request.atEpochMillis ?: System.currentTimeMillis())
        val rows = repository.usageRows()
        if (k == 0 || rows.isEmpty()) return NearbyUsageResult(emptyList(), emptyList())

        val matchedIdRows = if (objectFilter.isEmpty()) rows else rows.filter{row -> row.objectId in objectFilter}
        val ranked = matchedIdRows
            .map { row -> cosineDistance(target, UsageVector(row.daytimeAngle, row.weekdayAngle)) to row }
            .filter{it -> it.first >= 0.8} // Magic number here. Tested with multiplier = 0.3 and threshold 0.8: 2h30 for the same day, 2h15 for another day, 1h45 for two days apart, 1h for three days apart.
            .sortedWith(compareByDescending<Pair<Double, UsageRow>> { it.first }.thenBy { it.second.tag })
            .take(k)
            .map { it ->
                UsageMatch(
                    objectId = it.second.objectId,
                    tag = it.second.tag?:"",
                    it.first,
                    it.second.detectedAtEpochMillis?:0,
                    it.second.confidenceScore.toDouble()
                )
            }.sortedWith(compareByDescending<UsageMatch> { it.vectorDistance }.thenByDescending{it.matchedEpochMillis}.thenByDescending { it.objectId })

        val dedupObject : List<String> = ranked.map{it -> it.objectId}.distinct().sortedBy{ it->it }
        println(ranked)
        println(dedupObject)
        return NearbyUsageResult(ranked, dedupObject)
    }



    private fun encode(epochMillis: Long): UsageVector {
        val millisOfDay = Math.floorMod(epochMillis, 86_400_000L)
        val daytimeAngle = TAU * millisOfDay / 86_400_000.0
        val local = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC)
        val weekdayIndex = local.dayOfWeek.value % 7
        val weekdayAngle = TAU * weekdayIndex / 7.0
        return UsageVector(daytimeAngle, weekdayAngle)
    }

    private fun cosineDistance(vector1: UsageVector, vector2: UsageVector): Double {
        val dotProduct =
            cos(vector1.daytimeAngle) * cos(vector2.daytimeAngle) + sin(vector1.daytimeAngle) * sin(vector2.daytimeAngle) +
                    UsageVector.WEEK_LENGTH_MULTIPLIER.pow(2) *
                    (cos(vector1.weekdayAngle) * cos(vector2.weekdayAngle) + sin(vector1.weekdayAngle) * sin(vector2.weekdayAngle))
        return dotProduct / (1 + UsageVector.WEEK_LENGTH_MULTIPLIER.pow(2))
    }

}
