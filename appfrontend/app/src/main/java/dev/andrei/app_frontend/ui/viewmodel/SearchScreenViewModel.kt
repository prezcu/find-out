package dev.andrei.app_frontend.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrei.app_frontend.data.local.entity.LocationEntity
import dev.andrei.app_frontend.data.repository.LocationRepository
import dev.andrei.app_frontend.ui.state.SearchUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val repository: LocationRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Top suggestions for the autocomplete dropdown. Held separately from [uiState] so the dropdown
    // stays stable while a new query is loading (instead of flickering empty on every keystroke).
    private val _suggestions = MutableStateFlow<List<LocationEntity>>(emptyList())
    val suggestions: StateFlow<List<LocationEntity>> = _suggestions.asStateFlow()

    init {
        // Search-as-you-type: debounce keystrokes, then run the (typo-tolerant) backend search.
        // flatMapLatest cancels an in-flight search when the query changes again.
        _query
            .debounce(SEARCH_DEBOUNCE_MS)
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { q ->
                if (q.isEmpty()) {
                    flowOf<SearchUiState>(SearchUiState.Idle)
                } else {
                    flow<SearchUiState> {
                        emit(SearchUiState.Loading)
                        emit(
                            repository.searchLocationsByName(q).fold(
                                onSuccess = { SearchUiState.Success(it) },
                                onFailure = { SearchUiState.Error(it.message ?: "Search failed") }
                            )
                        )
                    }
                }
            }
            .onEach { state ->
                _uiState.value = state
                when (state) {
                    is SearchUiState.Success -> _suggestions.value = state.results.take(MAX_SUGGESTIONS)
                    is SearchUiState.Idle -> _suggestions.value = emptyList()
                    else -> Unit // keep previous suggestions during Loading/Error
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newValue: String) {
        _query.value = newValue
    }

    /** Hide the dropdown without changing the query (on suggestion tap, IME search, or focus loss). */
    fun dismissSuggestions() {
        _suggestions.value = emptyList()
    }

    fun clearQuery() {
        _query.value = ""
        _suggestions.value = emptyList()
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 280L
        const val MAX_SUGGESTIONS = 8
    }
}
