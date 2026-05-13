package kwiktwik.ratewatch.app.data.model

data class StockQuote(
    val symbol: String,
    val shortName: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val currency: String = "INR",
    val lastUpdated: Long = System.currentTimeMillis()
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
    )

    val stocks = listOf(
        StockSymbol("RELIANCE.NS", "Reliance Industries"),
        StockSymbol("TCS.NS", "Tata Consultancy"),
        StockSymbol("HDFCBANK.NS", "HDFC Bank"),
        StockSymbol("INFY.NS", "Infosys"),
        StockSymbol("ICICIBANK.NS", "ICICI Bank"),
        StockSymbol("SBIN.NS", "State Bank of India"),
    )

    val all = indices + stocks
}
