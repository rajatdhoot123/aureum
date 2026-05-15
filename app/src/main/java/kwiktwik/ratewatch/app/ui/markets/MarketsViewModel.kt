package kwiktwik.ratewatch.app.ui.markets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kwiktwik.ratewatch.app.data.model.CityPrice
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.data.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketsUiState(
    val allCities: List<CityPrice> = emptyList(),
    val filteredCities: List<CityPrice> = emptyList(),
    val currentCity: CityPrice? = null,
    val searchQuery: String = "",
    val sortByPrice: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MarketsViewModel @Inject constructor(
    private val repository: PriceRepository,
    private val prefs: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketsUiState(isLoading = true))
    val uiState: StateFlow<MarketsUiState> = _uiState.asStateFlow()

    val selectedCity: StateFlow<String> = prefs.selectedCityFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Mumbai")

    init {
        refresh()
        // React to selected city changes
        viewModelScope.launch {
            selectedCity.collect { cityName ->
                updateCurrentCity(cityName)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            repository.getGoldSilverPrices()
                .onSuccess { response ->
                    val cities = response.data
                    val city = selectedCity.value
                    val current = cities.find {
                        it.city.equals(city, ignoreCase = true)
                    } ?: cities.find {
                        it.city.equals("Mumbai", ignoreCase = true)
                    }
                    val others = cities.filter { it != current }

                    _uiState.update {
                        it.copy(
                            allCities = others,
                            filteredCities = applyFilters(others, it.searchQuery, it.sortByPrice),
                            currentCity = current,
                            isLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Failed to load prices")
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredCities = applyFilters(it.allCities, query, it.sortByPrice)
            )
        }
    }

    fun toggleSortByPrice() {
        _uiState.update {
            val newSort = !it.sortByPrice
            it.copy(
                sortByPrice = newSort,
                filteredCities = applyFilters(it.allCities, it.searchQuery, newSort)
            )
        }
    }

    private fun updateCurrentCity(cityName: String) {
        val state = _uiState.value
        if (state.allCities.isEmpty() && state.currentCity == null) return

        val allData = buildList {
            state.currentCity?.let { add(it) }
            addAll(state.allCities)
        }
        val newCurrent = allData.find { it.city.equals(cityName, ignoreCase = true) }
            ?: state.currentCity
        val others = allData.filter { it != newCurrent }

        _uiState.update {
            it.copy(
                currentCity = newCurrent,
                allCities = others,
                filteredCities = applyFilters(others, it.searchQuery, it.sortByPrice)
            )
        }
    }

    private fun applyFilters(
        cities: List<CityPrice>,
        query: String,
        sortByPrice: Boolean
    ): List<CityPrice> {
        var result = if (query.isBlank()) cities
        else cities.filter { it.city.contains(query, ignoreCase = true) }

        if (sortByPrice) {
            result = result.sortedByDescending { it.gold24kPer10g ?: 0 }
        }
        return result
    }
}