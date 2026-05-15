package kwiktwik.ratewatch.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import kwiktwik.ratewatch.app.ui.navigation.SonarNavigation
import kwiktwik.ratewatch.app.ui.theme.SonarTheme
import kwiktwik.ratewatch.app.util.LanguageManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var languageManager: LanguageManager

    @Inject
    lateinit var prefs: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        languageManager.applyLanguage(this)

        setContent {
            val isDarkTheme by prefs.isDarkThemeFlow().collectAsState(initial = true)
            SonarTheme(darkTheme = isDarkTheme ?: true) {
                SonarNavigation()
            }
        }
    }
}
