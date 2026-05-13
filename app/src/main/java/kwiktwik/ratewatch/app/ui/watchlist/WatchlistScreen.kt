package kwiktwik.ratewatch.app.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import kwiktwik.ratewatch.app.ui.theme.GlassMorphism
import kwiktwik.ratewatch.app.data.model.StockQuote

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel
) {
    val watchlistSymbols by viewModel.watchlistSymbols.collectAsState()
    val quotes by viewModel.quotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(watchlistSymbols) {
        viewModel.loadWatchlistQuotes()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(GlassMorphism.backgroundBrush(isSystemInDarkTheme()))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            
            Text(
                text = androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.watchlist),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Your pinned stocks & indices", 
                style = MaterialTheme.typography.bodyMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            if (watchlistSymbols.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyWatchlist()
                }
            } else {
                if (isLoading) {
                    LinearProgressIndicator(
                        Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(quotes) { quote ->
                        WatchlistItem(
                            quote = quote,
                            onRemove = { viewModel.removeFromWatchlist(quote.symbol) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWatchlist() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GlassMorphism.surfaceColor(isSystemInDarkTheme()),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("⭐", style = MaterialTheme.typography.displaySmall)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(
            androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.empty_watchlist),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.add_symbols),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun WatchlistItem(quote: StockQuote, onRemove: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val isPositive = quote.change >= 0
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = GlassMorphism.surfaceColor(isDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            GlassMorphism.strokeColor(isDark)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    quote.shortName, 
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    quote.symbol, 
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${String.format("%.2f", quote.price)}", 
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "${if (isPositive) "+" else ""}${quote.changePercent}%",
                    color = if (isPositive) androidx.compose.ui.graphics.Color(0xFF22C55E) else androidx.compose.ui.graphics.Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            IconButton(
                onClick = onRemove,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Remove",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

