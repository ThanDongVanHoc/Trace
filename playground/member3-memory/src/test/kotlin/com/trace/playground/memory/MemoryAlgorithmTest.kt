package com.trace.playground.memory

import com.trace.playground.contracts.EnrollmentResult
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.ReferenceVector
import com.trace.playground.storage.SqliteTraceRepository
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryAlgorithmTest {
    @Test
    fun `find returns latest sighting without an input image`() = runBlocking {
        val repository = SqliteTraceRepository(createTempDirectory().resolve("trace.db"))
        repository.initialize()
        val reference = ReferenceVector("ref-1", "object-1", "balo", listOf(1f), "test", "1")
        repository.saveEnrollment(
            EnrollmentResult("object-1", "ref-1", "balo", 1f, reference),
            "blobs/ref-1.jpg",
        )
        val memory = MemoryAlgorithm(repository)
        memory.record(RecordSightingRequest("object-1", 1_000L, 0.9f))
        memory.record(RecordSightingRequest("object-1", 2_000L, 0.8f))

        assertEquals(2_000L, memory.find("BAL").single().lastSeen?.detectedAtEpochMillis)
    }
}
