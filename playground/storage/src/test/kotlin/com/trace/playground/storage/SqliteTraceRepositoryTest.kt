package com.trace.playground.storage

import com.trace.playground.contracts.EnrollmentResult
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.ReferenceVector
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class SqliteTraceRepositoryTest {
    @Test
    fun `persists enrollment and sighting without external database`() {
        val repository = SqliteTraceRepository(createTempDirectory().resolve("trace.db"))
        repository.initialize()
        val reference = ReferenceVector("ref-1", "object-1", "balo", listOf(0.1f), "test", "1")
        repository.saveEnrollment(
            EnrollmentResult("object-1", "ref-1", "balo", 1f, reference),
            "blobs/ref-1.jpg",
        )

        repository.recordSighting(RecordSightingRequest("object-1", 1000L, 0.9f))

        assertEquals("balo", repository.findObjects("bal").single().tag)
        assertEquals(1, repository.timeline("object-1", 10).size)
    }
}
