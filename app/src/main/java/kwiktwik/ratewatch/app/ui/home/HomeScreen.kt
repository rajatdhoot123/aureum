package kwiktwik.ratewatch.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kwiktwik.ratewatch.app.ui.components.PriceCard
import kwiktwik.ratewatch.app.ui.components.CitySelectorSheet
import kwiktwik.ratewatch.app.ui.theme.GlassMorphism
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    var showCitySheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
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
            Spacer(Modifier.height(24.dp))

            // Modern Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "RateWatch",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(Modifier.width(8.dp))
                        LivePulseIndicator()
                    }
                    Text(
                        androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Surface(
                    onClick = { viewModel.refresh() },
                    shape = RoundedCornerShape(16.dp),
                    color = GlassMorphism.surfaceColor(isSystemInDarkTheme()),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // City Selection Area
            Surface(
                onClick = { showCitySheet = true },
                shape = RoundedCornerShape(24.dp),
                color = GlassMorphism.surfaceColor(isSystemInDarkTheme()),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📍", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Location",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            selectedCity,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(
                        androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "UIState"
            ) { state ->
                when {
                    state.isLoading -> {
                        LoadingState()
                    }

                    state.error != null -> {
                        ErrorState(
                            message = state.error!!,
                            onRetry = { viewModel.refresh() }
                        )
                    }

                    else -> {
                        val cityData = state.prices.find { it.city == selectedCity }
                            ?: state.prices.firstOrNull()

                        cityData?.let { price ->
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                // Gold 24K Card
                                PriceCard(
                                    title = androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.gold_24k),
                                    price = price.gold24kPer10g ?: 0,
                                    unit = androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.per_10_grams),
                                    change = price.gold24kChange,
                                    iconEmoji = "🪙",
                                    accentColor = kwiktwik.ratewatch.app.ui.theme.GoldAccent
                                )

                                // Gold 22K Card
                                PriceCard(
                                    title = androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.gold_22k),
                                    price = price.gold22kPer10g ?: 0,
                                    unit = androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.per_10_grams),
                                    change = price.gold22kChange,
                                    iconEmoji = "🥇",
                                    accentColor = kwiktwik.ratewatch.app.ui.theme.GoldAccent.copy(alpha = 0.8f)
                                )

                                // Silver Card
                                PriceCard(
                                    title = androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.silver),
                                    price = price.silverPerKg ?: 0,
                                    unit = androidx.compose.ui.res.stringResource(kwiktwik.ratewatch.app.R.string.per_kg),
                                    change = price.silverChange,
                                    iconEmoji = "🥈",
                                    accentColor = kwiktwik.ratewatch.app.ui.theme.SilverAccent
                                )

                                Spacer(Modifier.height(8.dp))

                                // Last Updated & Source
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Updated: ${formatTime(price.scrapedAt)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (state.source.isNotEmpty()) {
                                            Text(
                                                "via ${state.source}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))


            Spacer(Modifier.height(100.dp)) // Extra space for navigation bar
        }
    }

    if (showCitySheet) {
        CitySelectorSheet(
            cities = uiState.prices.map { it.city },
            selectedCity = selectedCity,
            onCitySelected = {
                viewModel.setCity(it)
                showCitySheet = false
            },
            onDismiss = { showCitySheet = false }
        )
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
                color = Color(0xFF22C55E).copy(alpha = alpha),
                radius = size.minDimension / 2 * scale
            )
            drawCircle(
                color = Color(0xFF22C55E),
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

private fun formatTime(isoString: String): String {
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

