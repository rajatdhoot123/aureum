package kwiktwik.ratewatch.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import kwiktwik.ratewatch.app.ui.theme.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailsSheet(
    quote: StockQuote,
    onDismiss: () -> Unit,
    onAddToWatchlist: (String) -> Unit
) {
    val isPositive = quote.changePercent >= 0
    val accentColor = if (isPositive) EmeraldGreen else RubyRed
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AureumSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!quote.logoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = quote.logoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                    
                    Column {
                        Text(
                            quote.shortName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            "${quote.symbol} • ${quote.exchange ?: "NSE"}${if (!quote.gsin.isNullOrEmpty()) " • ${quote.gsin}" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }
                
                Button(
                    onClick = { onAddToWatchlist(quote.symbol) },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Watch", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Price and Change
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "₹${"%,.2f".format(quote.price)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${if (isPositive) "+" else ""}${"%.2f".format(quote.change)} (${"%.2f".format(quote.changePercent)}%)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // Stats Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Open", "₹${"%,.0f".format(quote.open ?: 0.0)}", Modifier.weight(1f))
                StatCard("Prev. Close", "₹${"%,.0f".format(quote.previousClose ?: 0.0)}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Day High", "₹${"%,.0f".format(quote.high ?: 0.0)}", Modifier.weight(1f))
                StatCard("Day Low", "₹${"%,.0f".format(quote.low ?: 0.0)}", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("52W High", "₹${"%,.0f".format(quote.yearHigh ?: 0.0)}", Modifier.weight(1f))
                StatCard("52W Low", "₹${"%,.0f".format(quote.yearLow ?: 0.0)}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(40.dp))

            // Fundamentals
            if (quote.aum != null || quote.expenseRatio != null || quote.trackingError != null || quote.nav != null) {
                Text("Fundamentals", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AureumCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        quote.aum?.let { InfoRow("AUM", it) }
                        quote.expenseRatio?.let { InfoRow("Expense Ratio", it) }
                        quote.trackingError?.let { InfoRow("Tracking Error", it) }
                        quote.nav?.let { InfoRow("NAV", it) }
                    }
                }
                Spacer(Modifier.height(40.dp))
            }

            // Returns
            if (quote.return1M != null || quote.return6M != null || quote.return1Y != null) {
                Text("Returns", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    quote.return1M?.let { ReturnCard("1M", it, Modifier.weight(1f)) }
                    quote.return6M?.let { ReturnCard("6M", it, Modifier.weight(1f)) }
                    quote.return1Y?.let { ReturnCard("1Y", it, Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(40.dp))
            }

            // Investment Objective
            if (!quote.description.isNullOrEmpty()) {
                Text("Investment Objective", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AureumCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Text(
                        quote.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(40.dp))
            }

            // Peer Comparison
            if (!quote.peers.isNullOrEmpty()) {
                Text("Peer Comparison", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                quote.peers.forEach { peer ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = AureumCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(peer.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("ER: ${peer.expenseRatio}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReturnCard(label: String, value: String, modifier: Modifier = Modifier) {
    val isPositive = !value.startsWith("-")
    val color = if (isPositive) EmeraldGreen else RubyRed
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = AureumCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = AureumCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
