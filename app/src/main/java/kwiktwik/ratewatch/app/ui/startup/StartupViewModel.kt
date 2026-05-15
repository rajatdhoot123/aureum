package kwiktwik.ratewatch.app.ui.startup

import androidx.lifecycle.ViewModel
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val prefs: PreferencesRepository
) : ViewModel() {

    /**
     * One-shot read from DataStore. Much more reliable for startup decision
     * than collecting a StateFlow.
     */
    suspend fun isOnboardingCompleted(): Boolean {
        return prefs.isOnboardingCompletedFlow().first()
    }
}
