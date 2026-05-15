package kwiktwik.ratewatch.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kwiktwik.ratewatch.app.data.model.*
import kwiktwik.ratewatch.app.data.remote.GrowwMarketCategory
import kwiktwik.ratewatch.app.data.remote.GrowwMarketIndex
import kwiktwik.ratewatch.app.ui.stocks.StocksViewModel
import kwiktwik.ratewatch.app.ui.stocks.StocksUiState
import kwiktwik.ratewatch.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToDetail: (StockQuote) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val stocksViewModel: StocksViewModel = hiltViewModel()
    val stocksState by stocksViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
        stocksViewModel.loadStocks()
        stocksViewModel.loadLatestIndices()
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToDetail.collect { quote ->
            onNavigateToDetail(quote)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .aureumBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // 1. Sonar Header
            AureumHeader()

            Spacer(Modifier.height(24.dp))

            // 2. Search Bar
            SearchBar(onClick = onNavigateToSearch)

            Spacer(Modifier.height(24.dp))

            // 3. Quick Actions
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionCard(
                    title = "Check Price",
                    icon = Icons.Outlined.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Set Alert",
                    icon = Icons.Outlined.NotificationsActive,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // 4. Current Market Prices
            Text(
                "Current Market Prices",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))

            // Gold Card (Large)
            GoldPriceCard(uiState)

            Spacer(Modifier.height(16.dp))

            // Silver Card (Large)
            SilverPriceCard(uiState)

            Spacer(Modifier.height(16.dp))

            // NIFTY & SENSEX Row
            IndicesRow(stocksState)

            Spacer(Modifier.height(32.dp))

            // 5. Market Insights (Groww Categories)
            MarketInsightsSection(
                uiState = uiState,
                onCategorySelected = { viewModel.selectCategory(it) },
                onIndexSelected = { viewModel.selectIndex(it) },
                onStockClick = { stock -> viewModel.fetchStockDetails(stock) }
            )

            Spacer(Modifier.height(120.dp)) // Padding for bottom nav
        }

        // Loading overlay while fetching stock details
        if (uiState.isDetailsLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = GoldAccent)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading details...", color = Color.White.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun AureumHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* TODO: Menu */ }) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
        }
        
        Text(
            "SONAR",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AureumGold,
            letterSpacing = 1.sp
        )

        // User avatar - Using the generated image if possible, fallback to icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                .clickable { /* TODO: Profile */ }
        ) {
            AsyncImage(
                model = kwiktwik.ratewatch.app.R.drawable.user_profile,
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun SearchBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = SearchBarBg,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search, 
                contentDescription = "Search", 
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Search gold, silver, or stocks...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun QuickActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                Icon(
                    icon, 
                    contentDescription = title, 
                    tint = GoldAccent, 
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun GoldPriceCard(uiState: HomeUiState) {
    val price = uiState.prices.firstOrNull()?.gold24kPer10g?.toDouble()
    val change = uiState.prices.firstOrNull()?.gold24kChange
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.05f)
                    ) {
                        Text(
                            "GOLD (24K)",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AureumGold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "per 10g",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (price != null) "₹${"%,.0f".format(price)}" else "₹---",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 38.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    if (change != null) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = EmeraldGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    change,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = AureumGold, modifier = Modifier.size(28.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Bar Chart
            BarChart(
                data = listOf(0.2f, 0.3f, 0.5f, 0.4f, 0.6f, 0.5f, 0.8f, 0.7f, 0.9f, 0.8f),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )
        }
    }
}

@Composable
fun SilverPriceCard(uiState: HomeUiState) {
    val price = uiState.prices.firstOrNull()?.silverPerKg?.toDouble()
    val change = uiState.prices.firstOrNull()?.silverChange

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.05f)
                    ) {
                        Text(
                            "SILVER",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SilverAccent
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "per 1kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (price != null) "₹${"%,.0f".format(price)}" else "₹---",
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 38.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    if (change != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                change,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Line Chart
            LineChart(
                data = listOf(0.1f, 0.2f, 0.15f, 0.3f, 0.25f, 0.4f, 0.35f, 0.5f, 0.45f, 0.6f),
                modifier = Modifier.fillMaxWidth().height(60.dp),
                color = ChartGreen
            )
        }
    }
}

@Composable
fun IndicesRow(stocksState: StocksUiState) {
    val indices = stocksState.latestIndices
    
    if (stocksState.isLoading && indices.isEmpty()) {
        // Loading state: 2x2 skeleton grid
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(145.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(AureumCard.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }
    } else if (indices.isNotEmpty()) {
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .height(306.dp), // (145.dp * 2) + 16.dp spacing
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 20.dp)
        ) {
            items(indices) { index ->
                IndexCard(
                    quote = index,
                    modifier = Modifier.width(160.dp)
                )
            }
        }
    }
}

@Composable
fun IndexCard(quote: StockQuote, modifier: Modifier = Modifier) {
    val isPositive = quote.changePercent >= 0
    val change = "${if (isPositive) "+" else ""}${"%.2f".format(quote.changePercent)}%"
    
    Surface(
        modifier = modifier.height(145.dp), // Increased height for more data
        shape = RoundedCornerShape(24.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (!quote.logoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = quote.logoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        quote.shortName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (isPositive) EmeraldGreen else RubyRed).copy(alpha = 0.1f)
                ) {
                    Text(
                        "${if (isPositive) "↑" else "↓"} $change",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) EmeraldGreen else RubyRed
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Text(
                "₹${"%,.2f".format(quote.price)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            
            Spacer(Modifier.weight(1f))
            
            // Show all available data: High and Low
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("L: ₹${"%,.0f".format(quote.low ?: 0.0)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("H: ₹${"%,.0f".format(quote.high ?: 0.0)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            // Minimal visual indicator instead of hardcoded chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                val range = (quote.high ?: 0.0) - (quote.low ?: 0.0)
                if (range > 0) {
                    val position = ((quote.price - (quote.low ?: 0.0)) / range).coerceIn(0.0, 1.0).toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(position)
                            .fillMaxHeight()
                            .background(if (isPositive) EmeraldGreen else RubyRed)
                    )
                }
            }
        }
    }
}

// Keep the old one for empty state if needed, or update empty state to use a placeholder
@Composable
fun IndexCard(name: String, price: Double?, change: String, isPositive: Boolean = true, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(130.dp),
        shape = RoundedCornerShape(24.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (isPositive) EmeraldGreen else RubyRed).copy(alpha = 0.1f)
                ) {
                    Text(
                        "${if (isPositive) "↑" else "↓"} $change",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) EmeraldGreen else RubyRed
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (price != null) "%,.2f".format(price) else "---",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            LineChart(
                data = if (isPositive) listOf(0.4f, 0.3f, 0.5f, 0.45f, 0.6f) else listOf(0.6f, 0.5f, 0.4f, 0.45f, 0.3f),
                modifier = Modifier.fillMaxWidth().height(24.dp),
                color = if (isPositive) EmeraldGreen else RubyRed
            )
        }
    }
}

@Composable
fun MarketInsightsSection(
    uiState: HomeUiState,
    onCategorySelected: (GrowwMarketCategory) -> Unit,
    onIndexSelected: (GrowwMarketIndex) -> Unit,
    onStockClick: (StockQuote) -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Market Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // Index selector
            uiState.marketCategories?.indices?.let { indices ->
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Surface(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                uiState.selectedIndex?.name ?: "NIFTY 50",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccent
                            )
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(AureumCard).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        indices.forEach { index ->
                            DropdownMenuItem(
                                text = { Text(index.name, color = Color.White) },
                                onClick = {
                                    onIndexSelected(index)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Category selection tabs
        uiState.marketCategories?.sections?.let { sections ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sections) { category ->
                    val isSelected = uiState.selectedCategory?.id == category.id
                    Surface(
                        onClick = { onCategorySelected(category) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            category.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Market data list
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = AureumCard,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (uiState.isMarketLoading) {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldAccent)
                    }
                } else if (uiState.marketStocks.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No data available", color = Color.White.copy(alpha = 0.5f))
                    }
                } else {
                    uiState.marketStocks.take(6).forEachIndexed { index, stock ->
                        StockItem(
                            symbol = stock.symbol.take(4),
                            name = stock.shortName,
                            sector = stock.exchange ?: "NSE",
                            price = stock.price,
                            change = "${if (stock.change >= 0) "+" else ""}${"%.2f".format(stock.changePercent)}%",
                            isPositive = stock.change >= 0,
                            logoUrl = stock.logoUrl,
                            onClick = { onStockClick(stock) }
                        )
                        if (index < uiState.marketStocks.take(6).size - 1) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopPerformersSection(stocksState: StocksUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Top Performers",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            StockItem(
                symbol = "RELI",
                name = "Reliance Ind.",
                sector = "Energy",
                price = 2954.20,
                change = "+1.85%",
                isPositive = true
            )
            
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 16.dp))
            
            StockItem(
                symbol = "TCS",
                name = "TCS Ltd.",
                sector = "Technology",
                price = 3980.50,
                change = "+1.20%",
                isPositive = true
            )
        }
    }
}

@Composable
fun StockItem(symbol: String, name: String, sector: String, price: Double, change: String, isPositive: Boolean, logoUrl: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!logoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        symbol,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                sector,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "₹${"%,.2f".format(price)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                change,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isPositive) EmeraldGreen else RubyRed
            )
        }
    }
}

@Composable
fun BarChart(data: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val spacing = size.width / (data.size * 1.5f)
        val barWidth = spacing * 0.8f
        
        data.forEachIndexed { index, value ->
            val barHeight = size.height * value
            drawRoundRect(
                color = ChartGreen,
                topLeft = Offset(index * spacing * 1.5f, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
fun LineChart(data: List<Float>, modifier: Modifier = Modifier, color: Color = ChartGreen) {
    Canvas(modifier = modifier) {
        val path = Path()
        val spacing = size.width / (data.size - 1)
        
        data.forEachIndexed { index, value ->
            val x = index * spacing
            val y = size.height - (size.height * value)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Gradient fill below path
        val fillPath = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.2f), Color.Transparent)
            )
        )
    }
}
