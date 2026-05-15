package kwiktwik.ratewatch.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Brush

// Aureum Modern Fintech Palette - Deep Navy/Charcoal
val AureumBg = Color(0xFF031427)
val AureumSurface = Color(0xFF0A1F3A)
val AureumCard = Color(0xFF122A47)
val Cyan400 = Color(0xFF22D3EE)
val Cyan500 = Color(0xFF06B6D4)
val GoldAccent = Color(0xFFFFD700) // Brighter Gold for accents
val AureumGold = Color(0xFFFFCC33) // Brand gold color
val SilverAccent = Color(0xFF94A3B8)
val EmeraldGreen = Color(0xFF10B981)
val RubyRed = Color(0xFFEF4444)
val ChartGreen = Color(0xFF34D399)
val SearchBarBg = Color(0xFF0D1E33)

private val LightColorScheme = lightColorScheme(
    primary = Cyan500,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF083344),
    secondary = AureumSurface,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = AureumBg,
    surface = Color.White,
    onSurface = AureumBg,
    surfaceVariant = Color(0xFFF1F5F9),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = Cyan400,
    onPrimary = AureumBg,
    primaryContainer = Color(0xFF0A2A4A),
    onPrimaryContainer = Color(0xFFCFFAFE),
    secondary = Color(0xFF94A3B8),
    onSecondary = AureumBg,
    background = AureumBg,
    onBackground = Color(0xFFF1F5F9),
    surface = AureumSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = AureumCard,
    error = RubyRed,
    onError = Color.White,
)

@Composable
fun AureumTheme(
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
        typography = AureumTypography,
        content = content
    )
}

object GlassMorphism {
    @Composable
    fun backgroundBrush(darkTheme: Boolean): Brush {
        return if (darkTheme) {
            Brush.verticalGradient(
                colors = listOf(
                    AureumBg,
                    AureumSurface,
                    AureumBg
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
        return if (darkTheme) AureumCard else Color(0x66FFFFFF)
    }

    @Composable
    fun strokeColor(darkTheme: Boolean): Color {
        return if (darkTheme) Color(0x22FFFFFF) else Color(0x4D000000)
    }
}

/**
 * Reusable modifier for the Aureum glassmorphism gradient background.
 * Respects system light/dark theme automatically.
 *
 * Example:
 *   Box(Modifier.fillMaxSize().aureumBackground()) { ... }
 */
fun Modifier.aureumBackground(): Modifier = composed {
    background(AureumBg)
}

