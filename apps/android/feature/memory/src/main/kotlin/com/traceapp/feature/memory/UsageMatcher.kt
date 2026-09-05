package com.traceapp.feature.memory

import com.traceapp.core.contracts.SightingTime
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs

/**
 * Port of the prototype "nearby usage" matcher (`playground/member3-memory/MemoryAlgorithm`).
 *
 * Each detection is encoded as a periodic (daytime, weekday) angle pair computed in UTC; the
 * item whose history is closest to the query moment ranks first. Only the best row per item is
 * considered, so the returned item is the single "most matched at that time".
 */
object UsageMatcher {

    private const val TAU = 2.0 * PI
    private const val WEEK_LENGTH_MULTIPLIER = 0.3
    private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

    data class Vector(val daytimeAngle: Double, val weekdayAngle: Double)

    data class Match(
        val objectId: String,
        val tag: String?,
        val similarity: Double,
        val matchedEpochMillis: Long,
    )

    fun encode(epochMillis: Long): Vector {
        val millisOfDay = Math.floorMod(epochMillis, DAY_MILLIS)
        val daytimeAngle = TAU * millisOfDay / DAY_MILLIS
        val cal = Calendar.getInstance(UTC).apply { timeInMillis = epochMillis }
        val weekdayIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1) % 7
        val weekdayAngle = TAU * weekdayIndex / 7.0
        return Vector(daytimeAngle, weekdayAngle)
    }

    /**
     * Finds the single most-likely item at [nowEpochMillis] across [rows], using
     * [tagByObjectId] for display tags. Returns null when there is no usage history.
     */
    fun topMatch(
        nowEpochMillis: Long,
        rows: List<SightingTime>,
        tagByObjectId: Map<String, String>,
    ): Match? {
        if (rows.isEmpty()) return null
        val target = encode(nowEpochMillis)
        val bestRow = rows
            .map { row ->
                Scored(row.objectId, row.detectedAtEpochMillis, periodicSimilarity(target, encode(row.detectedAtEpochMillis)))
            }
            .sortedWith(scoredComparator) // best first, so distinctBy keeps each object's best row
            .distinctBy { it.objectId }
            .firstOrNull()
            ?: return null
        return Match(
            objectId = bestRow.objectId,
            tag = tagByObjectId[bestRow.objectId],
            similarity = bestRow.similarity,
            matchedEpochMillis = bestRow.matchedEpochMillis,
        )
    }

    private fun periodicSimilarity(first: Vector, second: Vector): Double {
        val daytimeDistance = circularDistance(first.daytimeAngle, second.daytimeAngle) / PI
        val weekdayDistance = circularDistance(first.weekdayAngle, second.weekdayAngle) / PI
        return 1.0 - (daytimeDistance + WEEK_LENGTH_MULTIPLIER * weekdayDistance) / (1.0 + WEEK_LENGTH_MULTIPLIER)
    }

    private fun circularDistance(first: Double, second: Double): Double {
        val raw = abs(first - second) % TAU
        return minOf(raw, TAU - raw)
    }

    private data class Scored(
        val objectId: String,
        val matchedEpochMillis: Long,
        val similarity: Double,
    )

    /** Highest similarity first; ties broken by recency then id for stable results. */
    private val scoredComparator = compareByDescending<Scored> { it.similarity }
        .thenByDescending { it.matchedEpochMillis }
        .thenBy { it.objectId }

    private const val DAY_MILLIS = 86_400_000L
}