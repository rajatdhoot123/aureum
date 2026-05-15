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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import kwiktwik.ratewatch.app.data.model.*
import kwiktwik.ratewatch.app.ui.theme.*

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
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onAddToWatchlist(quote.symbol) }) {
                        Icon(Icons.Outlined.StarOutline, null, tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AureumBg)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .aureumBackground(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Header: Logo + Badges + Name + Symbol
            item { StockHeader(quote) }

            // 2. Price Section
            item { PriceSection(quote, accentColor, isPositive) }

            // 3. Returns Bar Chart (only available periods)
            item { ReturnsChartSection(quote) }

            // 4. Fundamentals
            item { FundamentalsSection(quote) }

            // 5. Today's Stats (OHLC)
            item { TodaysStatsSection(quote) }

            // 6. 52-Week Range
            if (quote.yearHigh != null && quote.yearLow != null) {
                item { PriceRangeSection(quote) }
            }

            // 7. Returns Detail
            item { ReturnsDetailSection(quote) }

            // 8. Investment Objective / About
            if (!quote.description.isNullOrEmpty() || !quote.businessSummary.isNullOrEmpty()) {
                item { InvestmentObjectiveSection(quote) }
            }

            // 9. Fund Info (ETF-specific)
            if (quote.amc != null || !quote.fundManagers.isNullOrEmpty()) {
                item { FundInfoSection(quote) }
            }

            // 10. Peer Comparison
            if (!quote.peers.isNullOrEmpty() || !quote.stockPeers.isNullOrEmpty()) {
                item { PeerComparisonSection(quote) }
            }
        }
    }
}

// ======== Section Composables ========

@Composable
private fun StockHeader(quote: StockQuote) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 4.dp)) {
        Row(verticalAlignment = Alignment.Top) {
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
            Spacer(Modifier.width(14.dp))
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val type = quote.instrumentType ?: quote.cappedType
                    if (!type.isNullOrEmpty()) TypeBadge(type)
                    if (!quote.exchange.isNullOrEmpty()) TypeBadge(quote.exchange!!)
                    if (quote.isFnoEnabled) TypeBadge("F&O")
                }
                Spacer(Modifier.height(6.dp))
                Text(quote.fullName ?: quote.shortName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold, color = Color.White,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(quote.nseScriptCode ?: quote.bseScriptCode ?: quote.symbol.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun PriceSection(quote: StockQuote, accentColor: Color, isPositive: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("₹${"%,.2f".format(quote.price)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                null, tint = accentColor, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "${if (isPositive) "+" else ""}${"%.2f".format(quote.changePercent)}% (₹${"%.2f".format(quote.change)})",
                color = accentColor, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ReturnsChartSection(quote: StockQuote) {
    // Build list of available return entries only
    val entries = buildList {
        quote.return1M?.let { add("1M" to parseReturnValue(it)) }
        quote.return3M?.let { add("3M" to parseReturnValue(it)) }
        quote.return6M?.let { add("6M" to parseReturnValue(it)) }
        quote.return1Y?.let { add("1Y" to parseReturnValue(it)) }
        quote.returnAll?.let { add("ALL" to parseReturnValue(it)) }
    }
    if (entries.isEmpty()) return

    val maxVal = entries.maxOf { kotlin.math.abs(it.second) }.coerceAtLeast(1.0)

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Returns", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(20.dp))

                // Bar chart
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                        .drawBehind {
                            val barCount = entries.size
                            val gap = 14.dp.toPx()
                            val totalGaps = gap * (barCount - 1)
                            val barW = (size.width - totalGaps) / barCount
                            val chartH = size.height - 28.dp.toPx() // leave space for labels

                            entries.forEachIndexed { i, (_, value) ->
                                val x = i * (barW + gap)
                                val frac = (kotlin.math.abs(value) / maxVal).toFloat().coerceIn(0f, 1f)
                                val barH = chartH * frac * 0.85f
                                val color = if (value >= 0) EmeraldGreen else RubyRed
                                val y = chartH - barH

                                drawRoundRect(
                                    color = color.copy(alpha = 0.2f),
                                    topLeft = Offset(x, y),
                                    size = Size(barW, barH),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(x, y),
                                    size = Size(barW, barH),
                                    cornerRadius = CornerRadius(6.dp.toPx()),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                                )
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.1f)),
                                        startY = y, endY = y + barH
                                    ),
                                    topLeft = Offset(x, y),
                                    size = Size(barW, barH),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                            }
                        }
                )

                // Labels row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    entries.forEach { (label, value) ->
                        val color = if (value >= 0) EmeraldGreen else RubyRed
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${"%.1f".format(value)}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold, color = color)
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.5f))
                        }
                    }
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
    val hasStockMetrics = quote.peRatio != null || quote.pbRatio != null ||
            quote.marketCap != null || quote.divYield != null

    if (!hasEtfFundamentals && !hasStockFundamentals && !hasStockMetrics) return

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Fundamentals", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(12.dp))

                if (hasEtfFundamentals) {
                    quote.aum?.let { FundamentalRow("AUM", it) }
                    quote.expenseRatio?.let { FundamentalRow("Expense Ratio", it) }
                    quote.trackingError?.let { FundamentalRow("Tracking Error", it) }
                    quote.nav?.let { FundamentalRow("NAV", it) }
                }

                if (hasStockFundamentals) {
                    quote.fundamentals!!.forEach { f -> FundamentalRow(f.name, f.value) }
                }

                quote.peRatio?.let { FundamentalRow("P/E Ratio", "%.2f".format(it)) }
                quote.pbRatio?.let { FundamentalRow("P/B Ratio", "%.2f".format(it)) }
                quote.marketCap?.let { FundamentalRow("Market Cap", formatMcap(it)) }
                quote.divYield?.let { FundamentalRow("Dividend Yield", "%.2f%%".format(it)) }
            }
        }
    }
}

@Composable
private fun TodaysStatsSection(quote: StockQuote) {
    val hasStats = quote.open != null || quote.high != null || quote.low != null ||
            quote.previousClose != null || quote.volume != null

    if (!hasStats) return

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Today's Stats", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip("Open", quote.open?.let { "₹${"%,.2f".format(it)}" } ?: "—", Modifier.weight(1f))
                    StatChip("Prev Close", quote.previousClose?.let { "₹${"%,.2f".format(it)}" } ?: "—", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip("Day High", quote.high?.let { "₹${"%,.2f".format(it)}" } ?: "—", Modifier.weight(1f))
                    StatChip("Day Low", quote.low?.let { "₹${"%,.2f".format(it)}" } ?: "—", Modifier.weight(1f))
                }
                if (quote.volume != null) {
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatChip("Volume", formatVolume(quote.volume), Modifier.weight(1f))
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceRangeSection(quote: StockQuote) {
    val lo = quote.yearLow ?: return
    val hi = quote.yearHigh ?: return
    val cur = quote.price
    val fraction = ((cur - lo) / (hi - lo)).coerceIn(0.0, 1.0).toFloat()

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("52-Week Range", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = GoldAccent,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("₹${"%,.2f".format(lo)}", color = RubyRed,
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text("₹${"%,.2f".format(cur)}", color = GoldAccent,
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text("₹${"%,.2f".format(hi)}", color = EmeraldGreen,
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReturnsDetailSection(quote: StockQuote) {
    val entries = buildList {
        quote.return1M?.let { add("1M" to it) }
        quote.return3M?.let { add("3M" to it) }
        quote.return6M?.let { add("6M" to it) }
        quote.return1Y?.let { add("1Y" to it) }
        quote.returnAll?.let { add("Since Launch" to it) }
    }
    if (entries.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Returns Breakdown", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(12.dp))
                entries.forEach { (label, value) ->
                    val isPos = !value.trimStart().startsWith("-")
                    val color = if (isPos) EmeraldGreen else RubyRed
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f))
                        Text(value, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold, color = color)
                    }
                }

                // Benchmark note if available
                if (!quote.benchmarkIndex.isNullOrEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text("Benchmark: ${quote.benchmarkIndex}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.4f))
                    }
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
                Text(
                    if (!quote.businessSummary.isNullOrEmpty() && quote.description.isNullOrEmpty())
                        "About ${quote.shortName}" else "Investment Objective",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
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
private fun FundInfoSection(quote: StockQuote) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
        SectionCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Fund Info", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(12.dp))
                quote.amc?.let { FundamentalRow("AMC", it) }
                if (!quote.fundManagers.isNullOrEmpty()) {
                    FundamentalRow("Fund Managers", quote.fundManagers.joinToString(", "))
                }
                quote.etfCategory?.let { FundamentalRow("Category", it) }
                quote.benchmarkIndex?.let { FundamentalRow("Benchmark", it) }
                quote.foundationDate?.let {
                    FundamentalRow("Inception", it.take(10))
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
                fontWeight = FontWeight.Bold, color = Color.White)
            Icon(Icons.Outlined.Info, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(12.dp))

        simplePeers?.forEach { peer ->
            EtfPeerCard(peer)
            Spacer(Modifier.height(8.dp))
        }

        stockPeers?.take(5)?.forEach { peer ->
            StockPeerCard(peer)
            Spacer(Modifier.height(8.dp))
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
            letterSpacing = 0.5.sp)
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = AureumCard, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
    ) { content() }
}

@Composable
private fun FundamentalRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.5f), modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium,
            color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp),
        color = AureumSurface, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun EtfPeerCard(peer: PeerInfo) {
    val isPos = (peer.dayChangePerc ?: 0.0) >= 0
    val changeColor = if (isPos) EmeraldGreen else RubyRed

    SectionCard {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                if (!peer.logoUrl.isNullOrEmpty()) {
                    AsyncImage(model = peer.logoUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else {
                    Text(peer.name.take(1), color = GoldAccent, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(peer.name, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ER: ${peer.expenseRatio}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f))
                    if (peer.return1Y != null) {
                        Text("1Y: ${"%.1f".format(peer.return1Y)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.45f))
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (peer.ltp != null) {
                    Text("₹${"%,.2f".format(peer.ltp)}",
                        fontWeight = FontWeight.ExtraBold, color = Color.White,
                        style = MaterialTheme.typography.bodyMedium)
                }
                if (peer.dayChangePerc != null) {
                    Text("${if (isPos) "+" else ""}${"%.2f".format(peer.dayChangePerc)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = changeColor, fontWeight = FontWeight.Bold)
                }
            }
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

// ======== Utility ========

private fun parseReturnValue(s: String): Double {
    return s.replace("%", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
}

private fun formatVolume(v: Long?): String {
    if (v == null) return "—"
    return when {
        v >= 10_000_000 -> "${"%.2f".format(v / 10_000_000.0)} Cr"
        v >= 100_000 -> "${"%.2f".format(v / 100_000.0)} L"
        v >= 1_000 -> "${"%.1f".format(v / 1_000.0)} K"
        else -> v.toString()
    }
}

private fun formatMcap(v: Double?): String {
    if (v == null) return "—"
    return when {
        v >= 100_000 -> "₹${"%,.0f".format(v / 100_000)}L Cr"
        else -> "₹${"%,.0f".format(v)} Cr"
    }
}
