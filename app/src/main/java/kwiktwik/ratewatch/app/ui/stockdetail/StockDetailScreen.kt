package kwiktwik.ratewatch.app.ui.stockdetail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Financials", "Peers", "News")

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
                        Icon(Icons.Default.BookmarkBorder, null, tint = GoldAccent)
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
            // Hero Header
            item { StockHeroHeader(quote, accentColor, isPositive) }

            // Price Range Bar
            item {
                if (quote.yearHigh != null && quote.yearLow != null) {
                    PriceRangeSection(quote)
                }
            }

            // Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AureumBg,
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GoldAccent
                        )
                    }
                ) {
                    tabs.forEachIndexed { i, label ->
                        Tab(
                            selected = selectedTab == i,
                            onClick = { selectedTab = i },
                            text = {
                                Text(label,
                                    color = if (selectedTab == i) GoldAccent else Color.White.copy(alpha = 0.5f),
                                    fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Tab Content
            when (selectedTab) {
                0 -> overviewItems(quote)
                1 -> financialsItems(quote)
                2 -> peersItems(quote)
                3 -> newsItems(quote)
            }
        }
    }
}

@Composable
private fun StockHeroHeader(quote: StockQuote, accentColor: Color, isPositive: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!quote.logoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = quote.logoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(14.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(quote.shortName, style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!quote.exchange.isNullOrEmpty()) {
                        SmallBadge(quote.exchange!!)
                    }
                    if (!quote.cappedType.isNullOrEmpty()) {
                        SmallBadge(quote.cappedType!!, color = GoldAccent.copy(alpha = 0.15f), textColor = GoldAccent)
                    }
                    if (quote.isFnoEnabled) {
                        SmallBadge("F&O", color = Color(0xFF1E3A5F), textColor = Color(0xFF60A5FA))
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("₹${"%,.2f".format(quote.price)}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                null, tint = accentColor, modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "${if (isPositive) "+" else ""}${"%.2f".format(quote.change)} (${"%.2f".format(quote.changePercent)}%)",
                color = accentColor, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            if (!quote.industryName.isNullOrEmpty()) {
                Text(" • ${quote.industryName}", color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.bodySmall)
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text("52-Week Range", style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f))
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = GoldAccent,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("₹${"%,.0f".format(lo)}", color = RubyRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text("₹${"%,.0f".format(hi)}", color = EmeraldGreen, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun LazyListScope.overviewItems(quote: StockQuote) {
    // OHLC stats
    item {
        Padded {
            SectionTitle("Today's Stats")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip("Open", quote.open?.let { "₹${"%,.0f".format(it)}" } ?: "—", Modifier.weight(1f))
                StatChip("Prev Close", quote.previousClose?.let { "₹${"%,.0f".format(it)}" } ?: "—", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip("Day High", quote.high?.let { "₹${"%,.0f".format(it)}" } ?: "—", Modifier.weight(1f))
                StatChip("Day Low", quote.low?.let { "₹${"%,.0f".format(it)}" } ?: "—", Modifier.weight(1f))
            }
            if (quote.volume != null) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip("Volume", formatVolume(quote.volume), Modifier.weight(1f))
                    StatChip("VWAP", "—", Modifier.weight(1f))
                }
            }
        }
    }

    // Fundamentals grid
    if (!quote.fundamentals.isNullOrEmpty()) {
        item {
            Padded {
                SectionTitle("Fundamentals")
                Spacer(Modifier.height(10.dp))
                SectionCard {
                    Column(modifier = Modifier.padding(4.dp)) {
                        quote.fundamentals.chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth()) {
                                row.forEach { f ->
                                    FundCard(f.shortName, f.value, Modifier.weight(1f))
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    // About
    if (!quote.businessSummary.isNullOrEmpty()) {
        item {
            Padded {
                SectionTitle("About ${quote.shortName}")
                Spacer(Modifier.height(10.dp))
                var expanded by remember { mutableStateOf(false) }
                SectionCard {
                    Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
                        Text(
                            quote.businessSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = if (expanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (expanded) "Show less ↑" else "Read more ↓",
                            color = GoldAccent, style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { expanded = !expanded }
                        )
                    }
                }
                // Company info chips
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    if (!quote.ceo.isNullOrEmpty()) InfoChip("CEO", quote.ceo!!)
                    if (!quote.headquarters.isNullOrEmpty()) InfoChip("HQ", quote.headquarters!!)
                    if (quote.foundedYear != null) InfoChip("Founded", "${quote.foundedYear}")
                }
            }
        }
    }

    // Shareholding
    if (!quote.shareholdingPattern.isNullOrEmpty()) {
        item {
            Padded {
                SectionTitle("Shareholding Pattern")
                Spacer(Modifier.height(10.dp))
                val lastQuarter = quote.shareholdingPattern.keys.last()
                val sh = quote.shareholdingPattern[lastQuarter]!!
                SectionCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(lastQuarter, style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.4f))
                        Spacer(Modifier.height(12.dp))
                        ShareholdingBar("Promoters", sh.promoters, Color(0xFF3B82F6))
                        ShareholdingBar("Foreign Inst.", sh.foreignInstitutions, Color(0xFF8B5CF6))
                        ShareholdingBar("Mutual Funds", sh.mutualFunds, GoldAccent)
                        ShareholdingBar("Insurance", sh.insurance, EmeraldGreen)
                        ShareholdingBar("Retail & Others", sh.retailAndOthers, Color(0xFFF97316))
                    }
                }
            }
        }
    }

    // Corporate Events
    if (!quote.eventsItems.isNullOrEmpty()) {
        item {
            Padded {
                SectionTitle("Corporate Events")
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quote.eventsItems.take(5).forEach { ev ->
                        EventCard(ev)
                    }
                }
            }
        }
    }
}

private fun LazyListScope.financialsItems(quote: StockQuote) {
    val fin = quote.financials
    if (fin == null) {
        item { EmptyState("No financial data available") }
        return
    }
    item {
        Padded {
            if (!fin.consolidatedRevenue.isNullOrEmpty()) {
                SectionTitle("Revenue (₹ Cr)")
                Spacer(Modifier.height(10.dp))
                SimpleBarChart(fin.consolidatedRevenue, EmeraldGreen)
                Spacer(Modifier.height(20.dp))
            }
            if (!fin.consolidatedProfit.isNullOrEmpty()) {
                SectionTitle("Net Profit (₹ Cr)")
                Spacer(Modifier.height(10.dp))
                SimpleBarChart(fin.consolidatedProfit, GoldAccent)
                Spacer(Modifier.height(20.dp))
            }
            if (!fin.quarterlyRevenue.isNullOrEmpty()) {
                SectionTitle("Quarterly Revenue (₹ Cr)")
                Spacer(Modifier.height(10.dp))
                SimpleBarChart(fin.quarterlyRevenue, Color(0xFF60A5FA))
                Spacer(Modifier.height(20.dp))
            }
            if (!fin.quarterlyProfit.isNullOrEmpty()) {
                SectionTitle("Quarterly Profit (₹ Cr)")
                Spacer(Modifier.height(10.dp))
                SimpleBarChart(fin.quarterlyProfit, Color(0xFFA78BFA))
            }
        }
    }
}

private fun LazyListScope.peersItems(quote: StockQuote) {
    val peers = quote.stockPeers
    if (peers.isNullOrEmpty()) {
        item { EmptyState("No peer data available") }
        return
    }
    item {
        Padded {
            SectionTitle("Peer Comparison")
            Spacer(Modifier.height(10.dp))
        }
    }
    items(peers) { peer ->
        Padded(vertical = 4.dp) {
            PeerCard(peer)
        }
    }
}

private fun LazyListScope.newsItems(quote: StockQuote) {
    val news = quote.newsItems
    if (news.isNullOrEmpty()) {
        item { EmptyState("No news available") }
        return
    }
    item {
        Padded {
            SectionTitle("Latest News")
            Spacer(Modifier.height(10.dp))
        }
    }
    items(news) { n ->
        Padded(vertical = 4.dp) { NewsCard(n) }
    }
}

// ---- Sub-components ----

@Composable
private fun SmallBadge(text: String, color: Color = Color.White.copy(0.08f), textColor: Color = Color.White.copy(0.6f)) {
    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = AureumCard, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
        content()
    }
}

@Composable
private fun Padded(vertical: Dp = 8.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = vertical), content = content)
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp),
        color = AureumCard, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun FundCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(12.dp)) {
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ShareholdingBar(label: String, percent: Double, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Text("${"%.1f".format(percent)}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (percent / 100).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color, trackColor = Color.White.copy(alpha = 0.08f)
        )
    }
}

@Composable
private fun EventCard(ev: StockEventItem) {
    SectionCard {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val (bg, ic) = when (ev.corporateEventFilter) {
                "DIVIDEND" -> Pair(EmeraldGreen.copy(alpha = 0.15f), EmeraldGreen)
                "BONUS" -> Pair(GoldAccent.copy(alpha = 0.15f), GoldAccent)
                "RESULTS" -> Pair(Color(0xFF60A5FA).copy(alpha = 0.15f), Color(0xFF60A5FA))
                else -> Pair(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.4f))
            }
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(bg),
                contentAlignment = Alignment.Center) {
                Icon(when (ev.corporateEventFilter) {
                    "DIVIDEND" -> Icons.Default.AttachMoney
                    "BONUS" -> Icons.Default.Star
                    else -> Icons.Default.CalendarToday
                }, null, tint = ic, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(ev.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                if (!ev.description.isNullOrEmpty()) {
                    Text(ev.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.55f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (!ev.primaryDate.isNullOrEmpty()) {
                    Text(formatDate(ev.primaryDate), style = MaterialTheme.typography.labelSmall, color = GoldAccent.copy(alpha = 0.8f))
                }
            }
            if (!ev.detailValue.isNullOrEmpty()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(ev.detailValue, fontWeight = FontWeight.ExtraBold, color = ic, style = MaterialTheme.typography.bodyMedium)
                    if (!ev.detailDescription.isNullOrEmpty())
                        Text(ev.detailDescription, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
private fun PeerCard(peer: StockPeerInfo) {
    val isPos = (peer.dayChangePerc ?: 0.0) >= 0
    val color = if (isPos) EmeraldGreen else RubyRed
    SectionCard {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center) {
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
                    fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (peer.peRatio != null) Text("P/E: ${"%.1f".format(peer.peRatio)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
                    if (peer.marketCap != null) Text("MCap: ${formatMcap(peer.marketCap)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (peer.ltp != null) Text("₹${"%,.1f".format(peer.ltp)}", fontWeight = FontWeight.ExtraBold, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                if (peer.dayChangePerc != null) Text("${if (isPos) "+" else ""}${"%.2f".format(peer.dayChangePerc)}%",
                    style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NewsCard(news: StockNewsItem) {
    SectionCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Text(news.title, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f), maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (!news.source.isNullOrEmpty()) {
                    SmallBadge(news.source!!, color = GoldAccent.copy(alpha = 0.12f), textColor = GoldAccent)
                }
                if (!news.pubDate.isNullOrEmpty()) {
                    Text(formatDate(news.pubDate), style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.35f))
                }
            }
        }
    }
}

@Composable
private fun SimpleBarChart(data: Map<String, Double>, barColor: Color) {
    val entries = data.entries.toList()
    val max = entries.maxOfOrNull { it.value } ?: 1.0
    SectionCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().height(120.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                entries.forEach { (key, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        val frac = (value / max).toFloat().coerceIn(0f, 1f)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(frac)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barColor.copy(alpha = 0.8f)))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(key.takeLast(4), style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f), maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(msg: String) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(msg, color = Color.White.copy(alpha = 0.35f))
    }
}

private fun formatDate(iso: String?): String {
    if (iso.isNullOrEmpty()) return ""
    return try { iso.substring(0, 10) } catch (e: Exception) { iso }
}

private fun formatVolume(v: Long?): String {
    if (v == null) return "—"
    return when {
        v >= 10_000_000 -> "${"%.2f".format(v / 10_000_000.0)}Cr"
        v >= 100_000 -> "${"%.2f".format(v / 100_000.0)}L"
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
