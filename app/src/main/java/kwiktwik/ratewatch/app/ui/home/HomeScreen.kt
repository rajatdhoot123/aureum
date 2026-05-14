package kwiktwik.ratewatch.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kwiktwik.ratewatch.app.ui.stocks.StocksViewModel
import kwiktwik.ratewatch.app.ui.stocks.StocksUiState
import kwiktwik.ratewatch.app.ui.theme.Cyan400
import kwiktwik.ratewatch.app.ui.theme.EmeraldGreen
import kwiktwik.ratewatch.app.ui.theme.GlassMorphism
import kwiktwik.ratewatch.app.ui.theme.GoldAccent
import kwiktwik.ratewatch.app.ui.theme.RubyRed
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val stocksViewModel: StocksViewModel = hiltViewModel()
    val stocksState by stocksViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
        stocksViewModel.loadStocks()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(GlassMorphism.backgroundBrush(isSystemInDarkTheme()))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Aureum Header matching design brief
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Aureum",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    LivePulseIndicator()
                }
                // User avatar
                Surface(
                    shape = RoundedCornerShape(50),
                    color = GoldAccent,
                    modifier = Modifier.size(42.dp).clickable { /* open profile */ }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "R",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF031427)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Greeting - local context
            val greeting = getTimeBasedGreeting()
            Text(
                "$greeting, Rajat",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF1F5F9)
            )

            Spacer(Modifier.height(16.dp))

            // Top tabs visual (Overview | Portfolio | Alerts | News) - Overview active
            TabChips(selectedTab = 0)

            Spacer(Modifier.height(20.dp))

            // Market Overview - 4 asset cards (Gold, Silver, NIFTY, SENSEX)
            Text(
                "Market Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))

            MarketOverviewSection(uiState = uiState, stocksState = stocksState)

            Spacer(Modifier.height(28.dp))

            // Savings Summary - Portfolio snapshot (from brief)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Savings Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "View all",
                    style = MaterialTheme.typography.labelMedium,
                    color = Cyan400,
                    modifier = Modifier.clickable { /* navigate to Portfolio */ }
                )
            }
            Spacer(Modifier.height(12.dp))

            SavingsSummarySection()

            Spacer(Modifier.height(100.dp)) // Extra space for navigation bar
        }
    }
}

@Composable
fun LivePulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(16.dp)) {
            drawCircle(
                color = EmeraldGreen.copy(alpha = alpha),
                radius = size.minDimension / 2 * scale
            )
            drawCircle(
                color = EmeraldGreen,
                radius = size.minDimension / 4
            )
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Tracking market rates...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun EmptyMetalsState(onForceScrape: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📭", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(12.dp))
            Text(
                "No gold/silver data yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "The scraper hasn't fetched fresh rates from Goodreturns.in.\nTrigger a manual scrape to load the latest prices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onForceScrape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Force Scrape Now")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Rate limit: once every 5 minutes",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚠️", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(16.dp))
            Text(
                message, 
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.retry))
            }
        }
    }
}

private fun formatTime(isoString: String?): String {
    if (isoString == null) return "--"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(isoString.substringBefore("."))
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        isoString.take(16)
    }
}

@Composable
fun getTimeBasedGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
fun TabChips(selectedTab: Int) {
    val tabs = listOf("Overview", "Portfolio", "Alerts", "News")
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = index == selectedTab
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else GlassMorphism.surfaceColor(isSystemInDarkTheme()),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else GlassMorphism.strokeColor(isSystemInDarkTheme())
                ),
                modifier = Modifier.clickable { /* TODO: switch tab or navigate */ }
            ) {
                Text(
                    tab,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MarketOverviewSection(uiState: HomeUiState, stocksState: StocksUiState) {
    val isDark = isSystemInDarkTheme()
    // Extract live gold/silver (use first city or India approx)
    val price = uiState.prices.firstOrNull()
    val goldPrice = price?.gold24kPer10g ?: 7812.0
    val goldChange = price?.gold24kChange ?: "+0.8%"
    val silverPrice = price?.silverPerKg ?: 91200.0
    val silverChange = price?.silverChange ?: "+1.2%"

    // Find NIFTY and SENSEX from stocks
    val nifty = stocksState.quotes.firstOrNull { it.symbol.contains("NIFTY", true) || it.shortName.contains("Nifty", true) }
    val sensex = stocksState.quotes.firstOrNull { it.symbol.contains("SENSEX", true) || it.shortName.contains("Sensex", true) || it.shortName.contains("BSE", true) }

    val niftyPrice = nifty?.price ?: 24823.15
    val niftyPct = nifty?.changePercent ?: -0.3
    val niftyChange = if (niftyPct >= 0) "+${"%.1f".format(niftyPct)}%" else "${"%.1f".format(niftyPct)}%"
    val sensexPrice = sensex?.price ?: 81785.67
    val sensexPct = sensex?.changePercent ?: -0.2
    val sensexChange = if (sensexPct >= 0) "+${"%.1f".format(sensexPct)}%" else "${"%.1f".format(sensexPct)}%"

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            MarketAssetCard(
                name = "Gold 24K",
                price = "₹${"%,.0f".format(goldPrice)}",
                unit = "/10g",
                change = goldChange,
                icon = "🪙",
                isPositive = goldChange.startsWith("+", ignoreCase = true)
            )
        }
        item {
            MarketAssetCard(
                name = "Silver",
                price = "₹${"%,.0f".format(silverPrice)}",
                unit = "/kg",
                change = silverChange,
                icon = "🥈",
                isPositive = silverChange.startsWith("+", ignoreCase = true)
            )
        }
        item {
            MarketAssetCard(
                name = "NIFTY 50",
                price = "₹${"%,.0f".format(niftyPrice)}",
                unit = "",
                change = niftyChange,
                icon = "📈",
                isPositive = niftyChange.startsWith("+", ignoreCase = true)
            )
        }
        item {
            MarketAssetCard(
                name = "SENSEX",
                price = "₹${"%,.0f".format(sensexPrice)}",
                unit = "",
                change = sensexChange,
                icon = "📊",
                isPositive = sensexChange.startsWith("+", ignoreCase = true)
            )
        }
    }
}

@Composable
fun MarketAssetCard(
    name: String,
    price: String,
    unit: String,
    change: String,
    icon: String,
    isPositive: Boolean
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GlassMorphism.surfaceColor(isSystemInDarkTheme()),
        border = BorderStroke(1.dp, GlassMorphism.strokeColor(isSystemInDarkTheme())),
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(6.dp))
                Text(
                    name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB0B8C4)
                )
            }
            Text(
                price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (unit.isNotEmpty()) {
                Text(unit, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8A96A6))
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = (if (isPositive) EmeraldGreen else RubyRed).copy(alpha = 0.12f)
            ) {
                Text(
                    "${if (isPositive) "↑" else "↓"} $change",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPositive) EmeraldGreen else RubyRed
                )
            }
        }
    }
}

@Composable
fun SavingsSummarySection() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            PortfolioSummaryCard(
                name = "Physical Gold",
                value = "₹45,200",
                pnl = "+₹2,100 (4.8%)",
                isPositive = true,
                icon = "🪙"
            )
        }
        item {
            PortfolioSummaryCard(
                name = "Global Equities",
                value = "₹1,24,500",
                pnl = "+₹8,750 (7.5%)",
                isPositive = true,
                icon = "🌍"
            )
        }
        item {
            PortfolioSummaryCard(
                name = "Cash Reserves",
                value = "₹28,000",
                pnl = "—",
                isPositive = true,
                icon = "💵"
            )
        }
    }
}

@Composable
fun PortfolioSummaryCard(
    name: String,
    value: String,
    pnl: String,
    isPositive: Boolean,
    icon: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GlassMorphism.surfaceColor(isSystemInDarkTheme()),
        border = BorderStroke(1.dp, GlassMorphism.strokeColor(isSystemInDarkTheme())),
        modifier = Modifier.width(160.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFB0B8C4)
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                pnl,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (pnl.contains("+") || pnl == "—") EmeraldGreen else RubyRed
            )
        }
    }
}

