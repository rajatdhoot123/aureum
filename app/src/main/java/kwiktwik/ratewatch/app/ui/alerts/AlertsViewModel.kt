package kwiktwik.ratewatch.app.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kwiktwik.ratewatch.app.data.model.AlertAssetType
import kwiktwik.ratewatch.app.data.model.AlertCondition
import kwiktwik.ratewatch.app.data.model.CityPrice
import kwiktwik.ratewatch.app.data.model.PriceAlert
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.data.repository.PriceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val alerts: List<PriceAlert> = emptyList(),
    val currentPrices: CityPrice? = null,
    val isLoading: Boolean = false,
    val lastRefreshed: Long = 0L
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val priceRepo: PriceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState(isLoading = true))
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        // Observe alerts from DataStore
        viewModelScope.launch {
            prefs.alertsFlow().collect { alerts ->
                _uiState.update { it.copy(alerts = alerts) }
            }
        }
        // Initial price fetch
        refreshPrices()
        // Foreground auto-refresh every 5 minutes
        startForegroundRefresh()
    }

    private fun startForegroundRefresh() {
        viewModelScope.launch {
            while (isActive) {
                delay(5 * 60 * 1000L) // 5 minutes
                refreshPrices()
            }
        }
    }

    fun refreshPrices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            priceRepo.getGoldSilverPrices()
                .onSuccess { response ->
                    val cityPrice = response.data.firstOrNull()
                    _uiState.update {
                        it.copy(
                            currentPrices = cityPrice,
                            isLoading = false,
                            lastRefreshed = System.currentTimeMillis()
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    fun createAlert(assetType: AlertAssetType, condition: AlertCondition, targetPrice: Double) {
        viewModelScope.launch {
            val alert = PriceAlert(
                assetType = assetType,
                condition = condition,
                targetPrice = targetPrice
            )
            prefs.saveAlert(alert)
        }
    }

    fun toggleAlert(alertId: String) {
        viewModelScope.launch {
            val alerts = prefs.getAlerts()
            val alert = alerts.find { it.id == alertId } ?: return@launch
            prefs.saveAlert(alert.copy(enabled = !alert.enabled))
        }
    }

    fun deleteAlert(alertId: String) {
        viewModelScope.launch {
            prefs.deleteAlert(alertId)
        }
    }
}