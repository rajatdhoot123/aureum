package kwiktwik.ratewatch.app.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    private val prefs: PreferencesRepository
) {
    val supportedLanguages = listOf(
        Language("en", "English", "English"),
        Language("hi", "हिन्दी", "Hindi"),
        Language("ta", "தமிழ்", "Tamil"),
        Language("te", "తెలుగు", "Telugu"),
        Language("bn", "বাংলা", "Bengali"),
        Language("mr", "मराठी", "Marathi"),
        Language("gu", "ગુજરાતી", "Gujarati"),
        Language("kn", "ಕನ್ನಡ", "Kannada"),
        Language("ml", "മലയാളം", "Malayalam"),
        Language("pa", "ਪੰਜਾਬੀ", "Punjabi"),
    )

    fun getCurrentLanguageCode(): String = prefs.getLanguageCode()

    suspend fun setLanguage(languageCode: String) {
        prefs.setLanguageCode(languageCode)
    }

    fun applyLanguage(activity: Activity) {
        val code = getCurrentLanguageCode()
        if (code != "en") {
            val locale = Locale(code)
            Locale.setDefault(locale)

            val config = Configuration(activity.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                config.setLocale(locale)
            }

            @Suppress("DEPRECATION")
            activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        }
    }

    fun applyLanguageToContext(context: Context): Context {
        val code = getCurrentLanguageCode()
        if (code == "en") return context

        val locale = Locale(code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.setLocale(locale)
        }

        return context.createConfigurationContext(config)
    }

    fun changeAppLanguage(activity: Activity, languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        // Manual recreation ensures immediate update even if AppCompatDelegate 
        // takes a moment or doesn't trigger it on some versions/configurations.
        activity.recreate()
    }
}
