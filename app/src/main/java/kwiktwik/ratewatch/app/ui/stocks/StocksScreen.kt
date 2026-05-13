package kwiktwik.ratewatch.app.ui.stocks

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kwiktwik.ratewatch.app.data.model.StockQuote
import kwiktwik.ratewatch.app.ui.theme.GlassMorphism

@Composable
fun StocksScreen(
    viewModel: StocksViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStocks()
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
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.stocks),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(8.dp))
                LivePulseIndicator()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Live Indian Indices from Groww", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { viewModel.loadGlobalInstruments() }) {
                    Text("Show Global", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState.isLoading) {
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
                items(uiState.quotes) { quote ->
                    StockQuoteCard(
                        quote = quote, 
                        onAddToWatchlist = { viewModel.addToWatchlist(quote.symbol) }
                    )
                }
            }

            if (uiState.quotes.isEmpty() && !uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = GlassMorphism.surfaceColor(isSystemInDarkTheme()),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📈", style = MaterialTheme.typography.displaySmall)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            if (uiState.error != null) "Market data error" else "Market data unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            uiState.error ?: "Try refreshing in a few moments",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.loadStocks() },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
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
                color = androidx.compose.ui.graphics.Color(0xFF22C55E).copy(alpha = alpha),
                radius = size.minDimension / 2 * scale
            )
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF22C55E),
                radius = size.minDimension / 4
            )
        }
    }
}

@Composable
private fun StockQuoteCard(
    quote: StockQuote,
    onAddToWatchlist: () -> Unit
) {
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
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo or fallback indicator
            if (quote.logoUrl != null) {
                AsyncImage(
                    model = quote.logoUrl,
                    contentDescription = quote.shortName,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isPositive) androidx.compose.ui.graphics.Color(0xFF22C55E).copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color(0xFFEF4444).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isPositive) "↗" else "↘",
                        color = if (isPositive) androidx.compose.ui.graphics.Color(0xFF22C55E) else androidx.compose.ui.graphics.Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))

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
                    text = "${if (isPositive) "+" else ""}${quote.change} (${quote.changePercent}%)",
                    color = if (isPositive) androidx.compose.ui.graphics.Color(0xFF22C55E) else androidx.compose.ui.graphics.Color(0xFFEF4444),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                // Show day's range when rich Groww data is available
                if (quote.high != null && quote.low != null) {
                    Text(
                        text = "H: ${String.format("%.0f", quote.high)}  L: ${String.format("%.0f", quote.low)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                // Show 52-week range from updated Groww data (very useful for indices like NIFTY)
                if (quote.yearHigh != null && quote.yearLow != null) {
                    Text(
                        text = "52w: ${String.format("%.0f", quote.yearLow)} – ${String.format("%.0f", quote.yearHigh)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))
            
            IconButton(
                onClick = onAddToWatchlist,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add to watchlist", modifier = Modifier.size(20.dp))
            }
        }
    }
}

