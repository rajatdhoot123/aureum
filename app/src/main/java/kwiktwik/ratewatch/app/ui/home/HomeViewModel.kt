package kwiktwik.ratewatch.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kwiktwik.ratewatch.app.data.model.CityPrice
import kwiktwik.ratewatch.app.data.model.StockQuote
import kwiktwik.ratewatch.app.data.remote.GrowwMarketCategoriesData
import kwiktwik.ratewatch.app.data.remote.GrowwMarketCategory
import kwiktwik.ratewatch.app.data.remote.GrowwMarketIndex
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.data.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val prices: List<CityPrice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val source: String = "",
    val lastUpdated: String = "",
    // New fields for market data
    val marketCategories: GrowwMarketCategoriesData? = null,
    val selectedCategory: GrowwMarketCategory? = null,
    val selectedIndex: GrowwMarketIndex? = null,
    val marketStocks: List<StockQuote> = emptyList(),
    val isMarketLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PriceRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val selectedCity: StateFlow<String> = prefs.selectedCityFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "India (approx)")

    init {
        refresh()
        loadMarketCategories()
    }

    fun setCity(city: String) {
        viewModelScope.launch {
            prefs.setSelectedCity(city)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getGoldSilverPrices()
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            prices = response.data,
                            isLoading = false,
                            source = response.source ?: "",
                            lastUpdated = response.lastScrapeAt ?: ""
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Failed to load prices"
                        )
                    }
                }
        }
    }

    /**
     * Fetches market categories and initial data.
     */
    fun loadMarketCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMarketLoading = true) }
            repository.getMarketCategories()
                .onSuccess { data ->
                    val initialCategory = data.sections.firstOrNull()
                    val initialIndex = data.indices.find { it.id == "GIDXNIFTYTOTALMCAP" } ?: data.indices.firstOrNull()
                    
                    _uiState.update {
                        it.copy(
                            marketCategories = data,
                            selectedCategory = initialCategory,
                            selectedIndex = initialIndex
                        )
                    }
                    
                    if (initialCategory != null) {
                        loadMarketData(initialCategory.id, initialIndex?.id)
                    } else {
                        _uiState.update { it.copy(isMarketLoading = false) }
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isMarketLoading = false) }
                }
        }
    }

    /**
     * Fetches market data for a specific category and index.
     */
    fun selectCategory(category: GrowwMarketCategory) {
        val currentState = _uiState.value
        if (currentState.selectedCategory?.id == category.id) return

        _uiState.update { it.copy(selectedCategory = category) }
        loadMarketData(category.id, currentState.selectedIndex?.id)
    }

    fun selectIndex(index: GrowwMarketIndex) {
        val currentState = _uiState.value
        if (currentState.selectedIndex?.id == index.id) return

        _uiState.update { it.copy(selectedIndex = index) }
        currentState.selectedCategory?.let { category ->
            loadMarketData(category.id, index.id)
        }
    }

    private fun loadMarketData(type: String, index: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isMarketLoading = true) }
            repository.getMarketData(type, index)
                .onSuccess { stocks ->
                    _uiState.update {
                        it.copy(
                            marketStocks = stocks,
                            isMarketLoading = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isMarketLoading = false) }
                }
        }
    }

    /**
     * Forces an immediate fresh scrape from Goodreturns.in (bypasses cache).
     * Use when /latest returns empty data.
     */
    fun forceMetalsScrape() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.triggerMetalsScrape()
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            prices = response.data,
                            isLoading = false,
                            source = response.source ?: "",
                            lastUpdated = response.lastScrapeAt ?: ""
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Scrape failed"
                        )
                    }
                }
        }
    }
}
