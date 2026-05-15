package kwiktwik.ratewatch.app.ui.stockdetail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import kwiktwik.ratewatch.app.data.model.*
import kwiktwik.ratewatch.app.ui.theme.*
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    quote: StockQuote,
    onBack: () -> Unit,
    onAddToWatchlist: (String) -> Unit
) {
    val isPositive = quote.changePercent >= 0
    val accentColor = if (isPositive) EmeraldGreen else RubyRed

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Aureum", color = Color.White, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AureumBg)
            )
        },
        containerColor = AureumBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Header: Logo + Badges + Name + Symbol
            item { StockHeader(quote) }

            // 2. Action Row: Set Alert, Star, Invest
            item { ActionRow(onAddToWatchlist = { onAddToWatchlist(quote.symbol) }) }

            // 3. Price Section
            item { PriceSection(quote, accentColor, isPositive) }

            // 4. Chart Placeholder + Time Period Selector
            item { ChartSection(accentColor) }

            // 5. Fundamentals (ETF-style or Stock-style)
            item { FundamentalsSection(quote) }

            // 6. Returns Section
            if (quote.return1M != null || quote.return6M != null || quote.return1Y != null) {
                item { ReturnsSection(quote) }
            }

            // 7. Investment Objective / About
            if (!quote.description.isNullOrEmpty() || !quote.businessSummary.isNullOrEmpty()) {
                item { InvestmentObjectiveSection(quote) }
            }

            // 8. Peer Comparison
            if (!quote.peers.isNullOrEmpty() || !quote.stockPeers.isNullOrEmpty()) {
                item { PeerComparisonSection(quote) }
            }

            // 9. Portfolio Insights
            item { PortfolioInsightsSection(quote) }
        }
    }
}

// ======== Section Composables ========

@Composable
private fun StockHeader(quote: StockQuote) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            // Logo
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                    .background(AureumCard).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!quote.logoUrl.isNullOrEmpty()) {
                    AsyncImage(model = quote.logoUrl, contentDescription = null,
                        modifier = Modifier.size(40.dp), contentScale = ContentScale.Fit)
                } else {
                    Text(quote.shortName.take(1), color = GoldAccent,
                        fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                // Badges row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val type = quote.instrumentType ?: quote.cappedType
                    if (!type.isNullOrEmpty()) {
                        TypeBadge(type)
                    }
                    if (!quote.exchange.isNullOrEmpty()) {
                        TypeBadge(quote.exchange!!)
                    }
                    if (quote.isFnoEnabled) {
                        TypeBadge("F&O")
                    }
                }
                Spacer(Modifier.height(6.dp))
                // Name
                Text(quote.fullName ?: quote.shortName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold, color = Color.White,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                // Symbol code
                Text(quote.symbol.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.4f),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp)
            }
        }
    }
}

@Composable
private fun ActionRow(onAddToWatchlist: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set Alert
        OutlinedButton(
            onClick = { },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(Icons.Outlined.Notifications, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Set Alert", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        // Favorite Star
        Box(
            modifier = Modifier.size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .clickable { onAddToWatchlist() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.StarOutline, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.weight(1f))
        // Invest Button
        Button(
            onClick = { },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
        ) {
            Text("Invest", color = Color.Black, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun PriceSection(quote: StockQuote, accentColor: Color, isPositive: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp)) {
        HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
        Spacer(Modifier.height(20.dp))
        Text("₹${"%,.2f".format(quote.price)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "${if (isPositive) "+" else ""}${"%.2f".format(quote.changePercent)}% (₹${"%.2f".format(quote.change)})",
                color = accentColor, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ChartSection(accentColor: Color) {
    var selectedPeriod by remember { mutableStateOf("1M") }
    val periods = listOf("1D", "1W", "1M", "1Y", "ALL")

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        // Chart placeholder with fake line
        Box(
            modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 20.dp)
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val points = 60
                    val path = Path()
                    for (i in 0..points) {
                        val x = w * i / points
                        val y = h * 0.5f - (h * 0.25f * sin(i * 0.15 + 1.0).toFloat()) +
                                (h * 0.08f * sin(i * 0.4).toFloat())
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color = accentColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                    // Gradient fill
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(fillPath, brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = 0f, endY = h
                    ))
                    // Current price dot at the end
                    val lastX = w
                    val lastY = h * 0.5f - (h * 0.25f * sin(points * 0.15 + 1.0).toFloat()) +
                            (h * 0.08f * sin(points * 0.4).toFloat())
                    drawCircle(color = accentColor, radius = 5f, center = Offset(lastX, lastY))
                    drawCircle(color = accentColor.copy(alpha = 0.3f), radius = 10f, center = Offset(lastX, lastY))
                }
        )

        Spacer(Modifier.height(16.dp))

        // Time period selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            periods.forEach { period ->
                val isSelected = period == selectedPeriod
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .then(
                            if (isSelected) Modifier.background(Color.White.copy(alpha = 0.12f))
                            else Modifier
                        )
                        .clickable { selectedPeriod = period }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        period,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FundamentalsSection(quote: StockQuote) {
    val hasEtfFundamentals = quote.aum != null || quote.expenseRatio != null ||
            quote.trackingError != null || quote.nav != null
    val hasStockFundamentals = !quote.fundamentals.isNullOrEmpty()

    if (!hasEtfFundamentals && !hasStockFundamentals) return

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Fundamentals", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(16.dp))

                if (hasEtfFundamentals) {
                    quote.aum?.let { FundamentalRow("AUM", it) }
                    quote.expenseRatio?.let { FundamentalRow("Expense Ratio", it) }
                    quote.trackingError?.let { FundamentalRow("Tracking Error", it) }
                    quote.nav?.let { FundamentalRow("NAV", "₹$it") }
                }

                if (hasStockFundamentals) {
                    quote.fundamentals!!.forEach { f ->
                        FundamentalRow(f.name, f.value)
                    }
                }

                // Additional stock metrics
                quote.peRatio?.let { FundamentalRow("P/E Ratio", "%.2f".format(it)) }
                quote.pbRatio?.let { FundamentalRow("P/B Ratio", "%.2f".format(it)) }
                quote.marketCap?.let { FundamentalRow("Market Cap", formatMcap(it)) }
                quote.divYield?.let { FundamentalRow("Dividend Yield", "%.2f%%".format(it)) }
            }
        }
    }
}

@Composable
private fun ReturnsSection(quote: StockQuote) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Returns", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    quote.return1M?.let { ReturnChip("1M", it) }
                    quote.return6M?.let { ReturnChip("6M", it) }
                    quote.return1Y?.let { ReturnChip("1Y", it) }
                }
                Spacer(Modifier.height(16.dp))
                // Performance badge
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(EmeraldGreen.copy(alpha = 0.12f))
                        .border(1.dp, EmeraldGreen.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("Consistent performance against Benchmark.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium, color = EmeraldGreen,
                        fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun InvestmentObjectiveSection(quote: StockQuote) {
    val text = quote.description ?: quote.businessSummary ?: return
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Investment Objective", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(12.dp))
                var expanded by remember { mutableStateOf(false) }
                Text(text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 22.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize()
                )
                if (text.length > 200) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (expanded) "Show less" else "Read more",
                        color = GoldAccent, style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeerComparisonSection(quote: StockQuote) {
    val simplePeers = quote.peers
    val stockPeers = quote.stockPeers

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Peer Comparison", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = Color.White,
                fontFamily = FontFamily.Monospace)
            Icon(Icons.Outlined.Info, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(12.dp))

        // ETF peers (simple)
        simplePeers?.take(3)?.forEach { peer ->
            EtfPeerCard(peer)
            Spacer(Modifier.height(8.dp))
        }

        // Stock peers (rich)
        stockPeers?.take(3)?.forEach { peer ->
            StockPeerCard(peer)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))
        // View All Peers Button
        OutlinedButton(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent)
        ) {
            Text("VIEW ALL PEERS", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun PortfolioInsightsSection(quote: StockQuote) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Portfolio Insights", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(10.dp))
                val name = quote.shortName
                Text(
                    "${name} provides a strong hedge against market volatility. Add ${quote.symbol.uppercase()} to your recurring monthly investments.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f),
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(12.dp))
                Text("Setup SIP →", color = GoldAccent, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { })
            }
        }
    }
}

// ======== Sub-components ========

@Composable
private fun TypeBadge(text: String) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = AureumCard, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) {
        content()
    }
}

@Composable
private fun FundamentalRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f))
        Text(value, style = MaterialTheme.typography.bodyMedium,
            color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReturnChip(label: String, value: String) {
    val isPositive = !value.trimStart().startsWith("-")
    val color = if (isPositive) EmeraldGreen else RubyRed
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
private fun EtfPeerCard(peer: PeerInfo) {
    SectionCard {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(peer.name.take(1), color = GoldAccent, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(peer.name, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("ER:  ${peer.expenseRatio}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f))
            }
            Icon(Icons.Default.ChevronRight, null,
                tint = Color.White.copy(alpha = 0.25f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun StockPeerCard(peer: StockPeerInfo) {
    val isPos = (peer.dayChangePerc ?: 0.0) >= 0
    val color = if (isPos) EmeraldGreen else RubyRed
    SectionCard {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (!peer.logoUrl.isNullOrEmpty()) {
                    AsyncImage(model = peer.logoUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else {
                    Text(peer.shortName.take(1), color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(peer.displayName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (peer.peRatio != null) Text("P/E: ${"%.1f".format(peer.peRatio)}",
                        style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
                    if (peer.marketCap != null) Text("MCap: ${formatMcap(peer.marketCap)}",
                        style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (peer.ltp != null) Text("₹${"%,.1f".format(peer.ltp)}",
                    fontWeight = FontWeight.ExtraBold, color = Color.White,
                    style = MaterialTheme.typography.bodyMedium)
                if (peer.dayChangePerc != null) Text(
                    "${if (isPos) "+" else ""}${"%.2f".format(peer.dayChangePerc)}%",
                    style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun formatMcap(v: Double?): String {
    if (v == null) return "—"
    return when {
        v >= 100_000 -> "₹${"%,.0f".format(v / 100_000)}L Cr"
        else -> "₹${"%,.0f".format(v)} Cr"
    }
}
