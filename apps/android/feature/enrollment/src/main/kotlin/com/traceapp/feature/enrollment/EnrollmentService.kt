package com.traceapp.feature.enrollment

import com.traceapp.core.contracts.EnrollRequest
import com.traceapp.core.contracts.EnrollResponse
import com.traceapp.core.contracts.EnrollmentApi
import com.traceapp.core.contracts.ObjectDraft
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.ObjectStore
import com.traceapp.core.contracts.SecureAssetStore
import com.traceapp.core.contracts.SecureAssetType
import com.traceapp.core.contracts.TraceError
import com.traceapp.core.contracts.TraceErrorCode
import com.traceapp.core.contracts.TraceResult
import com.traceapp.core.contracts.VisualEncoder
import java.text.Normalizer
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnrollmentService @Inject constructor(
    private val visualEncoder: VisualEncoder,
    private val objectStore: ObjectStore,
    private val assetStore: SecureAssetStore,
) : EnrollmentApi {
    override suspend fun enroll(request: EnrollRequest): TraceResult<EnrollResponse> {
        val tag = request.tag.trim()
        if (tag.isEmpty() || tag.length > 80 || !request.roi.isValid) {
            return TraceResult.Failure(
                TraceError(TraceErrorCode.INVALID_INPUT, "Tag or ROI is invalid"),
            )
        }
        if (request.roi.area < MINIMUM_ROI_AREA) {
            return TraceResult.Failure(
                TraceError(TraceErrorCode.ROI_TOO_SMALL, "Selected area is too small"),
            )
        }
        if (request.image.jpegBytes.isEmpty()) {
            return TraceResult.Failure(
                TraceError(TraceErrorCode.INVALID_INPUT, "Image is empty"),
            )
        }

        val existingReferences = when (val result = objectStore.getAllReferences()) {
            is TraceResult.Success -> result.value
            is TraceResult.Failure -> return result
        }
        if (existingReferences.any { normalizeTag(it.tag) == normalizeTag(tag) }) {
            return TraceResult.Failure(
                TraceError(
                    TraceErrorCode.INVALID_INPUT,
                    "Tên “$tag” đã được dùng. Hãy chọn một tên khác để tránh nhầm lẫn.",
                ),
            )
        }

        val objectId = UUID.randomUUID().toString()
        val referenceId = UUID.randomUUID().toString()
        val embedding = when (val result = visualEncoder.encode(request.image, request.roi)) {
            is TraceResult.Success -> result.value
            is TraceResult.Failure -> return result
        }
        val asset = when (
            val result = assetStore.write(
                ownerRecordId = referenceId,
                type = SecureAssetType.REFERENCE_IMAGE,
                plaintext = request.image.jpegBytes,
                mimeType = "image/jpeg",
            )
        ) {
            is TraceResult.Success -> result.value
            is TraceResult.Failure -> return result
        }

        val reference = ObjectReference(
            referenceId = referenceId,
            objectId = objectId,
            tag = tag,
            imageAssetId = asset.assetId,
            roi = request.roi,
            embeddings = listOf(embedding),
            qualityScore = embedding.qualityScore,
            createdAtEpochMillis = System.currentTimeMillis(),
        )
        return when (val stored = objectStore.create(ObjectDraft(objectId, tag, reference))) {
            is TraceResult.Success -> TraceResult.Success(
                EnrollResponse(
                    objectId = objectId,
                    referenceId = referenceId,
                    qualityScore = reference.qualityScore,
                    embeddingCount = reference.embeddings.size,
                    warnings = emptyList(),
                ),
            )
            is TraceResult.Failure -> {
                assetStore.delete(asset.assetId)
                stored
            }
        }
    }

    private companion object {
        const val MINIMUM_ROI_AREA = 0.01f

        fun normalizeTag(value: String): String = Normalizer
            .normalize(value.trim(), Normalizer.Form.NFC)
            .replace(Regex("\\s+"), " ")
            .lowercase(Locale.ROOT)
    }
}
