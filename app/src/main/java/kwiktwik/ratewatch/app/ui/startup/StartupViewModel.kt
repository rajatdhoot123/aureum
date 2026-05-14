package kwiktwik.ratewatch.app.ui.startup

import android.util.Log
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
        Log.d("Aureum", "StartupViewModel: Reading onboarding status from DataStore...")
        val completed = prefs.isOnboardingCompletedFlow().first()
        Log.d("Aureum", "StartupViewModel: isOnboardingCompleted = $completed")
        return completed
    }
}
