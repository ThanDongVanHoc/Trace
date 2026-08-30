package com.traceapp.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.traceapp.core.contracts.FindLastSeenResponse
import com.traceapp.core.contracts.MemoryApi
import com.traceapp.core.contracts.ObjectReference
import com.traceapp.core.contracts.ObjectStore
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
)

@HiltViewModel
class ObjectMemoryViewModel @Inject constructor(
    private val objectStore: ObjectStore,
    private val memoryApi: MemoryApi,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ObjectMemoryUiState())
    val state: StateFlow<ObjectMemoryUiState> = mutableState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            mutableState.update { it.copy(loading = true, error = null) }
            when (val result = objectStore.getAllReferences()) {
                is TraceResult.Success -> mutableState.update {
                    it.copy(loading = false, references = result.value)
                }
                is TraceResult.Failure -> mutableState.update {
                    it.copy(loading = false, error = result.error.message)
                }
            }
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
        }
    }

    fun clearSelection() = mutableState.update { it.copy(selected = null) }
}
