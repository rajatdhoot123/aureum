package kwiktwik.ratewatch.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kwiktwik.ratewatch.app.data.model.StockQuote
import kwiktwik.ratewatch.app.data.remote.GrowwSearchResultItem
import kwiktwik.ratewatch.app.data.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val priceRepo: PriceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GrowwSearchResultItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isDetailsLoading = MutableStateFlow(false)
    val isDetailsLoading = _isDetailsLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Emits a fully loaded StockQuote to trigger navigation to the detail page
    private val _navigateToDetail = MutableSharedFlow<StockQuote>(extraBufferCapacity = 1)
    val navigateToDetail = _navigateToDetail.asSharedFlow()

    init {
        searchQuery
            .debounce(300)
            .filter { it.length >= 2 }
            .onEach { performSearch(it) }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.length < 2) _searchResults.value = emptyList()
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            priceRepo.searchGroww(query)
                .onSuccess { _searchResults.value = it }
                .onFailure {
                    _searchResults.value = emptyList()
                    _error.value = "Search failed"
                }
            _isLoading.value = false
        }
    }

    fun fetchDetails(searchId: String) {
        if (searchId.isEmpty()) { _error.value = "Invalid instrument ID"; return }
        viewModelScope.launch {
            _isDetailsLoading.value = true
            _error.value = null
            priceRepo.getGrowwDetails(searchId)
                .onSuccess { _navigateToDetail.emit(it) }
                .onFailure { _error.value = "Failed to load details: ${it.message}" }
            _isDetailsLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
