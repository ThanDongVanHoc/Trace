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
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


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
            isCloseInTime = true
        }
        val prevLoc = prev.location;
        val requestLoc = request.location;

        if (prevLoc != null && requestLoc != null) {
            val distance = haversineDistance(prevLoc, requestLoc) <= DEDUPLICATION_DISTANCE_WINDOW
            if (distance) {
                isCloseInSpace = true
            }
        } else {
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
            return repository.recordSighting(normalizedRequest)
        };
        if (isDuplicated(lastSightingHistory, request)) run {
            return lastSightingHistory
        }
        return repository.recordSighting(normalizedRequest)
    }

    override suspend fun find(query: String): List<MemoryResult> {
        require(query.isNotBlank()) { "query must not be blank" }
        var resultList = repository.findObjects(query.lowercase())
        resultList =
            resultList.sortedWith(compareByDescending<MemoryResult> { it.lastSeen?.detectedAtEpochMillis }.thenBy { it.tag })
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
            .map { row ->
                val rowVector = UsageVector(row.daytimeAngle, row.weekdayAngle)
                ScoredUsage(
                    similarity = periodicSimilarity(target, rowVector),
                    forwardTieBreak = forwardDistance(target, rowVector),
                    row = row,
                )
            }
            .sortedWith(::compareScoredUsage)
            .distinctBy { it.row.objectId }
            .take(k)
            .map { scored ->
                UsageMatch(
                    objectId = scored.row.objectId,
                    tag = scored.row.tag ?: "",
                    vectorDistance = scored.similarity,
                    matchedEpochMillis = scored.row.detectedAtEpochMillis ?: 0,
                    confidence = scored.row.confidenceScore.toDouble(),
                )
            }

        return NearbyUsageResult(ranked, ranked.map(UsageMatch::objectId))
    }



    private fun encode(epochMillis: Long): UsageVector {
        val millisOfDay = Math.floorMod(epochMillis, 86_400_000L)
        val daytimeAngle = TAU * millisOfDay / 86_400_000.0
        val local = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC)
        val weekdayIndex = local.dayOfWeek.value % 7
        val weekdayAngle = TAU * weekdayIndex / 7.0
        return UsageVector(daytimeAngle, weekdayAngle)
    }

    private fun periodicSimilarity(vector1: UsageVector, vector2: UsageVector): Double {
        val daytimeDistance = circularDistance(vector1.daytimeAngle, vector2.daytimeAngle) / PI
        val weekdayDistance = circularDistance(vector1.weekdayAngle, vector2.weekdayAngle) / PI
        val weekWeight = UsageVector.WEEK_LENGTH_MULTIPLIER
        return 1.0 - (daytimeDistance + weekWeight * weekdayDistance) / (1.0 + weekWeight)
    }

    private fun circularDistance(first: Double, second: Double): Double {
        val raw = abs(first - second) % TAU
        return minOf(raw, TAU - raw)
    }

    private fun forwardDistance(target: UsageVector, candidate: UsageVector): Double {
        val daytime = forwardAngle(target.daytimeAngle, candidate.daytimeAngle)
        val weekday = forwardAngle(target.weekdayAngle, candidate.weekdayAngle)
        val weekWeight = UsageVector.WEEK_LENGTH_MULTIPLIER.pow(2)
        return (daytime + weekWeight * weekday) / (1 + weekWeight)
    }

    private fun forwardAngle(from: Double, to: Double): Double =
        ((to - from) % TAU + TAU) % TAU

    private fun compareScoredUsage(first: ScoredUsage, second: ScoredUsage): Int {
        val similarityDelta = second.similarity - first.similarity
        if (abs(similarityDelta) > SCORE_EPSILON) return similarityDelta.compareTo(0.0)
        val forwardDelta = first.forwardTieBreak - second.forwardTieBreak
        if (abs(forwardDelta) > SCORE_EPSILON) return forwardDelta.compareTo(0.0)
        val epoch = (second.row.detectedAtEpochMillis ?: Long.MIN_VALUE)
            .compareTo(first.row.detectedAtEpochMillis ?: Long.MIN_VALUE)
        if (epoch != 0) return epoch
        return second.row.objectId.compareTo(first.row.objectId)
    }

    private data class ScoredUsage(
        val similarity: Double,
        val forwardTieBreak: Double,
        val row: UsageRow,
    )

    private companion object {
        const val SCORE_EPSILON = 1e-12
    }

}
