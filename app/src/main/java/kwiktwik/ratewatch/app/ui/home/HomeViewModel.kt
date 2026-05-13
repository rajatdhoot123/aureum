package kwiktwik.ratewatch.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kwiktwik.ratewatch.app.data.model.CityPrice
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
    val lastUpdated: String = ""
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
