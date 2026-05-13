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
    val latestTradingDay: String? = null
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
