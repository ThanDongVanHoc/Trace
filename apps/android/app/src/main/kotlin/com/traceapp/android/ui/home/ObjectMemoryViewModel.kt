package com.traceapp.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.traceapp.android.ui.pattern.UsageAxis
import com.traceapp.android.ui.pattern.UsagePattern
import com.traceapp.android.ui.pattern.UsageSeries
import com.traceapp.core.contracts.FindLastSeenResponse
import com.traceapp.core.contracts.MemoryApi
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.ObjectStore
import com.traceapp.core.contracts.SightingStore
import com.traceapp.core.contracts.TraceResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ObjectMemoryUiState(
    val loading: Boolean = true,
    val references: List<ObjectReference> = emptyList(),
    val selected: FindLastSeenResponse? = null,
    val error: String? = null,
    val usageAxis: UsageAxis = UsageAxis.HOUR_OF_DAY,
    val usagePeriodStartMillis: Long = 0L,
    val usageLoading: Boolean = false,
    val appUsage: UsageSeries? = null,
    val itemUsage: UsageSeries? = null,
    val usageCanStepForward: Boolean = false,
)

@HiltViewModel
class ObjectMemoryViewModel @Inject constructor(
    private val objectStore: ObjectStore,
    private val memoryApi: MemoryApi,
    private val sightingStore: SightingStore,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ObjectMemoryUiState())
    val state: StateFlow<ObjectMemoryUiState> = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            mutableState.update {
                it.copy(
                    loading = true,
                    error = null,
                    usagePeriodStartMillis = it.usagePeriodStartMillis.takeIf { s -> s > 0L }
                        ?: UsagePattern.currentPeriodStart(it.usageAxis, now),
                )
            }
            when (val result = objectStore.getAllReferences()) {
                is TraceResult.Success -> mutableState.update {
                    it.copy(loading = false, references = result.value)
                }
                is TraceResult.Failure -> mutableState.update {
                    it.copy(loading = false, error = result.error.message)
                }
            }
            reloadUsage()
        }
    }

    fun findLastSeen(objectId: String) {
        viewModelScope.launch {
            mutableState.update { it.copy(loading = true, error = null) }
            when (val result = memoryApi.findLastSeen(objectId)) {
                is TraceResult.Success -> mutableState.update {
                    it.copy(loading = false, selected = result.value)
                }
                is TraceResult.Failure -> mutableState.update {
                    it.copy(loading = false, error = result.error.message)
                }
            }
            reloadUsage()
        }
    }

    fun clearSelection() = mutableState.update { it.copy(selected = null, itemUsage = null) }

    fun delete(objectId: String) {
        viewModelScope.launch {
            when (val result = objectStore.delete(objectId)) {
                is TraceResult.Success -> {
                    val remaining = mutableState.value.references.filterNot { it.objectId == objectId }
                    mutableState.update {
                        it.copy(
                            references = remaining,
                            selected = it.selected?.takeIf { sel -> sel.objectId != objectId },
                            error = null,
                        )
                    }
                }
                is TraceResult.Failure -> mutableState.update {
                    it.copy(error = result.error.message)
                }
            }
            reloadUsage()
        }
    }

    fun setUsageAxis(axis: UsageAxis) {
        val now = System.currentTimeMillis()
        mutableState.update {
            it.copy(
                usageAxis = axis,
                usagePeriodStartMillis = UsagePattern.currentPeriodStart(axis, now),
            )
        }
        reloadUsage()
    }

    /** Moves the window by ±1 period (day in hour-mode, week in weekday-mode). */
    fun stepUsage(delta: Int) {
        val state = mutableState.value
        val next = UsagePattern.stepPeriod(state.usageAxis, state.usagePeriodStartMillis, delta)
        mutableState.update { it.copy(usagePeriodStartMillis = next) }
        reloadUsage()
    }

    private fun reloadUsage() {
        val now = System.currentTimeMillis()
        val axis = mutableState.value.usageAxis
        val periodStart = mutableState.value.usagePeriodStartMillis.takeIf { it > 0L }
            ?: UsagePattern.currentPeriodStart(axis, now)
        val selectedObjectId = mutableState.value.selected?.objectId
        viewModelScope.launch {
            mutableState.update {
                it.copy(
                    usagePeriodStartMillis = periodStart,
                    usageLoading = true,
                    usageCanStepForward = UsagePattern.canStepForward(axis, periodStart, now),
                )
            }
            val period = UsagePattern.periodFor(axis, periodStart)
            val appSeries = when (val result = sightingStore.getAllTimestamps(period.startEpochMillis, period.endEpochMillis)) {
                is TraceResult.Success -> UsagePattern.series(axis, periodStart, result.value, now)
                is TraceResult.Failure -> null
            }
            val itemSeries = selectedObjectId?.let { objectId ->
                when (val result = sightingStore.getObjectTimestamps(objectId, period.startEpochMillis, period.endEpochMillis)) {
                    is TraceResult.Success -> UsagePattern.series(axis, periodStart, result.value, now)
                    is TraceResult.Failure -> null
                }
            }
            mutableState.update { it.copy(usageLoading = false, appUsage = appSeries, itemUsage = itemSeries) }
        }
    }
}
