package com.traceapp.feature.enrollment

import com.google.common.truth.Truth.assertThat
import com.traceapp.core.contracts.ImageInput
import com.traceapp.core.contracts.NormalizedRect
import com.traceapp.core.contracts.ObjectDraft
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.ObjectStore
import com.traceapp.core.contracts.SecureAsset
import com.traceapp.core.contracts.SecureAssetStore
import com.traceapp.core.contracts.SecureAssetType
import com.traceapp.core.contracts.TraceResult
import com.traceapp.core.contracts.VisualEmbedding
import com.traceapp.core.contracts.VisualEncoder
import com.traceapp.core.contracts.EnrollRequest
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EnrollmentServiceTest {
    @Test
    fun duplicateTag_isRejectedBeforeModelOrStorageWork() = runTest {
        val encoder = FakeEncoder()
        val assets = FakeAssetStore()
        val existing = reference(tag = "Ba lô đen")
        val service = EnrollmentService(encoder, FakeObjectStore(listOf(existing)), assets)

        val result = service.enroll(
            EnrollRequest(
                tag = "  BA LÔ   ĐEN ",
                image = ImageInput(byteArrayOf(1), 100, 100, 0, 1L),
                roi = NormalizedRect(0.1f, 0.1f, 0.9f, 0.9f),
            ),
        )

        assertThat(result).isInstanceOf(TraceResult.Failure::class.java)
        assertThat((result as TraceResult.Failure).error.message).contains("đã được dùng")
        assertThat(encoder.calls).isEqualTo(0)
        assertThat(assets.writeCalls).isEqualTo(0)
    }

    private fun reference(tag: String) = ObjectReference(
        referenceId = "reference-1",
        objectId = "object-1",
        tag = tag,
        imageAssetId = "asset-1",
        roi = NormalizedRect.FullImage,
        embeddings = listOf(VisualEmbedding(floatArrayOf(1f), "test", "1")),
        qualityScore = 1f,
        createdAtEpochMillis = 1L,
    )
}

private class FakeEncoder : VisualEncoder {
    var calls = 0

    override suspend fun encode(
        image: ImageInput,
        roi: NormalizedRect?,
    ): TraceResult<VisualEmbedding> {
        calls++
        return TraceResult.Success(VisualEmbedding(floatArrayOf(1f), "test", "1"))
    }
}

private class FakeObjectStore(private val references: List<ObjectReference>) : ObjectStore {
    override suspend fun create(draft: ObjectDraft) = TraceResult.Success(draft.reference)
    override suspend fun get(objectId: String) = TraceResult.Success(references.first())
    override suspend fun getAllReferences() = TraceResult.Success(references)
    override suspend fun delete(objectId: String) = TraceResult.Success(Unit)
}

private class FakeAssetStore : SecureAssetStore {
    var writeCalls = 0

    override suspend fun write(
        ownerRecordId: String,
        type: SecureAssetType,
        plaintext: ByteArray,
        mimeType: String,
    ): TraceResult<SecureAsset> {
        writeCalls++
        return TraceResult.Success(SecureAsset("asset", ownerRecordId, type, mimeType, 1L))
    }

    override suspend fun read(
        assetId: String,
        expectedOwnerRecordId: String,
        expectedType: SecureAssetType,
    ) = TraceResult.Success(byteArrayOf())

    override suspend fun delete(assetId: String) = TraceResult.Success(Unit)
}
