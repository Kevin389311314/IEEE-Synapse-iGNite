package com.example.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnalysisResult
import com.example.data.model.RiskLevel
import com.example.data.repository.AnalysisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryFilterState(
    val searchQuery: String = "",
    val riskFilter: RiskLevel? = null,
    val selectedDetail: AnalysisResult? = null,
    val showClearDialog: Boolean = false
)

class HistoryViewModel(
    private val repository: AnalysisRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(HistoryFilterState())
    val filterState: StateFlow<HistoryFilterState> = _filterState

    val historyList: StateFlow<List<AnalysisResult>> = combine(
        repository.allHistory,
        _filterState
    ) { allItems, filter ->
        allItems.filter { item ->
            val matchesRisk = filter.riskFilter == null || item.riskLevel == filter.riskFilter
            val matchesQuery = filter.searchQuery.isBlank() ||
                    item.rawInput.contains(filter.searchQuery, ignoreCase = true) ||
                    item.title.contains(filter.searchQuery, ignoreCase = true) ||
                    item.explanation.contains(filter.searchQuery, ignoreCase = true)
            matchesRisk && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun setRiskFilter(level: RiskLevel?) {
        _filterState.update { it.copy(riskFilter = level) }
    }

    fun selectDetail(item: AnalysisResult?) {
        _filterState.update { it.copy(selectedDetail = item) }
    }

    fun setShowClearDialog(show: Boolean) {
        _filterState.update { it.copy(showClearDialog = show) }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteAnalysis(id)
            if (_filterState.value.selectedDetail?.id == id) {
                _filterState.update { it.copy(selectedDetail = null) }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _filterState.update { it.copy(showClearDialog = false, selectedDetail = null) }
        }
    }

    class Factory(private val repository: AnalysisRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }
    }
}
