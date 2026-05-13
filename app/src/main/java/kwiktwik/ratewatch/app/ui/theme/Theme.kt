package kwiktwik.ratewatch.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Brush

// Premium Colors
val Navy950 = Color(0xFF020617)
val Navy900 = Color(0xFF0F172A)
val Navy800 = Color(0xFF1E293B)
val Cyan400 = Color(0xFF22D3EE)
val Cyan500 = Color(0xFF06B6D4)
val GoldAccent = Color(0xFFD4AF37)
val SilverAccent = Color(0xFF94A3B8)

private val LightColorScheme = lightColorScheme(
    primary = Cyan500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF083344),
    secondary = Navy800,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Navy950,
    surface = Color.White,
    onSurface = Navy950,
    surfaceVariant = Color(0xFFF1F5F9),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Cyan400,
    onPrimary = Navy950,
    primaryContainer = Color(0xFF083344),
    onPrimaryContainer = Color(0xFFCFFAFE),
    secondary = Color(0xFF94A3B8),
    onSecondary = Navy950,
    background = Navy950,
    onBackground = Color(0xFFF8FAFC),
    surface = Navy900,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Navy800,
    error = Color(0xFFFF6B6B),
    onError = Navy950,
)

@Composable
fun RateWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            // Make status bar transparent
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RateWatchTypography,
        content = content
    )
}

object GlassMorphism {
    @Composable
    fun backgroundBrush(darkTheme: Boolean): Brush {
        return if (darkTheme) {
            Brush.verticalGradient(
                colors = listOf(
                    Navy950,
                    Navy900,
                    Navy950
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF1F5F9),
                    Color(0xFFE2E8F0),
                    Color(0xFFF1F5F9)
                )
            )
        }
    }

    @Composable
    fun surfaceColor(darkTheme: Boolean): Color {
        return if (darkTheme) Color(0x1AFFFFFF) else Color(0x66FFFFFF)
    }

    @Composable
    fun strokeColor(darkTheme: Boolean): Color {
        return if (darkTheme) Color(0x33FFFFFF) else Color(0x4D000000)
    }
}

