package kwiktwik.ratewatch.app.data.repository

import kwiktwik.ratewatch.app.data.model.*
import kwiktwik.ratewatch.app.data.remote.RetrofitClient
import kwiktwik.ratewatch.app.data.remote.CandlesData
import kwiktwik.ratewatch.app.data.remote.ScrapeResponse
import kwiktwik.ratewatch.app.data.remote.ScraperHealthResponse
import kwiktwik.ratewatch.app.data.remote.SearchResultItem
import kwiktwik.ratewatch.app.data.remote.StockQuoteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepository @Inject constructor(
    private val prefs: PreferencesRepository
) {

    suspend fun getGoldSilverPrices(): Result<GoldSilverResponse> = withContext(Dispatchers.IO) {
        runCatching {
            RetrofitClient.goldSilverApi.getLatestPrices()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Triggers immediate gold/silver scrape from Goodreturns.
     * Returns the freshly scraped data.
     */
    suspend fun triggerMetalsScrape(): Result<GoldSilverResponse> = withContext(Dispatchers.IO) {
        runCatching {
            RetrofitClient.goldSilverApi.triggerScrape()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Fetches unified scraper health status (last scrape times, cache state, available endpoints).
     */
    suspend fun getScraperHealth(): Result<ScraperHealthResponse> = withContext(Dispatchers.IO) {
        runCatching {
            RetrofitClient.stocksApi.getHealth()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    fun getStockQuotes(symbols: List<String>): Flow<Result<List<StockQuote>>> = flow {
        if (symbols.isEmpty()) {
            emit(Result.success(emptyList()))
            return@flow
        }
        val result = runCatching {
            val csv = symbols.joinToString(",")
            val response = RetrofitClient.stocksApi.getQuotes(csv)
            if (!response.success) throw Exception("Stocks API returned failure")
            response.data.map { item ->
                StockQuote(
                    symbol = item.symbol,
                    shortName = item.name ?: item.symbol,
                    price = item.price,
                    change = item.change,
                    changePercent = item.changePercent.replace("%", "").replace("+", "").toDoubleOrNull() ?: 0.0,
                    currency = item.currency ?: "INR"
                )
            }
        }
        emit(result)
    }

    suspend fun getAllPopularStockQuotes(): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val symbols = PopularStocks.all.map { it.symbol }
            val csv = symbols.joinToString(",")
            val response = RetrofitClient.stocksApi.getQuotes(csv)
            if (!response.success) throw Exception("Stocks API returned failure")
            response.data.map { item ->
                StockQuote(
                    symbol = item.symbol,
                    shortName = item.name ?: item.symbol,
                    price = item.price,
                    change = item.change,
                    changePercent = item.changePercent.replace("%", "").replace("+", "").toDoubleOrNull() ?: 0.0,
                    currency = item.currency ?: "INR"
                )
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun getUsStockQuotes(): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            // Use the unified Stocks API (supports any Yahoo symbol including US)
            val usSymbols = listOf("AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "NVDA", "META")
            val csv = usSymbols.joinToString(",")
            val response = RetrofitClient.stocksApi.getQuotes(csv)
            if (!response.success) throw Exception("Stocks API returned failure")
            response.data.map { item ->
                StockQuote(
                    symbol = item.symbol,
                    shortName = item.name ?: item.symbol,
                    price = item.price,
                    change = item.change,
                    changePercent = item.changePercent.replace("%", "").replace("+", "").toDoubleOrNull() ?: 0.0,
                    currency = item.currency ?: "USD"
                )
            }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    // --- New Stocks API (per /stocks/... spec) ---

    suspend fun getStocksSummary(): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.getSummary()
            if (!response.success) throw Exception("Stocks API returned failure")
            response.data.map { item -> item.toStockQuote() }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun getLatestStocks(): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.getLatest()
            if (!response.success) throw Exception("Stocks API returned failure")
            response.data.map { item -> item.toStockQuote() }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun searchStocks(query: String): Result<List<SearchResultItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.search(query)
            if (!response.success) throw Exception("Stocks API returned failure")
            response.data
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun getStockChart(
        symbol: String,
        interval: String = "5m",
        range: String = "1d"
    ): Result<CandlesData?> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.getChart(symbol, interval, range)
            if (!response.success) throw Exception("Stocks API returned failure")
            response.data?.candles
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    // --- Groww-powered live market data (recommended primary source) ---

    /**
     * Fetches live Indian indices from Groww via the unified scraper.
     * Includes NIFTY 50, BANK NIFTY, INDIA VIX, FINNIFTY, MIDCAP, and 15+ sectoral indices.
     * Single HTTP call with 6s server cache.
     */
    suspend fun getGrowwIndianIndices(): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.getGrowwIndices()
            if (!response.success) throw Exception("Groww indices endpoint failed")
            response.data.map { it.toStockQuote() }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Fetches global instruments (GIFT Nifty, Dow Jones, S&P 500, Nikkei, Hang Seng, etc.)
     * 15-second cache on the scraper.
     */
    suspend fun getGrowwGlobalInstruments(): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.getGrowwGlobal()
            if (!response.success) throw Exception("Groww global endpoint failed")
            response.data.map { it.toStockQuote() }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Triggers an immediate background scrape of Groww market data.
     * Use sparingly — the scheduled + 6s cache already provides fresh data.
     */
    suspend fun triggerStocksScrape(): Result<ScrapeResponse> = withContext(Dispatchers.IO) {
        runCatching {
            RetrofitClient.stocksApi.triggerScrape()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    private fun StockQuoteItem.toStockQuote(): StockQuote = StockQuote(
        symbol = symbol,
        shortName = name ?: symbol,
        price = price,
        change = change,
        changePercent = changePercent.replace("%", "").replace("+", "").toDoubleOrNull() ?: 0.0,
        currency = currency ?: "INR",
        open = open,
        high = high,
        low = low,
        previousClose = previousClose,
        exchange = exchange,
        latestTradingDay = latestTradingDay
    )
}
