package kwiktwik.ratewatch.app.ui.stocks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import kwiktwik.ratewatch.app.data.model.StockQuote
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.data.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StocksUiState(
    val quotes: List<StockQuote> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StocksViewModel @Inject constructor(
    private val repository: PriceRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StocksUiState(isLoading = true))
    val uiState: StateFlow<StocksUiState> = _uiState.asStateFlow()

    fun loadStocks(type: String = "top-gainers", index: String = "GIDXNIFTYTOTALMCAP") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Primary source: Groww market data (Top Gainers, etc. as requested by user)
            repository.getMarketData(type, index)
                .onSuccess { quotes ->
                    if (quotes.isNotEmpty()) {
                        Log.i("StocksViewModel", "Loaded ${quotes.size} items for $type / $index")
                        _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                        return@launch
                    }
                }
                .onFailure { Log.e("StocksViewModel", "Market data failed", it) }

            // Fallback 1: Groww-powered Indian indices
            val growwResult = repository.getGrowwIndianIndices()
            growwResult.onSuccess { quotes ->
                if (quotes.isNotEmpty()) {
                    Log.i("StocksViewModel", "Loaded ${quotes.size} indices from Groww")
                    _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                    return@launch
                }
            }

            // Fallback 2: Legacy popular quotes
            repository.getAllPopularStockQuotes()
                .onSuccess { quotes ->
                    _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load market data") }
                }
        }
    }

    fun loadGlobalInstruments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getGrowwGlobalInstruments()
                .onSuccess { quotes ->
                    Log.i("StocksViewModel", "Loaded ${quotes.size} global instruments from Groww")
                    _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                }
                .onFailure { throwable ->
                    Log.e("StocksViewModel", "Groww global failed", throwable)
                    _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load global markets") }
                }
        }
    }

    fun addToWatchlist(symbol: String) {
        viewModelScope.launch {
            prefs.addToWatchlist(symbol)
        }
    }
}
