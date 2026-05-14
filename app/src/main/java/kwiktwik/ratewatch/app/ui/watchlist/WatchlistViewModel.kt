package kwiktwik.ratewatch.app.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kwiktwik.ratewatch.app.data.model.StockQuote
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.data.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val priceRepo: PriceRepository
) : ViewModel() {

    val watchlistSymbols: StateFlow<Set<String>> = prefs.watchlistFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _selectedCategory = MutableStateFlow("Indian Market")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _quotes = MutableStateFlow<List<StockQuote>>(emptyList())
    val quotes: StateFlow<List<StockQuote>> = combine(_quotes, _selectedCategory) { quotes, category ->
        // In a real app, you'd filter by category or instrument type
        // For now we'll just return the quotes
        quotes
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _usStocks = MutableStateFlow<List<StockQuote>>(emptyList())
    val usStocks: StateFlow<List<StockQuote>> = _usStocks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedStock = MutableStateFlow<StockQuote?>(null)
    val selectedStock = _selectedStock.asStateFlow()

    private val _isDetailsLoading = MutableStateFlow(false)
    val isDetailsLoading = _isDetailsLoading.asStateFlow()

    fun loadWatchlistQuotes() {
        viewModelScope.launch {
            val symbols = watchlistSymbols.value.toList()
            if (symbols.isEmpty()) {
                _quotes.value = emptyList()
                return@launch
            }

            _isLoading.value = true
            priceRepo.getStockQuotes(symbols).collect { result ->
                result.onSuccess { _quotes.value = it }
                _isLoading.value = false
            }
        }
    }

    fun loadUsStocks() {
        viewModelScope.launch {
            _isLoading.value = true
            priceRepo.getUsStockQuotes().onSuccess { _usStocks.value = it }
            _isLoading.value = false
        }
    }
    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
    fun addToWatchlist(symbol: String) {
        viewModelScope.launch {
            prefs.addToWatchlist(symbol)
        }
    }
    fun removeFromWatchlist(symbol: String) {
        viewModelScope.launch {
            prefs.removeFromWatchlist(symbol)
        }
    }

    fun fetchDetails(searchId: String) {
        viewModelScope.launch {
            _isDetailsLoading.value = true
            priceRepo.getGrowwDetails(searchId)
                .onSuccess { _selectedStock.value = it }
                .onFailure { /* Handle error */ }
            _isDetailsLoading.value = false
        }
    }

    fun clearDetails() {
        _selectedStock.value = null
    }
}
