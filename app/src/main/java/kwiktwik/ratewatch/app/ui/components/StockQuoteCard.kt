package kwiktwik.ratewatch.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import kwiktwik.ratewatch.app.ui.theme.EmeraldGreen
import kwiktwik.ratewatch.app.ui.theme.GlassMorphism
import kwiktwik.ratewatch.app.ui.theme.RubyRed

@Composable
fun StockQuoteCard(
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
                        .background(if (isPositive) EmeraldGreen.copy(alpha = 0.1f) else RubyRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isPositive) "↗" else "↘",
                        color = if (isPositive) EmeraldGreen else RubyRed,
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
                    color = if (isPositive) EmeraldGreen else RubyRed,
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
