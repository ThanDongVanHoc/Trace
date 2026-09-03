package com.traceapp.android.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.traceapp.android.notification.TraceNotifier
import com.traceapp.core.contracts.EnrollRequest
import com.traceapp.core.contracts.EnrollmentApi
import com.traceapp.core.contracts.ImageInput
import com.traceapp.core.contracts.MatchStatus
import com.traceapp.core.contracts.MemoryApi
import com.traceapp.core.contracts.NormalizedRect
import com.traceapp.core.contracts.ObjectStore
import com.traceapp.core.contracts.RecognizeRequest
import com.traceapp.core.contracts.RecognitionApi
import com.traceapp.core.contracts.RecordSightingRequest
import com.traceapp.core.contracts.TraceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanMode { TAG, RECOGNIZE }

data class ScanUiState(
    val mode: ScanMode = ScanMode.TAG,
    val image: ImageInput? = null,
    val roi: NormalizedRect = NormalizedRect(0.15f, 0.2f, 0.85f, 0.8f),
    val tag: String = "",
    val busy: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val dataRevision: Int = 0,
    val canOpenFind: Boolean = false,
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val enrollmentApi: EnrollmentApi,
    private val recognitionApi: RecognitionApi,
    private val memoryApi: MemoryApi,
    private val objectStore: ObjectStore,
    private val locationReader: LocationReader,
    private val notifier: TraceNotifier,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = mutableState.asStateFlow()

    fun setMode(mode: ScanMode) {
        mutableState.value = ScanUiState(mode = mode)
    }

    fun setImage(image: ImageInput) {
        mutableState.update { it.copy(image = image, message = null, canOpenFind = false) }
    }

    fun setRoi(roi: NormalizedRect) = mutableState.update { it.copy(roi = roi) }

    fun setTag(tag: String) = mutableState.update { it.copy(tag = tag.take(80)) }

    fun resetCapture() = mutableState.update {
        it.copy(image = null, message = null, busy = false, canOpenFind = false)
    }

    fun enroll() {
        val snapshot = mutableState.value
        val image = snapshot.image ?: return
        if (snapshot.busy) return
        viewModelScope.launch {
            mutableState.update { it.copy(busy = true, message = null) }
            when (
                val result = enrollmentApi.enroll(
                    EnrollRequest(snapshot.tag, image, snapshot.roi),
                )
            ) {
                is TraceResult.Success -> mutableState.update {
                    it.copy(
                        busy = false,
                        message = "Đã lưu tag “${snapshot.tag.trim()}”.",
                        isError = false,
                        dataRevision = it.dataRevision + 1,
                    )
                }
                is TraceResult.Failure -> mutableState.update {
                    it.copy(busy = false, message = result.error.message, isError = true)
                }
            }
        }
    }

    fun recognize() {
        val snapshot = mutableState.value
        val image = snapshot.image ?: return
        if (snapshot.busy) return
        viewModelScope.launch {
            mutableState.update { it.copy(busy = true, message = null, canOpenFind = false) }
            val references = when (val result = objectStore.getAllReferences()) {
                is TraceResult.Success -> result.value
                is TraceResult.Failure -> {
                    fail(result.error.message)
                    return@launch
                }
            }
            if (references.isEmpty()) {
                fail("Chưa có đồ vật đã gắn tag để nhận diện.")
                return@launch
            }
            val recognition = when (
                val result = recognitionApi.recognize(
                    RecognizeRequest(image = image, references = references),
                )
            ) {
                is TraceResult.Success -> result.value
                is TraceResult.Failure -> {
                    fail(result.error.message)
                    return@launch
                }
            }
            val detection = recognition.detections.firstOrNull()
            val objectId = detection?.objectId
            if (detection == null || detection.status == MatchStatus.UNKNOWN || objectId == null) {
                mutableState.update {
                    it.copy(
                        busy = false,
                        message = "Không nhận ra đồ vật đã đăng ký.",
                        isError = false,
                    )
                }
                return@launch
            }
            val reference = references.first { it.objectId == objectId }
            val recorded = memoryApi.recordSighting(
                RecordSightingRequest(
                    objectId = objectId,
                    detectedAtEpochMillis = image.capturedAtEpochMillis,
                    confidence = detection.similarity,
                    boundingBox = detection.boundingBox,
                    location = locationReader.currentOrNull(),
                    evidenceImage = image,
                ),
            )
            when (recorded) {
                is TraceResult.Success -> {
                    notifier.sightingRecorded(reference.tag)
                    mutableState.update {
                        it.copy(
                            busy = false,
                            message = "Đã nhận ra “${reference.tag}” (${(detection.similarity * 100).toInt()}%).",
                            isError = false,
                            dataRevision = it.dataRevision + 1,
                            canOpenFind = true,
                        )
                    }
                }
                is TraceResult.Failure -> fail(recorded.error.message)
            }
        }
    }

    private fun fail(message: String) {
        mutableState.update { it.copy(busy = false, message = message, isError = true) }
    }
}
