package kwiktwik.ratewatch.app.data.model

data class StockQuote(
    val symbol: String,
    val shortName: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val currency: String = "INR",
    val lastUpdated: Long = System.currentTimeMillis(),
    // Rich fields from Groww scraper (available on /groww/indices and /groww/global)
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val previousClose: Double? = null,
    val exchange: String? = null,
    val latestTradingDay: String? = null,
    // Additional fields from updated Groww Indian indices response
    val logoUrl: String? = null,
    val searchId: String? = null,
    val yearHigh: Double? = null,
    val yearLow: Double? = null,
    val instrumentType: String? = null,
    val source: String? = null,
    val gsin: String? = null,
    // ETF-specific fields
    val aum: String? = null,
    val expenseRatio: String? = null,
    val trackingError: String? = null,
    val nav: String? = null,
    val return1M: String? = null,
    val return3M: String? = null,
    val return6M: String? = null,
    val return1Y: String? = null,
    val returnAll: String? = null,
    val description: String? = null,
    val peers: List<PeerInfo>? = null,
    val fundManagers: List<String>? = null,
    val amc: String? = null,
    val foundationDate: String? = null,
    val benchmarkIndex: String? = null,
    val etfCategory: String? = null,
    // Stock-specific enriched fields
    val fullName: String? = null,
    val headquarters: String? = null,
    val ceo: String? = null,
    val foundedYear: Int? = null,
    val businessSummary: String? = null,
    val websiteUrl: String? = null,
    val industryName: String? = null,
    val cappedType: String? = null,          // Large Cap / Mid Cap / Small Cap
    val marketCap: Double? = null,
    val peRatio: Double? = null,
    val pbRatio: Double? = null,
    val divYield: Double? = null,
    val epsTtm: Double? = null,
    val roe: Double? = null,
    val debtToEquity: Double? = null,
    val faceValue: Double? = null,
    val netProfitMargin: Double? = null,
    val operatingProfitMargin: Double? = null,
    val bookValue: Double? = null,
    val industryPe: Double? = null,
    val fundamentals: List<FundamentalItem>? = null,
    val stockPeers: List<StockPeerInfo>? = null,
    val financials: StockFinancials? = null,
    val newsItems: List<StockNewsItem>? = null,
    val eventsItems: List<StockEventItem>? = null,
    val shareholdingPattern: Map<String, ShareholdingQuarter>? = null,
    val brandLogos: List<BrandInfo>? = null,
    val isFnoEnabled: Boolean = false,
    val nseScriptCode: String? = null,
    val bseScriptCode: String? = null,
    val volume: Long? = null,
    val totalBuyQty: Long? = null,
    val totalSellQty: Long? = null
)

data class PeerInfo(
    val name: String,
    val expenseRatio: String,
    val isin: String,
    val logoUrl: String? = null,
    val ltp: Double? = null,
    val dayChange: Double? = null,
    val dayChangePerc: Double? = null,
    val return1Y: Double? = null,
    val aumInCrores: Double? = null,
    val trackingError: Double? = null
)

data class FundamentalItem(
    val name: String,
    val shortName: String,
    val value: String
)

data class StockPeerInfo(
    val searchId: String,
    val displayName: String,
    val shortName: String,
    val logoUrl: String?,
    val marketCap: Double?,
    val peRatio: Double?,
    val pbRatio: Double?,
    val nseSymbol: String?,
    val ltp: Double?,
    val dayChange: Double?,
    val dayChangePerc: Double?,
    val yearHigh: Double?,
    val yearLow: Double?
)

data class StockFinancials(
    val consolidatedRevenue: Map<String, Double>?,
    val consolidatedProfit: Map<String, Double>?,
    val consolidatedNetWorth: Map<String, Double>?,
    val quarterlyRevenue: Map<String, Double>?,
    val quarterlyProfit: Map<String, Double>?
)

data class StockNewsItem(
    val id: String,
    val title: String,
    val url: String?,
    val pubDate: String?,
    val source: String?
)

data class StockEventItem(
    val title: String,
    val eventType: String?,
    val primaryDate: String?,
    val description: String?,
    val corporateEventFilter: String?,
    val detailValue: String?,
    val detailDescription: String?
)

data class ShareholdingQuarter(
    val promoters: Double,
    val mutualFunds: Double,
    val foreignInstitutions: Double,
    val insurance: Double,
    val retailAndOthers: Double
)

data class BrandInfo(
    val name: String,
    val logoUrl: String
)

data class StockSymbol(
    val symbol: String,
    val displayName: String,
    val exchange: String = "NSE"
)

object PopularStocks {
    val indices = listOf(
        StockSymbol("^NSEI", "Nifty 50"),
        StockSymbol("^BSESN", "Sensex"),
        StockSymbol("^NSEBANK", "Bank Nifty"),
        StockSymbol("^NSEIT", "Nifty IT"),
        StockSymbol("^CNXAUTO", "Nifty Auto"),
        StockSymbol("^INDIAVIX", "India VIX"),
    )

    val stocks = listOf(
        StockSymbol("RELIANCE.NS", "Reliance Industries"),
        StockSymbol("TCS.NS", "Tata Consultancy"),
        StockSymbol("HDFCBANK.NS", "HDFC Bank"),
        StockSymbol("ICICIBANK.NS", "ICICI Bank"),
        StockSymbol("INFY.NS", "Infosys"),
        StockSymbol("SBIN.NS", "State Bank of India"),
        StockSymbol("BHARTIARTL.NS", "Bharti Airtel"),
        StockSymbol("LT.NS", "Larsen & Toubro"),
        StockSymbol("ITC.NS", "ITC Limited"),
        StockSymbol("TATAMOTORS.NS", "Tata Motors"),
    )

    val metals = listOf(
        StockSymbol("GC=F", "Gold Futures"),
        StockSymbol("SI=F", "Silver Futures"),
        StockSymbol("GOLDBEES.NS", "Nippon India Gold BeES"),
        StockSymbol("SILVERBEES.NS", "Nippon India Silver ETF"),
        StockSymbol("HDFCGOLD.NS", "HDFC Gold ETF"),
    )

    val all = indices + stocks
}
