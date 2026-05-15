package kwiktwik.ratewatch.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StocksApi {
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

    // --- Groww-powered live endpoints (recommended for Indian market data) ---
    // Single call returns 15-25 Indian indices with full OHLC + change data. 6s server cache.
    @GET("stocks/groww/indices")
    suspend fun getGrowwIndices(): GrowwIndicesResponse

    // Global instruments: GIFT Nifty, Dow Jones, S&P 500, Nikkei, Hang Seng, etc.
    @GET("stocks/groww/global")
    suspend fun getGrowwGlobal(): GrowwGlobalResponse

    // Groww-powered global search (autocomplete)
    @GET("stocks/groww/search")
    suspend fun searchGroww(@Query("q") query: String, @Query("size") size: Int? = 10): GrowwSearchResponse

    // Raw Groww search metadata (for symbol resolution / autocomplete)
    @GET("stocks/{searchId}")
    suspend fun getGrowwDetails(@Path("searchId") searchId: String): com.google.gson.JsonObject

    // Server health & scrape status (useful for diagnostics)
    @GET("health")
    suspend fun getHealth(): ScraperHealthResponse

    // --- Groww Market Sections (Top Gainers, Losers, etc.) ---
    @GET("stocks/markets/categories")
    suspend fun getMarketCategories(): GrowwMarketCategoriesResponse

    @GET("stocks/markets/{type}")
    suspend fun getMarketData(
        @Path("type") type: String,
        @Query("index") index: String? = null
    ): GrowwMarketDataResponse
}

data class StockQuoteItem(
    val symbol: String? = null,
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
    val scrapedAt: String? = null,
    // New fields from updated Groww indices scraper response
    @SerializedName(value = "logoUrl", alternate = ["logo_url"]) val logoUrl: String? = null,
    @SerializedName(value = "searchId", alternate = ["search_id"]) val searchId: String? = null,
    @SerializedName(value = "gsin", alternate = ["isin"]) val gsin: String? = null,
    @SerializedName(value = "yearHigh", alternate = ["year_high"]) val yearHigh: Double? = null,
    @SerializedName(value = "yearLow", alternate = ["year_low"]) val yearLow: Double? = null,
    @SerializedName(value = "instrumentType", alternate = ["instrument_type"]) val instrumentType: String? = null,
    @SerializedName("isBse") val isBse: Boolean? = null,
    val continent: String? = null,
    val country: String? = null,
    val source: String? = null,

    // Groww Market Specific Fields
    @SerializedName(value = "companyName", alternate = ["company_name"]) val companyName: String? = null,
    @SerializedName(value = "companyShortName", alternate = ["company_short_name"]) val companyShortName: String? = null,
    @SerializedName(value = "nseScriptCode", alternate = ["nse_scrip_code", "nse_script_code"]) val nseScriptCode: String? = null,
    @SerializedName(value = "bseScriptCode", alternate = ["bse_scrip_code", "bse_script_code"]) val bseScriptCode: String? = null,
    @SerializedName("ltp") val ltp: Double? = null,
    @SerializedName("dayChange") val dayChange: Double? = null,
    @SerializedName(value = "dayChangePerc", alternate = ["day_change_perc"]) val dayChangePerc: Double? = null,
    @SerializedName("marketCap") val marketCap: Double? = null,
    @SerializedName("close") val close: Double? = null
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

// --- Groww Live Endpoints (Unified Scraper API) ---

/**
 * Response for GET /stocks/groww/indices
 * Returns 15-25 Indian market indices (NIFTY 50, BANK NIFTY, INDIA VIX, sectoral, etc.)
 * in a single fast call with 6-second server-side cache.
 */
data class GrowwIndicesResponse(
    val success: Boolean = true,   // tolerant default if field missing
    val count: Int = 0,
    val data: List<StockQuoteItem> = emptyList(),
    val source: String? = null,
    val cached: Boolean = false
)

/**
 * Response for GET /stocks/groww/global
 * GIFT Nifty + major global indices (Dow Jones, S&P 500, Nikkei, Hang Seng, etc.)
 * 15-second cache.
 */
data class GrowwGlobalResponse(
    val success: Boolean = true,
    val count: Int = 0,
    val data: List<StockQuoteItem> = emptyList(),
    val source: String? = null,
    val cached: Boolean = false
)

// --- Health Check Response (Unified Scraper) ---

data class ScraperHealthResponse(
    val status: String = "ok",
    val metals: MetalsHealth? = null,
    val indices: IndicesHealth? = null,
    val stocksApi: StocksApiHealth? = null
)

data class MetalsHealth(
    @SerializedName("lastScrapeAt") val lastScrapeAt: String? = null,
    val isScraping: Boolean = false,
    val count: Int = 0
)

data class IndicesHealth(
    @SerializedName("lastScrapeAt") val lastScrapeAt: String? = null,
    val isScraping: Boolean = false,
    val count: Int = 0,
    val growwCacheSize: Int? = null
)

data class StocksApiHealth(
    val endpoints: List<String> = emptyList(),
    val sources: List<String> = emptyList(),
    val indianMarketReady: Boolean = false,
    val note: String? = null
)

// --- Groww Market Categories ---

data class GrowwMarketCategoriesResponse(
    val success: Boolean,
    val data: GrowwMarketCategoriesData
)

data class GrowwMarketCategoriesData(
    val sections: List<GrowwMarketCategory> = emptyList(),
    val indices: List<GrowwMarketIndex> = emptyList()
)

data class GrowwMarketCategory(
    val id: String,
    val name: String
)

data class GrowwMarketIndex(
    val id: String,
    val name: String
)

data class GrowwMarketDataResponse(
    val success: Boolean,
    val type: String? = null,
    val index: String? = null,
    val count: Int = 0,
    val data: List<StockQuoteItem> = emptyList(),
    val source: String? = null
)

// --- Groww Search ---

data class GrowwSearchResponse(
    val success: Boolean,
    val query: String? = null,
    val count: Int = 0,
    val data: List<GrowwSearchResultItem> = emptyList(),
    val source: String? = null
)

data class GrowwSearchResultItem(
    @SerializedName(value = "searchId", alternate = ["search_id"]) val searchId: String,
    val title: String,
    @SerializedName(value = "entityType", alternate = ["entity_type"]) val entityType: String? = null,
    val isin: String? = null,
    @SerializedName(value = "bseScripCode", alternate = ["bse_scrip_code"]) val bseScripCode: String? = null,
    @SerializedName(value = "nseScripCode", alternate = ["nse_scrip_code"]) val nseScripCode: String? = null,
    @SerializedName(value = "logoUrl", alternate = ["logo_url"]) val logoUrl: String? = null,
    val id: String? = null
)

