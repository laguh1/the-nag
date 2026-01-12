package com.thenag.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thenag.data.local.entity.NagItem
import com.thenag.data.repository.NagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 * Manages UI state and business logic for displaying the list of nags.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadNags()
    }

    /**
     * Load nags from repository based on current filter.
     */
    private fun loadNags() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val flow = when (_uiState.value.selectedFilter) {
                FilterType.ALL -> repository.getAllNags()
                FilterType.ACTIVE -> repository.getActiveNags()
                FilterType.INACTIVE -> repository.getInactiveNags()
            }

            flow.catch { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            }.collect { nags ->
                _uiState.update {
                    it.copy(
                        nags = nags,
                        filteredNags = filterNags(nags, it.searchQuery),
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    /**
     * Handle user actions.
     */
    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.SearchQueryChanged -> {
                _uiState.update {
                    it.copy(
                        searchQuery = action.query,
                        filteredNags = filterNags(it.nags, action.query)
                    )
                }
            }

            is HomeAction.FilterSelected -> {
                _uiState.update { it.copy(selectedFilter = action.filter) }
                loadNags()
            }

            is HomeAction.ToggleActive -> {
                viewModelScope.launch {
                    repository.toggleActive(action.nagId)
                }
            }

            is HomeAction.DeleteNag -> {
                viewModelScope.launch {
                    repository.deleteNagById(action.nagId)
                }
            }

            HomeAction.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    /**
     * Filter nags based on search query.
     */
    private fun filterNags(nags: List<NagItem>, query: String): List<NagItem> {
        if (query.isBlank()) return nags

        val lowerQuery = query.lowercase()
        return nags.filter {
            it.name.lowercase().contains(lowerQuery) ||
                    it.event.lowercase().contains(lowerQuery)
        }
    }
}

/**
 * UI state for the Home screen.
 */
data class HomeUiState(
    val nags: List<NagItem> = emptyList(),
    val filteredNags: List<NagItem> = emptyList(),
    val selectedFilter: FilterType = FilterType.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Filter types for nags.
 */
enum class FilterType {
    ALL,
    ACTIVE,
    INACTIVE
}

/**
 * User actions on the Home screen.
 */
sealed interface HomeAction {
    data class SearchQueryChanged(val query: String) : HomeAction
    data class FilterSelected(val filter: FilterType) : HomeAction
    data class ToggleActive(val nagId: Int) : HomeAction
    data class DeleteNag(val nagId: Int) : HomeAction
    object ClearError : HomeAction
}
