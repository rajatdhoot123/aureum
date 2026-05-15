package kwiktwik.ratewatch.app.ui.markets

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kwiktwik.ratewatch.app.data.model.CityPrice
import kwiktwik.ratewatch.app.ui.theme.*

@Composable
fun MarketsScreen(viewModel: MarketsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Box(
        Modifier
            .fillMaxSize()
            .aureumBackground()
    ) {
        if (uiState.isLoading && uiState.currentCity == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldAccent)
            }
        } else if (uiState.error != null && uiState.currentCity == null) {
            ErrorState(
                message = uiState.error ?: "Something went wrong",
                onRetry = { viewModel.refresh() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header
                item {
                    Text(
                        "Gold Rates by City",
                        modifier = Modifier.padding(horizontal = 20.dp),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Search bar
                item {
                    CitySearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                }

                // Current location card
                uiState.currentCity?.let { city ->
                    item {
                        CurrentLocationCard(
                            city = city,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(32.dp))
                    }
                }

                // Major Cities header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Major Cities",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            onClick = { viewModel.toggleSortByPrice() },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent
                        ) {
                            Text(
                                "Sort by Price",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = if (uiState.sortByPrice) GoldAccent else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // City list
                items(uiState.filteredCities, key = { it.city }) { city ->
                    CityPriceCard(
                        city = city,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                if (uiState.filteredCities.isEmpty() && uiState.searchQuery.isNotBlank()) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No cities match \"${uiState.searchQuery}\"",
                                color = Color.White.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CitySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        color = SearchBarBg,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
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
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        "Search city...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp
                    ),
                    cursorBrush = SolidColor(GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentLocationCard(
    city: CityPrice,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "CURRENT LOCATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        city.city,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = GoldAccent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                // 24K column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "24K (10g)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatPrice(city.gold24kPer10g),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    ChangeIndicator(change = city.gold24kChange)
                }

                // Divider
                Box(
                    Modifier
                        .width(1.dp)
                        .height(70.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                Spacer(Modifier.width(24.dp))

                // 22K column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "22K (10g)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatPrice(city.gold22kPer10g),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    ChangeIndicator(change = city.gold22kChange)
                }
            }
        }
    }
}

@Composable
private fun CityPriceCard(
    city: CityPrice,
    modifier: Modifier = Modifier
) {
    val changeText = city.gold24kChange
    val isPositive = changeText != null && !changeText.startsWith("-")

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // City info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    city.city,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "22K: ${formatPrice(city.gold22kPer10g)}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color.White.copy(alpha = 0.45f)
                )
            }

            // Price and change
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatPrice(city.gold24kPer10g),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                if (changeText != null) {
                    Text(
                        "${if (isPositive) "\u2197" else "\u2198"}${changeText}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) EmeraldGreen else RubyRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangeIndicator(change: String?) {
    if (change == null) return
    val isPositive = !change.startsWith("-")
    val color = if (isPositive) EmeraldGreen else RubyRed

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            change,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .aureumBackground(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatPrice(price: Int?): String {
    if (price == null) return "---"
    return "\u20B9${"%,d".format(price)}"
}