package kwiktwik.ratewatch.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.util.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    val languageManager: LanguageManager
) : ViewModel() {

    suspend fun saveLanguageAndComplete(code: String) {
        prefs.setLanguageCode(code)
        prefs.setOnboardingCompleted(true)
    }
}
