package com.trace.playground.memory

import com.trace.playground.contracts.EnrollmentResult
import com.trace.playground.contracts.NearbyUsageRequest
import com.trace.playground.contracts.NearbyUsageResult
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.ReferenceVector
import com.trace.playground.contracts.TraceRepository
import com.trace.playground.storage.SqliteTraceRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.io.path.createTempDirectory
import kotlin.math.PI
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val TAU = 2.0 * PI

class UsageTimeNearbySpecTest {

    // ----- helpers ---------------------------------------------------------

    private fun repoWith(vararg tags: String): Pair<MemoryAlgorithm, SqliteTraceRepository> {
        val repository = SqliteTraceRepository(createTempDirectory().resolve("trace.db"))
        repository.initialize()
        tags.forEachIndexed { index, tag ->
            val id = "object-$index"
            repository.saveEnrollment(
                EnrollmentResult(
                    id,
                    "ref-$index",
                    tag,
                    1f,
                    ReferenceVector("ref-$index", id, tag, listOf(1f), "test", "1"),
                ),
                "blobs/ref-$index.jpg",
            )
        }
        return MemoryAlgorithm(repository) to repository
    }

    /** Enrolls n objects with blank tags, ids object-0 .. object-(n-1). */
    private fun repoWithObjects(n: Int): Pair<MemoryAlgorithm, SqliteTraceRepository> =
        repoWith(*Array(n) { "" })

    /** Base Monday of the reference week (2026-09-07 00:00 UTC). */
    private val baseMondayEpochMillis: Long =
        LocalDateTime.of(2026, 9, 7, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()

    /** Epoch millis for a moment in the reference week; day 0 == Monday, day 6 == Sunday. */
    private fun moment(daysFromMonday: Int, hour: Int, minute: Int): Long =
        baseMondayEpochMillis + daysFromMonday * 86_400_000L + (hour * 60L + minute) * 60_000L

    private suspend fun MemoryAlgorithm.seen(objectId: String, atEpochMillis: Long) {
        record(
            RecordSightingRequest(
                objectId = objectId,
                detectedAtEpochMillis = atEpochMillis,
                confidence = 0.9f,
                location = null,
            ),
        )
    }

    /** Verifies ranked order and per-match time against expected (objectId, epochMillis) pairs. */
    private fun assertNearby(result: NearbyUsageResult, vararg expected: Pair<String, Long>) {
        assertEquals(expected.size, result.ranked.size, "ranked size")
        assertEquals(expected.size, result.matchedObjects.size, "matchedObjects size")
        assertEquals(expected.map { it.first }, result.ranked.map { it.objectId }, "ranked order")
        assertEquals(expected.map { it.first }.toSet(), result.matchedObjects.toSet(), "matched set")
        expected.forEachIndexed { index, (id, epoch) ->
            val match = result.ranked[index]
            assertEquals(id, match.objectId)
            assertEquals(epoch, match.matchedEpochMillis, "matched epoch for $id")
        }
    }

    // ----- normal cases ----------------------------------------------------

    // Top-k nearest when only the day-of-week axis varies (identical clock time).
    @Test
    fun `ranks nearest by day of week`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            // Every object appears weekly at 09:00 on its own weekday; query Monday 09:00.
            // Use only a forward arc Mon..Thu from the query so the ordering is unambiguous.
            val monday = moment(0, 9, 0)
            val tuesday = moment(1, 9, 0)
            val wednesday = moment(2, 9, 0)
            val thursday = moment(3, 9, 0)
            mem.seen("object-0", monday)
            mem.seen("object-1", tuesday)
            mem.seen("object-2", wednesday)
            mem.seen("object-3", thursday)
// Far-away padding rows (≥10 data entries), never close enough to disturb the top-k.
            mem.seen("object-4", moment(5, 9, 0))

            mem.seen("object-5", moment(6, 9, 0))
            mem.seen("object-6", moment(4, 23, 0))
            mem.seen("object-7", moment(4, 8, 0))
            mem.seen("object-8", moment(6, 14, 30))
            mem.seen("object-9", moment(5, 20, 45))

            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = monday, k = 5))
            assertNearby(
                result,
                "object-0" to monday,
                "object-1" to tuesday,
                "object-5" to moment(6, 9, 0),
                "object-2" to wednesday,
                "object-3" to thursday
            )
        }

    // Top-k nearest when only the time-of-day axis varies (identical weekday).
    @Test
    fun `ranks nearest by time of day`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            // All on Monday, clock times advance forward from the query at 09:00.
            val at09 = moment(0, 9, 0)
            val at10 = moment(0, 10, 0)
            val at11 = moment(0, 11, 0)
            val at12 = moment(0, 12, 0)
            mem.seen("object-0", at09)
            mem.seen("object-1", at10)
            mem.seen("object-2", at11)
            mem.seen("object-3", at12)
            // Padding: objects seen far away on other weekdays and clock times.
            mem.seen("object-4", moment(2, 7, 0))
            mem.seen("object-5", moment(3, 6, 0))
            mem.seen("object-6", moment(4, 22, 0))
            mem.seen("object-7", moment(5, 21, 0))
            mem.seen("object-8", moment(6, 5, 0))
            mem.seen("object-9", moment(1, 23, 59))

            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = at09, k = 4))
            assertNearby(
                result,
                "object-0" to at09,
                "object-1" to at10,
                "object-2" to at11,
                "object-3" to at12,
            )
        }

    // A weekly-periodic match from an older week beats a raw-timestamp-nearer one.
    @Test
    fun `periodic weekday and time beat raw recency`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            val query = moment(0, 9, 0) // Monday 09:00
            // object-0 seen the PREVIOUS week on Monday at 09:05 -> same weekday/time (7 days back).
            val olderSameSlot = moment(0, 9, 5) - 7 * 86_400_000L
            // object-1 seen 'yesterday' (Sunday) at 17:00 -> raw-closer to the query epoch.
            val recentFarSlot = moment(6, 17, 0)
            mem.seen("object-0", olderSameSlot)
            mem.seen("object-1", recentFarSlot)
            for (i in 2..9) mem.seen("object-$i", moment(3, 2, 0)) // far padding

            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = query, k = 2))
            assertNearby(
                result,
                "object-0" to olderSameSlot, // matched to the historical same-periodic-slot row
                "object-1" to recentFarSlot,
            )
        }

    // Day-of-week wrap-around: a Sunday (just before Monday) is 1 step, not 6.
    @Test
    fun `wraps day of week across the week boundary`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            val query = moment(0, 9, 0) // Monday 09:00
            val sunday = moment(6, 9, 0) // one step behind Monday
            val wednesday = moment(2, 9, 0) // two steps ahead
            mem.seen("object-0", sunday)
            mem.seen("object-1", wednesday)
            for (i in 2..9) mem.seen("object-$i", moment(5, 21, 0))

            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = query, k = 2))
            // Sunday (wrap distance 1) must be ranked before Wednesday (distance 2).
            assertNearby(result, "object-0" to sunday, "object-1" to wednesday)
        }

    // Wraps time-of-day across midnight (23:59 -> 00:xx treated as near).
    @Test
    fun `wraps time of day across midnight`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            val query = moment(0, 0, 5) // Monday 00:05
            val afterMidnight = moment(0, 0, 30)
            val sameDayLate = moment(0, 23, 30)
            mem.seen("object-0", afterMidnight) // 25 min ahead, on the wrapped side
            mem.seen("object-1", sameDayLate) // 23h25m back, far in linear ms
            for (i in 2..9) mem.seen("object-$i", moment(1, 12, 0))

            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = query, k = 2))
            // 00:30 is ~25 minutes from 00:05 on the wrapped circle -> ranks first.
            assertNearby(result, "object-0" to afterMidnight, "object-1" to sameDayLate)
        }

    // A single exact query moment returns that exact historical row with ~0 offset.
    @Test
    fun `exact moment returns the exact matching row`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            val query = moment(1, 14, 30) // Tuesday 14:30
            val otherSlots =
                listOf(
                    moment(0, 9, 0),
                    moment(2, 18, 15),
                    moment(3, 7, 45),
                    moment(4, 12, 0),
                    moment(5, 20, 5),
                    moment(6, 6, 30),
                )
            mem.seen("object-0", query)
            mem.seen("object-0", moment(4, 9, 0)) // same object, distant slot
            for (i in 1..8) mem.seen("object-$i", otherSlots[i % otherSlots.size])
            mem.seen("object-9", query) // another object sharing the exact moment

            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = query, k = 2))
            // object-0 and object-9 both hold the exact query row, so they are the top two
            // (mutual order may vary since they are equidistant).
            assertEquals(setOf("object-0", "object-9"), result.matchedObjects.toSet())
            assertEquals(2, result.ranked.size)
            result.ranked.forEach {
                assertTrue(it.objectId == "object-0" || it.objectId == "object-9")
                assertEquals(query, it.matchedEpochMillis, "epoch for ${it.objectId}")
            }
        }

    // Multiple weekly occurrences of one object are deduplicated into a single entry
    // carrying the nearest historical row's epoch.
    @Test
    fun `dedups repeated rows of one object and reports nearest epoch`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            val query = moment(0, 9, 0) // Monday 09:00
            // object-0 has several rows; the query-exact Monday row is the nearest.
            val exact = moment(0, 9, 0)
            val row2 = moment(2, 11, 0)
            val row3 = moment(5, 16, 30)
            mem.seen("object-0", row2)
            mem.seen("object-0", exact)
            mem.seen("object-0", row3)
            for (i in 1..9) mem.seen("object-$i", moment(4, 13, 0)) // far padding

            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = query, k = 5))
            assertEquals(1, result.matchedObjects.count { it == "object-0" }, "dedup object-0")
            val first = result.ranked.first()
            assertEquals("object-0", first.objectId)
            assertEquals(exact, first.matchedEpochMillis, "nearest row of object-0")
            assertTrue(result.ranked.none { it.objectId != "object-0" && it.matchedEpochMillis == exact })
        }

    // Large dataset (20+ rows) spread over the periodic grid; top-k correctness end-to-end.
    @Test
    fun `large periodic dataset returns correct top k`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            val query = moment(0, 9, 0) // Monday 09:00
            // Each object has a query-time slot (Monday 09:00) on alternating weeks, plus two
            // filler rows on distant weekdays/times, giving 30 rows in total.
            val slots = mutableMapOf<String, Long>()
            repeat(10) { i ->
                val weekOffset = if (i % 2 == 0) -7 * 86_400_000L else 0L
                val slot = moment(0, 9, 0) + weekOffset
                mem.seen("object-$i", slot)
                slots["object-$i"] = slot
                mem.seen("object-$i", moment((i + 2) % 7, 23, 0))
                mem.seen("object-$i", moment((i + 5) % 7, 6, 0))
            }
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = query, k = 10))
            assertEquals(10, result.ranked.size)
            assertEquals(10, result.matchedObjects.size)
            assertEquals(slots.keys, result.matchedObjects.toSet())
            // Each object's match points at its own Monday-09:00 slot, not the filler rows.
            slots.forEach { (id, epoch) ->
                val match = result.ranked.first { it.objectId == id }
                assertEquals(epoch, match.matchedEpochMillis, "epoch for $id")
            }
        }

    // Enrolled-but-never-seen objects never appear in the results.
    @Test
    fun `enrolled but unseen objects are excluded`() =
        runBlocking {
            val (mem, _) = repoWithObjects(12)
            val seenIds = listOf("object-0", "object-1", "object-2")
            val slot = moment(0, 8, 30)
            seenIds.forEach { mem.seen(it, slot) }
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = slot, k = 10))
            assertEquals(seenIds, result.matchedObjects.toList().sorted())
            assertEquals(seenIds.toSet(), result.matchedObjects.toSet())
            assertTrue(result.matchedObjects.none { it !in seenIds })
        }

    // Result carries the matched tag and a positive confidence for each object.
    @Test
    fun `matches expose object id tag and confidence`() =
        runBlocking {
            val (mem, _) = repoWith("alice", "bob", "carol", "dave")
            val slot = moment(0, 10, 0)
            mem.seen("object-0", slot)
            mem.seen("object-1", slot)
            mem.seen("object-2", slot + 3_600_000)
            mem.seen("object-3", slot + 7_200_000)
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = slot, k = 2))
            assertEquals(2, result.ranked.size)
            result.ranked.forEach {
                assertNotNull(it.tag)
                assertTrue(it.confidence > 0)
                assertTrue(it.vectorDistance >= 0)
                assertTrue(it.matchedEpochMillis > 0)
            }
            assertEquals("object-1", result.ranked[0].objectId)
            assertEquals("bob", result.ranked[0].tag)
        }

    // ----- edge cases ------------------------------------------------------

    @Test
    fun `k larger than available returns all seen objects`() =
        runBlocking {
            val (mem, _) = repoWithObjects(8)
            val slot = moment(0, 9, 0)
            (0 until 3).forEach { mem.seen("object-$it", slot + it * 60_000) }
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = slot, k = 100))
            assertEquals(3, result.ranked.size)
            assertEquals(3, result.matchedObjects.size)
            assertEquals(setOf("object-0", "object-1", "object-2"), result.matchedObjects.toSet())
        }

    @Test
    fun `k equal to zero returns nothing`() =
        runBlocking {
            val (mem, _) = repoWithObjects(5)
            val slot = moment(0, 9, 0)
            (0 until 5).forEach { mem.seen("object-$it", slot + it * 60_000) }
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = slot, k = 0))
            assertEquals(0, result.ranked.size)
            assertEquals(0, result.matchedObjects.size)
        }

    @Test
    fun `negative k returns nothing`() =
        runBlocking {
            val (mem, _) = repoWithObjects(5)
            val slot = moment(0, 9, 0)
            (0 until 5).forEach { mem.seen("object-$it", slot + it * 60_000) }
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = slot, k = -1))
            assertEquals(0, result.ranked.size)
            assertEquals(0, result.matchedObjects.size)
        }

    @Test
    fun `empty repository returns empty result`() =
        runBlocking {
            val (mem, _) = repoWith()
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = moment(0, 9, 0), k = 5))
            assertEquals(0, result.ranked.size)
            assertEquals(0, result.matchedObjects.size)
        }

    @Test
    fun `null query epoch falls back to a default`() =
        runBlocking {
            val (mem, _) = repoWithObjects(4)
            val slot = moment(0, 9, 0)
            (0 until 4).forEach { mem.seen("object-$it", slot + it * 60_000) }
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = null, k = 2))
            // Must not throw; returns up to k matches in a stable order.
            assertEquals(2, result.ranked.size)
            assertEquals(2, result.matchedObjects.size)
        }

    @Test
    fun `query within a tight cluster returns the nearest members`() =
        runBlocking {
            val (mem, _) = repoWithObjects(10)
            // Usages every minute from Wednesday 12:00; query at 12:02.
            val base = moment(2, 12, 0)
            repeat(10) { i -> mem.seen("object-$i", base + i * 60_000) }
            val query = base + 2 * 60_000 // Wednesday 12:02
            val result = mem.nearbyUsage(NearbyUsageRequest(atEpochMillis = query, k = 2))
            assertEquals(2, result.ranked.size)
            // object-2 sits exactly at the query minute, object-1/object-3 are adjacent.
            assertTrue("object-2" in result.matchedObjects)
            val first = result.ranked.first()
            assertEquals("object-2", first.objectId)
            assertEquals(query, first.matchedEpochMillis, "epoch for ${first.objectId}")
        }
}
