package kwiktwik.ratewatch.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kwiktwik.ratewatch.app.ui.navigation.AureumNavigation
import kwiktwik.ratewatch.app.ui.theme.AureumTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var languageManager: kwiktwik.ratewatch.app.util.LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("Aureum", "=== MainActivity onCreate started ===")
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        languageManager.applyLanguage(this)
        Log.d("Aureum", "Language applied, setting Compose content")

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }

            AureumTheme(darkTheme = true) {
                AureumNavigation(
                    onThemeChange = { isDarkTheme = it }
                )
            }
        }
        Log.d("Aureum", "=== setContent called ===")
    }
}
