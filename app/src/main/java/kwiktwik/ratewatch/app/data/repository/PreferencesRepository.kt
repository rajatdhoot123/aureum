package kwiktwik.ratewatch.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kwiktwik.ratewatch.app.data.model.PriceAlert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ratewatch_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val WATCHLIST_SYMBOLS = stringSetPreferencesKey("watchlist_symbols")
        val SELECTED_CITY = stringPreferencesKey("selected_city")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PRICE_ALERTS = stringSetPreferencesKey("price_alerts")
    }

    // Language
    fun getLanguageCodeFlow(): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[Keys.LANGUAGE_CODE] ?: "en" }

    fun getLanguageCode(): String =
        // For synchronous access at startup
        runCatching {
            kotlinx.coroutines.runBlocking {
                context.dataStore.data.map { it[Keys.LANGUAGE_CODE] }.first() ?: "en"
            }
        }.getOrDefault("en")

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }

    // Theme
    fun isDarkThemeFlow(): Flow<Boolean?> =
        context.dataStore.data.map { it[Keys.IS_DARK_THEME] }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_THEME] = enabled }
    }

    // Watchlist
    fun watchlistFlow(): Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.WATCHLIST_SYMBOLS] ?: emptySet()
        }

    suspend fun addToWatchlist(symbol: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WATCHLIST_SYMBOLS] ?: emptySet()
            prefs[Keys.WATCHLIST_SYMBOLS] = current + symbol
        }
    }

    suspend fun removeFromWatchlist(symbol: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.WATCHLIST_SYMBOLS] ?: emptySet()
            prefs[Keys.WATCHLIST_SYMBOLS] = current - symbol
        }
    }

    // Selected City
    fun selectedCityFlow(): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[Keys.SELECTED_CITY] ?: "India (approx)" }

    suspend fun setSelectedCity(city: String) {
        context.dataStore.edit { it[Keys.SELECTED_CITY] = city }
    }

    // Onboarding
    fun isOnboardingCompletedFlow(): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    // Price Alerts
    fun alertsFlow(): Flow<List<PriceAlert>> =
        context.dataStore.data.map { prefs ->
            PriceAlert.fromJsonSet(prefs[Keys.PRICE_ALERTS] ?: emptySet())
        }

    suspend fun getAlerts(): List<PriceAlert> =
        PriceAlert.fromJsonSet(
            context.dataStore.data.map { it[Keys.PRICE_ALERTS] ?: emptySet() }.first()
        )

    suspend fun saveAlert(alert: PriceAlert) {
        context.dataStore.edit { prefs ->
            val current = PriceAlert.fromJsonSet(prefs[Keys.PRICE_ALERTS] ?: emptySet())
            val updated = current.filter { it.id != alert.id } + alert
            prefs[Keys.PRICE_ALERTS] = PriceAlert.toJsonSet(updated)
        }
    }

    suspend fun deleteAlert(alertId: String) {
        context.dataStore.edit { prefs ->
            val current = PriceAlert.fromJsonSet(prefs[Keys.PRICE_ALERTS] ?: emptySet())
            prefs[Keys.PRICE_ALERTS] = PriceAlert.toJsonSet(current.filter { it.id != alertId })
        }
    }

    suspend fun updateAlerts(alerts: List<PriceAlert>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PRICE_ALERTS] = PriceAlert.toJsonSet(alerts)
        }
    }
}
