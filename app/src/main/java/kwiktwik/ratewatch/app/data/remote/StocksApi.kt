package kwiktwik.ratewatch.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StocksApi {
    @GET("stocks/quote")
    suspend fun getQuotes(@Query("symbols") symbols: String): StocksQuoteResponse

    @GET("stocks/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("interval") interval: String = "5m",
        @Query("range") range: String = "1d"
    ): StocksChartResponse

    @GET("stocks/search")
    suspend fun search(@Query("q") query: String): StocksSearchResponse

    @GET("stocks/summary")
    suspend fun getSummary(): StocksSummaryResponse

    @GET("stocks/latest")
    suspend fun getLatest(): StocksLatestResponse

    @POST("stocks/scrape")
    suspend fun triggerScrape(): ScrapeResponse
}

// --- Quote Endpoint ---

data class StocksQuoteResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<StockQuoteItem> = emptyList()
)

data class StockQuoteItem(
    val symbol: String,
    val name: String? = null,
    val price: Double = 0.0,
    val change: Double = 0.0,
    @SerializedName("changePercent") val changePercent: String = "0%",
    val volume: Long? = null,
    val high: Double? = null,
    val low: Double? = null,
    val open: Double? = null,
    val previousClose: Double? = null,
    val latestTradingDay: String? = null,
    val currency: String? = null,
    val exchange: String? = null,
    val scrapedAt: String? = null
)

// --- Chart Endpoint (TradingView Lightweight Charts compatible) ---

data class StocksChartResponse(
    val success: Boolean,
    val data: ChartDataWrapper? = null
)

data class ChartDataWrapper(
    val symbol: String,
    val meta: ChartMeta? = null,
    val candles: CandlesData? = null
)

data class ChartMeta(
    val regularMarketPrice: Double? = null,
    @SerializedName("previousClose") val previousClose: Double? = null,
    @SerializedName("fiftyTwoWeekHigh") val fiftyTwoWeekHigh: Double? = null,
    @SerializedName("fiftyTwoWeekLow") val fiftyTwoWeekLow: Double? = null,
    val currency: String? = null,
    val timezone: String? = null
)

data class CandlesData(
    @SerializedName("t") val t: List<Long> = emptyList(),
    @SerializedName("o") val o: List<Double> = emptyList(),
    @SerializedName("h") val h: List<Double> = emptyList(),
    @SerializedName("l") val l: List<Double> = emptyList(),
    @SerializedName("c") val c: List<Double> = emptyList(),
    @SerializedName("v") val v: List<Long>? = null
)

// --- Search Endpoint ---

data class StocksSearchResponse(
    val success: Boolean,
    val data: List<SearchResultItem> = emptyList()
)

data class SearchResultItem(
    val symbol: String,
    val shortname: String? = null,
    val longname: String? = null,
    val exchange: String? = null,
    @SerializedName("exchDisp") val exchDisp: String? = null,
    @SerializedName("quoteType") val quoteType: String? = null,
    val sector: String? = null
)

// --- Summary Endpoint ---

data class StocksSummaryResponse(
    val success: Boolean,
    val count: Int = 0,
    val data: List<StockQuoteItem> = emptyList()
)

// --- Latest Endpoint (background scrape fallback) ---

data class StocksLatestResponse(
    val success: Boolean,
    val data: List<StockQuoteItem> = emptyList(),
    @SerializedName("lastScrapeAt") val lastScrapeAt: String? = null
)

// --- Scrape trigger (admin/debug) ---

data class ScrapeResponse(
    val success: Boolean,
    val message: String? = null,
    val triggeredAt: String? = null
)
