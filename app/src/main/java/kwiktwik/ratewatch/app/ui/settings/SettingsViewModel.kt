package kwiktwik.ratewatch.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.util.Language
import kwiktwik.ratewatch.app.util.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    val languageManager: LanguageManager
) : ViewModel() {

    val currentLanguage: StateFlow<Language> = prefs.getLanguageCodeFlow()
        .map { code -> languageManager.supportedLanguages.firstOrNull { it.code == code } ?: languageManager.supportedLanguages[0] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), languageManager.supportedLanguages[0])

    fun isDarkTheme(): Flow<Boolean?> = prefs.isDarkThemeFlow()

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkTheme(enabled) }
    }

    fun changeLanguage(context: Context, code: String) {
        viewModelScope.launch {
            languageManager.setLanguage(code)
            if (context is androidx.activity.ComponentActivity) {
                languageManager.changeAppLanguage(context, code)
            }
        }
    }
}
