package kwiktwik.ratewatch.app.ui.watchlist

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kwiktwik.ratewatch.app.data.model.StockQuote
import kwiktwik.ratewatch.app.ui.home.LineChart
import kwiktwik.ratewatch.app.ui.components.StockDetailsSheet
import kwiktwik.ratewatch.app.ui.theme.*

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    onNavigateToSearch: () -> Unit
) {
    val quotes by viewModel.quotes.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedStock by viewModel.selectedStock.collectAsState()
    val isDetailsLoading by viewModel.isDetailsLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadWatchlistQuotes()
    }

    val categories = listOf("Indian Market", "Gold & Silver", "Global")

    Box(
        Modifier
            .fillMaxSize()
            .background(AureumBg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // 1. Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.AutoGraph, contentDescription = "Charts", tint = Color.White)
                }
                
                Text(
                    "Aureum",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AureumGold
                )
                
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. Category Chips
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            onClick = { viewModel.selectCategory(category) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AureumCard else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // 3. Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "TRACKED ASSETS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToSearch() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    Text(
                        "Add",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. Asset List
            if (isLoading && quotes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldAccent)
                }
            } else if (quotes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyWatchlist()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(quotes, key = { it.symbol }) { quote ->
                        WatchlistItem(
                            quote = quote,
                            onClick = { 
                                // Use searchId if available, otherwise symbol
                                quote.searchId?.let { viewModel.fetchDetails(it) }
                            },
                            onRemove = { viewModel.removeFromWatchlist(quote.symbol) }
                        )
                    }
                }
            }
        }

        if (isDetailsLoading) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldAccent)
            }
        }

        selectedStock?.let { stock ->
            StockDetailsSheet(
                quote = stock,
                onDismiss = { viewModel.clearDetails() },
                onAddToWatchlist = { viewModel.addToWatchlist(it) }
            )
        }
    }
}

@Composable
fun WatchlistItem(
    quote: StockQuote,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val isPositive = quote.changePercent >= 0
    val chartColor = if (isPositive) EmeraldGreen else RubyRed
    
    // Generate dummy sparkline data based on price and change
    val dummyData = remember(quote.symbol) {
        if (isPositive) {
            listOf(0.4f, 0.35f, 0.45f, 0.5f, 0.48f, 0.6f, 0.55f, 0.7f, 0.65f, 0.8f)
        } else {
            listOf(0.7f, 0.75f, 0.65f, 0.6f, 0.62f, 0.5f, 0.55f, 0.4f, 0.45f, 0.3f)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle (2x3 dots)
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(12.dp)
            ) {
                repeat(3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(2) {
                            Box(Modifier.size(3.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                        }
                    }
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Star icon
            Icon(
                Icons.Default.Star,
                contentDescription = "Favorite",
                tint = GoldAccent,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(Modifier.width(16.dp))
            
            // Name and Symbol
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    quote.symbol.take(8),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    quote.shortName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    maxLines = 1
                )
            }
            
            // Sparkline
            Box(modifier = Modifier.weight(1f).height(30.dp).padding(horizontal = 8.dp)) {
                LineChart(
                    data = dummyData,
                    modifier = Modifier.fillMaxSize(),
                    color = chartColor
                )
            }
            
            // Price and Change
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.3f)) {
                Text(
                    "₹${"%,.2f".format(quote.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = chartColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        "${if (isPositive) "+" else ""}${"%.2f".format(quote.changePercent)}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = chartColor
                    )
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White.copy(alpha = 0.35f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyWatchlist() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = AureumCard,
            modifier = Modifier.size(100.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Your watchlist is empty",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Add gold, silver or stocks to track them",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.4f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
