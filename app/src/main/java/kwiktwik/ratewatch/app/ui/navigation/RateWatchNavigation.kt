package kwiktwik.ratewatch.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kwiktwik.ratewatch.app.R
import kwiktwik.ratewatch.app.ui.home.HomeScreen
import kwiktwik.ratewatch.app.ui.onboarding.OnboardingScreen
import kwiktwik.ratewatch.app.ui.settings.SettingsScreen
import kwiktwik.ratewatch.app.ui.startup.StartupScreen
import kwiktwik.ratewatch.app.ui.stocks.StocksScreen
import kwiktwik.ratewatch.app.ui.watchlist.WatchlistScreen
import kwiktwik.ratewatch.app.ui.theme.GlassMorphism

sealed class Screen(val route: String, val labelRes: Int, val emoji: String) {
    object Home : Screen("home", R.string.nav_home, "🏠")
    object Markets : Screen("markets", R.string.nav_markets, "📈")
    object Watchlist : Screen("watchlist", R.string.nav_watchlist, "⭐")
    object Settings : Screen("settings", R.string.nav_settings, "⚙️")
}

private val bottomNavItems = listOf(
    Screen.Home, Screen.Markets, Screen.Watchlist, Screen.Settings
)

@Composable
fun RateWatchNavigation(
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "startup") {

        // Startup decision screen - checks if user has completed onboarding
        composable("startup") {
            StartupScreen(navController = navController)
        }

        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            MainScaffold(navController) {
                HomeScreen(viewModel = hiltViewModel())
            }
        }
        composable(Screen.Markets.route) {
            MainScaffold(navController) {
                StocksScreen(viewModel = hiltViewModel())
            }
        }
        composable(Screen.Watchlist.route) {
            MainScaffold(navController) {
                WatchlistScreen(viewModel = hiltViewModel())
            }
        }
        composable(Screen.Settings.route) {
            MainScaffold(navController) {
                SettingsScreen(
                    viewModel = hiltViewModel(),
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}

@Composable
private fun MainScaffold(
    navController: androidx.navigation.NavController,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = GlassMorphism.surfaceColor(isDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassMorphism.strokeColor(isDark))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(80.dp)
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        
                        NavigationBarItem(
                            icon = { 
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(screen.emoji, style = MaterialTheme.typography.titleMedium) 
                                }
                            },
                            label = { 
                                Text(
                                    stringResource(screen.labelRes),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

