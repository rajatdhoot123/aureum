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

    fun loadStocks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Primary source: Groww-powered Indian indices (recommended, single call, rich data)
            val growwResult = repository.getGrowwIndianIndices()
            growwResult.onSuccess { quotes ->
                if (quotes.isNotEmpty()) {
                    Log.i("StocksViewModel", "Loaded ${quotes.size} indices from Groww")
                    _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                    return@launch
                } else {
                    Log.w("StocksViewModel", "Groww returned empty list, trying legacy fallback")
                }
            }.onFailure { throwable ->
                Log.e("StocksViewModel", "Groww /scraper/stocks/groww/indices failed", throwable)
            }

            // Fallback to legacy popular quotes (Yahoo-backed multi-symbol)
            repository.getAllPopularStockQuotes()
                .onSuccess { quotes ->
                    Log.i("StocksViewModel", "Loaded ${quotes.size} quotes via legacy /stocks/quote fallback")
                    _uiState.update { it.copy(quotes = quotes, isLoading = false) }
                }
                .onFailure { throwable ->
                    Log.e("StocksViewModel", "Legacy stocks/quote fallback also failed", throwable)
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
