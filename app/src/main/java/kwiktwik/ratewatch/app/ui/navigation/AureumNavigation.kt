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
import kwiktwik.ratewatch.app.data.model.StockQuote
import kwiktwik.ratewatch.app.ui.search.SearchScreen
import kwiktwik.ratewatch.app.ui.search.SearchViewModel
import kwiktwik.ratewatch.app.ui.stockdetail.StockDetailScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import kwiktwik.ratewatch.app.R
import kwiktwik.ratewatch.app.ui.alerts.AlertsScreen
import kwiktwik.ratewatch.app.ui.home.HomeScreen
import kwiktwik.ratewatch.app.ui.onboarding.OnboardingScreen
import kwiktwik.ratewatch.app.ui.settings.SettingsScreen
import kwiktwik.ratewatch.app.ui.startup.StartupScreen
import kwiktwik.ratewatch.app.ui.stocks.StocksScreen
import kwiktwik.ratewatch.app.ui.markets.MarketsScreen
import kwiktwik.ratewatch.app.ui.watchlist.WatchlistScreen
import kwiktwik.ratewatch.app.ui.watchlist.WatchlistViewModel
import kwiktwik.ratewatch.app.ui.theme.GlassMorphism
import kwiktwik.ratewatch.app.ui.theme.AureumBg
import kwiktwik.ratewatch.app.ui.theme.GoldAccent
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.TrendingUp

sealed class Screen(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", R.string.nav_home, Icons.Outlined.Home)
    object Watchlist : Screen("watchlist", R.string.nav_watchlist, Icons.Outlined.StarOutline)
    object Alerts : Screen("alerts", R.string.nav_alerts, Icons.Outlined.Notifications)
    object Markets : Screen("markets", R.string.nav_markets, Icons.Outlined.Category)
    object Movers : Screen("movers", R.string.nav_movers, Icons.Outlined.TrendingUp)
    object Search : Screen("search", R.string.nav_home, Icons.Outlined.Home) // Helper for route
}

private val bottomNavItems = listOf(
    Screen.Home, Screen.Watchlist, Screen.Alerts, Screen.Markets, Screen.Movers
)

@Composable
fun AureumNavigation(
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    // Shared state to pass StockQuote to detail screen (avoids serialization)
    var pendingDetailQuote by remember { mutableStateOf<StockQuote?>(null) }

    NavHost(navController = navController, startDestination = "startup") {

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
                HomeScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }
        }
        composable(Screen.Watchlist.route) {
            MainScaffold(navController) {
                WatchlistScreen(
                    viewModel = hiltViewModel(),
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) }
                )
            }
        }
        composable(Screen.Alerts.route) {
            MainScaffold(navController) {
                AlertsScreen(viewModel = hiltViewModel())
            }
        }
        composable(Screen.Markets.route) {
            MainScaffold(navController) {
                MarketsScreen(viewModel = hiltViewModel())
            }
        }
        composable(Screen.Movers.route) {
            MainScaffold(navController) {
                StocksScreen(viewModel = hiltViewModel(), isMoversTab = true)
            }
        }
        composable(Screen.Search.route) {
            val watchlistViewModel: WatchlistViewModel = hiltViewModel()
            SearchScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { quote ->
                    pendingDetailQuote = quote
                    navController.navigate("stock_detail")
                },
                onAddToWatchlist = { symbol -> watchlistViewModel.addToWatchlist(symbol) }
            )
        }
        composable("stock_detail") {
            val watchlistViewModel: WatchlistViewModel = hiltViewModel()
            val quote = pendingDetailQuote
            if (quote != null) {
                StockDetailScreen(
                    quote = quote,
                    onBack = { navController.popBackStack() },
                    onAddToWatchlist = { symbol ->
                        watchlistViewModel.addToWatchlist(symbol)
                    }
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
                    .padding(0.dp),
                color = AureumBg,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
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
                                Icon(
                                    screen.icon, 
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
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
                                selectedIconColor = GoldAccent,
                                selectedTextColor = GoldAccent,
                                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                unselectedTextColor = Color.White.copy(alpha = 0.4f),
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

