package com.trace.playground.memory

import com.trace.playground.contracts.MemoryEngine
import com.trace.playground.contracts.MemoryResult
import com.trace.playground.contracts.RecordSightingRequest
import com.trace.playground.contracts.Sighting
import com.trace.playground.contracts.TraceRepository

/** Thành viên 3 triển khai retrieval, deduplication và timeline tại đây. */
class MemoryAlgorithm(
    private val repository: TraceRepository,
) : MemoryEngine {
    override suspend fun record(request: RecordSightingRequest): Sighting =
        repository.recordSighting(request)

    override suspend fun find(query: String): List<MemoryResult> {
        require(query.isNotBlank()) { "query must not be blank" }
        return repository.findObjects(query)
    }

    override suspend fun timeline(objectId: String, limit: Int): List<Sighting> =
        repository.timeline(objectId, limit)
}
