package com.trace.playground.memory

import com.trace.playground.contracts.EnrollmentResult
import com.trace.playground.contracts.LocationInput
import com.trace.playground.contracts.MemoryResult
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.ReferenceVector
import com.trace.playground.contracts.Sighting
import com.trace.playground.storage.SqliteTraceRepository
import kotlinx.coroutines.runBlocking
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val LAT = 10.7769
private const val LON = 106.7009
private val base = LocationInput(LAT, LON)
private val closePositive = LocationInput(LAT + 0.0001, LON) // ~11 m, under 30 m
private val farPositive = LocationInput(LAT + 0.001, LON) // ~111 m, over 30 m
private val closeNegative = LocationInput(LAT - 0.0001, LON) // ~11 m, under 30 m
private val farNegative = LocationInput(LAT - 0.001, LON) // ~111 m, over 30 m

private class Day(
    val memory: MemoryAlgorithm,
    val requests: List<RecordSightingRequest>,
    val results: List<Sighting>,
    val stored: Map<String, List<Long>>,
)

class MemoryAlgorithmTest {
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


    // ---- record / deduplication ----

    @Test
    fun `record persists the first sighting for an object`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            val stored = memory.record(RecordSightingRequest("object-0", 1_000L, 0.9f, base))

            assertEquals("object-0", stored.objectId)
            assertEquals(1, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record dedups a second sighting within two minutes when gps is absent`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 1_000L, 0.9f))
            memory.record(RecordSightingRequest("object-0", 2_000L, 0.8f))

            assertEquals(1, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record persists a sighting outside the two-minute window`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 0L, 0.9f))
            memory.record(RecordSightingRequest("object-0", 121_000L, 0.8f))

            assertEquals(2, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record persists a sighting exactly two minutes later`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 0L, 0.9f))
            memory.record(RecordSightingRequest("object-0", 120_000L, 0.8f))

            assertEquals(2, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record dedups a close gps sighting within two minutes`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 0L, 0.9f, base))
            memory.record(RecordSightingRequest("object-0", 5_000L, 0.8f, closePositive))

            assertEquals(1, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record persists a far gps sighting within two minutes`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 0L, 0.9f, base))
            memory.record(RecordSightingRequest("object-0", 5_000L, 0.8f, farPositive))

            assertEquals(2, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record persists a same-location sighting outside the two-minute window`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 0L, 0.9f, base))
            memory.record(RecordSightingRequest("object-0", 121_000L, 0.8f, base))

            assertEquals(2, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record dedups when the new request has no gps within two minutes`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 0L, 0.9f, base))
            memory.record(RecordSightingRequest("object-0", 5_000L, 0.8f))

            assertEquals(1, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record dedups when the previous sighting has no gps`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 0L, 0.9f))
            memory.record(RecordSightingRequest("object-0", 5_000L, 0.8f, farPositive))

            assertEquals(1, memory.timeline("object-0", 10).size)
        }

    @Test
    fun `record returns the existing latest sighting when deduped`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            val first = memory.record(RecordSightingRequest("object-0", 0L, 0.9f, base))
            val deduped = memory.record(RecordSightingRequest("object-0", 5_000L, 0.8f, closePositive))

            assertEquals(first.sightingId, deduped.sightingId)
        }

    @Test
    fun `record rejects an unknown object`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            assertFailsWith<IllegalArgumentException> {
                memory.record(RecordSightingRequest("missing", 1_000L, 0.9f))
            }
        }

    @Test
    fun `record rejects confidence outside zero to one`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            assertFailsWith<IllegalArgumentException> {
                memory.record(RecordSightingRequest("object-0", 1_000L, 1.5f))
            }
        }

    // ---- find ----

    @Test
    fun `find returns the latest sighting without an input image`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 1_000L, 0.9f))
            memory.record(RecordSightingRequest("object-0", 2_000L, 0.8f))

            assertEquals(
                1_000L,
                memory
                    .find("BAL")
                    .single()
                    .lastSeen
                    ?.detectedAtEpochMillis,
            )
        }

    @Test
    fun `find returns lastSeen null for an object with no sightings`() =
        runBlocking {
            val (memory, _) = repoWith("balo")

            assertNull(memory.find("balo").single().lastSeen)
        }

    @Test
    fun `find orders results by most recent sighting first`() =
        runBlocking {
            val (memory, _) = repoWith("balo", "balo cũ")
            memory.record(RecordSightingRequest("object-0", 2_000L, 0.9f))
            memory.record(RecordSightingRequest("object-1", 5_000L, 0.9f))

            assertEquals(listOf("balo cũ", "balo"), memory.find("balo").map { it.tag })
        }

    @Test
    fun `find places objects never seen last`() =
        runBlocking {
            val (memory, _) = repoWith("balo", "balo cũ", "balo mới")
            memory.record(RecordSightingRequest("object-0", 2_000L, 0.9f))
            memory.record(RecordSightingRequest("object-1", 5_000L, 0.9f))

            assertEquals(listOf("balo cũ", "balo", "balo mới"), memory.find("balo").map { it.tag })
        }

    @Test
    fun `find rejects a blank query`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            assertFailsWith<IllegalArgumentException> { memory.find("   ") }
        }

    // ---- timeline ----

    @Test
    fun `timeline returns sightings in descending order`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            memory.record(RecordSightingRequest("object-0", 1_000L, 0.9f))
            memory.record(RecordSightingRequest("object-0", 121_000L, 0.9f))
            memory.record(RecordSightingRequest("object-0", 242_000L, 0.9f))

            assertEquals(
                listOf(242_000L, 121_000L, 1_000L),
                memory.timeline("object-0", 10).map { it.detectedAtEpochMillis },
            )
        }

    @Test
    fun `timeline applies the requested limit`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            listOf(0L, 121_000L, 242_000L, 363_000L, 484_000L).forEach {
                memory.record(RecordSightingRequest("object-0", it, 0.9f))
            }

            val limited = memory.timeline("object-0", 2)
            assertEquals(2, limited.size)
            assertEquals(listOf(484_000L, 363_000L), limited.map { it.detectedAtEpochMillis })
        }

    @Test
    fun `timeline rejects an unknown object`() =
        runBlocking {
            val (memory, _) = repoWith("balo")
            assertFailsWith<IllegalArgumentException> { memory.timeline("missing", 10) }
        }

    @Test
    fun `Multiple objects in short time`() =
        runBlocking {
            val (memory, _) = repoWith("balo", "deple", "non", "epmusic")

            memory.record(RecordSightingRequest("object-1", 1500L, 0.5f, null))
            memory.record(RecordSightingRequest("object-1", 13000L, 0.5f, null))
            memory.record(RecordSightingRequest("object-3", 14000L, 0.5f, null))
            memory.record(RecordSightingRequest("object-1", 14000L, 0.5f, null))
            memory.record(RecordSightingRequest("object-1", 111000L, 0.5f, null))
            memory.record(RecordSightingRequest("object-1", 141000L, 0.5f, null))
            memory.record(RecordSightingRequest("object-1", 341000L, 0.5f, null))

            val findResult = memory.find("ep")
            print(findResult.map { it -> it.objectId })
            assertEquals(2, findResult.size)
            assertEquals(listOf<Long>(341000L, 14000L), findResult.map { it.lastSeen?.detectedAtEpochMillis })
        }
    // -- Timeline testing -- 
}
