package kwiktwik.ratewatch.app.data.repository

import kwiktwik.ratewatch.app.data.model.*
import kwiktwik.ratewatch.app.data.remote.RetrofitClient
import kwiktwik.ratewatch.app.data.remote.GrowwMarketCategoriesData
import kwiktwik.ratewatch.app.data.remote.CandlesData
import kwiktwik.ratewatch.app.data.remote.ScrapeResponse
import kwiktwik.ratewatch.app.data.remote.ScraperHealthResponse
import kwiktwik.ratewatch.app.data.remote.SearchResultItem
import kwiktwik.ratewatch.app.data.remote.StockQuoteItem
import kwiktwik.ratewatch.app.data.remote.GrowwSearchResultItem
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
            response.data.map { item -> item.toStockQuote() }
        }
        emit(result)
    }

    suspend fun getAllPopularStockQuotes(): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val symbols = PopularStocks.all.map { it.symbol }
            val csv = symbols.joinToString(",")
            val response = RetrofitClient.stocksApi.getQuotes(csv)
            if (!response.success) throw Exception("Stocks API returned failure")
            response.data.map { item -> item.toStockQuote() }
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
            response.data.map { item -> item.toStockQuote() }
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
     * Fetches market categories (sections like Top Gainers, Losers and Indices).
     */
    suspend fun getMarketCategories(): Result<GrowwMarketCategoriesData> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.getMarketCategories()
            if (!response.success) throw Exception("Market categories endpoint failed")
            response.data
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Fetches market data for a specific category (e.g., top-gainers) and index.
     */
    suspend fun getMarketData(type: String, index: String? = null): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.getMarketData(type, index)
            if (!response.success) throw Exception("Market data endpoint failed")
            response.data.map { it.toStockQuote() }
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }
    /**
     * Searches for instruments using Groww's global search API.
     */
    suspend fun searchGroww(query: String): Result<List<GrowwSearchResultItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.searchGroww(query)
            if (!response.success) throw Exception("Groww search endpoint failed")
            response.data
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Resolves a search ID into full instrument metadata.
     */
    suspend fun getGrowwDetails(searchId: String): Result<StockQuote> = withContext(Dispatchers.IO) {
        runCatching {
            val response = RetrofitClient.stocksApi.getGrowwDetails(searchId)
            if (!response.get("success").asBoolean) throw Exception("Groww details endpoint failed")
            val data = response.getAsJsonObject("data") ?: throw Exception("Details data missing")

            var header: com.google.gson.JsonObject? = null
            if (data.has("stockData") && data.getAsJsonObject("stockData").has("header")) {
                header = data.getAsJsonObject("stockData").getAsJsonObject("header")
            } else if (data.has("companyHeadersByIsin")) {
                val headersMap = data.getAsJsonObject("companyHeadersByIsin")
                for (key in headersMap.keySet()) {
                    val h = headersMap.getAsJsonObject(key)
                    if (h.get("searchId")?.asString == searchId) { header = h; break }
                }
            } else if (data.has("indexData") && data.getAsJsonObject("indexData").has("header")) {
                header = data.getAsJsonObject("indexData").getAsJsonObject("header")
            }
            if (header == null) throw Exception("Header not found in response")

            val nseCode = header.get("nseScriptCode")?.takeIf { !it.isJsonNull }?.asString
            val bseCode = header.get("bseScriptCode")?.takeIf { !it.isJsonNull }?.asString
            val symbol = nseCode ?: bseCode ?: searchId

            var ltp = 0.0; var dayChange = 0.0; var dayChangePerc = 0.0; var close = 0.0
            var yearHigh: Double? = null; var yearLow: Double? = null
            var volume: Long? = null; var totalBuyQty: Long? = null; var totalSellQty: Long? = null
            var openP: Double? = null; var highP: Double? = null; var lowP: Double? = null

            if (data.has("livePriceData")) {
                val lpd = data.getAsJsonObject("livePriceData")
                val po = (if (nseCode != null && lpd.has(nseCode)) lpd.getAsJsonObject(nseCode) else null)
                    ?: (if (bseCode != null && lpd.has(bseCode)) lpd.getAsJsonObject(bseCode) else null)
                if (po != null) {
                    ltp = po.get("ltp")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                    dayChange = po.get("dayChange")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                    dayChangePerc = po.get("dayChangePerc")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                    close = po.get("close")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                    yearHigh = po.get("yearHighPrice")?.takeIf { !it.isJsonNull }?.asDouble
                    yearLow = po.get("yearLowPrice")?.takeIf { !it.isJsonNull }?.asDouble
                    volume = po.get("volume")?.takeIf { !it.isJsonNull }?.asLong
                    totalBuyQty = po.get("totalBuyQty")?.takeIf { !it.isJsonNull }?.asLong
                    totalSellQty = po.get("totalSellQty")?.takeIf { !it.isJsonNull }?.asLong
                    openP = po.get("open")?.takeIf { !it.isJsonNull }?.asDouble
                    highP = po.get("high")?.takeIf { !it.isJsonNull }?.asDouble
                    lowP = po.get("low")?.takeIf { !it.isJsonNull }?.asDouble
                }
            }
            if (yearHigh == null && data.has("stockData")) {
                val pd = data.getAsJsonObject("stockData")?.getAsJsonObject("priceData")
                val ns = pd?.getAsJsonObject("nse") ?: pd?.getAsJsonObject("bse")
                yearHigh = ns?.get("yearHighPrice")?.takeIf { !it.isJsonNull }?.asDouble
                yearLow = ns?.get("yearLowPrice")?.takeIf { !it.isJsonNull }?.asDouble
            }

            val displayName = header.get("displayName")?.takeIf { !it.isJsonNull }?.asString
                ?: header.get("shortName")?.takeIf { !it.isJsonNull }?.asString ?: symbol

            // ETF fields
            var aum: String? = null; var expenseRatio: String? = null; var trackingError: String? = null; var nav: String? = null
            var return1M: String? = null; var return3M: String? = null; var return6M: String? = null
            var return1Y: String? = null; var returnAll: String? = null
            var description: String? = null; var etfPeers: List<PeerInfo>? = null
            var fundManagers: List<String>? = null; var etfAmc: String? = null
            var foundationDate: String? = null; var benchmarkIndex: String? = null; var etfCategory: String? = null

            data.getAsJsonObject("fundamentalsData")?.let { f ->
                aum = f.get("aumInCrores")?.takeIf { !it.isJsonNull }?.asString?.let { "₹$it Cr" }
                expenseRatio = f.get("expenseRatio")?.takeIf { !it.isJsonNull }?.asString?.let { "$it%" }
                trackingError = f.get("trackingError")?.takeIf { !it.isJsonNull }?.asString?.let { "$it%" }
                nav = f.get("nav")?.takeIf { !it.isJsonNull }?.asString?.let { "₹$it" }
            }
            data.getAsJsonObject("categoryReturnsData")?.let { r ->
                return1M = r.get("return1M")?.takeIf { !it.isJsonNull }?.asString?.let { "$it%" }
                return3M = r.get("return3M")?.takeIf { !it.isJsonNull }?.asString?.let { "$it%" }
                return6M = r.get("return6M")?.takeIf { !it.isJsonNull }?.asString?.let { "$it%" }
                return1Y = r.get("return1Y")?.takeIf { !it.isJsonNull }?.asString?.let { "$it%" }
                returnAll = r.get("returnAll")?.takeIf { !it.isJsonNull }?.asString?.let { "$it%" }
            }
            data.getAsJsonObject("etfInfoData")?.let { info ->
                description = info.get("description")?.takeIf { !it.isJsonNull }?.asString
                etfAmc = info.get("amc")?.takeIf { !it.isJsonNull }?.asString
                foundationDate = info.get("foundationDate")?.takeIf { !it.isJsonNull }?.asString
                benchmarkIndex = info.get("benchmarkIndex")?.takeIf { !it.isJsonNull }?.asString
                etfCategory = info.get("category")?.takeIf { !it.isJsonNull }?.asString
                info.getAsJsonArray("fundManagers")?.let { arr ->
                    fundManagers = (0 until arr.size()).mapNotNull { i ->
                        arr.get(i)?.takeIf { !it.isJsonNull }?.asString
                    }
                }
            }
            if (data.has("etfPeersData") && !data.get("etfPeersData").isJsonNull && data.has("companyHeadersByIsin")) {
                val ep = data.getAsJsonObject("etfPeersData")
                val hm = data.getAsJsonObject("companyHeadersByIsin")
                val lpd = if (data.has("livePriceData")) data.getAsJsonObject("livePriceData") else null
                val arr = ep.getAsJsonArray("peers")
                if (arr != null) {
                    etfPeers = (0 until arr.size()).mapNotNull { i ->
                        val p = arr.get(i).asJsonObject
                        val peerIsin = p.get("isin")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                        val er = p.get("expenseRatio")?.takeIf { !it.isJsonNull }?.asString?.let { "$it%" } ?: "N/A"
                        val peerAum = p.get("aumInCrores")?.takeIf { !it.isJsonNull }?.asDouble
                        val peerTe = p.get("trackingError")?.takeIf { !it.isJsonNull }?.asDouble
                        val peerRet1Y = p.get("return1Y")?.takeIf { !it.isJsonNull }?.asDouble
                        val ph = hm.getAsJsonObject(peerIsin)
                        val n = ph?.get("displayName")?.takeIf { !it.isJsonNull }?.asString ?: ph?.get("shortName")?.takeIf { !it.isJsonNull }?.asString ?: peerIsin
                        val peerLogo = ph?.get("logoUrl")?.takeIf { !it.isJsonNull }?.asString
                        val peerNse = ph?.get("nseScriptCode")?.takeIf { !it.isJsonNull }?.asString
                        val peerBse = ph?.get("bseScriptCode")?.takeIf { !it.isJsonNull }?.asString
                        val peerSym = peerNse ?: peerBse
                        var peerLtp: Double? = null; var peerDc: Double? = null; var peerDcp: Double? = null
                        if (lpd != null && peerSym != null && lpd.has(peerSym)) {
                            val pp = lpd.getAsJsonObject(peerSym)
                            peerLtp = pp.get("ltp")?.takeIf { !it.isJsonNull }?.asDouble
                            peerDc = pp.get("dayChange")?.takeIf { !it.isJsonNull }?.asDouble
                            peerDcp = pp.get("dayChangePerc")?.takeIf { !it.isJsonNull }?.asDouble
                        }
                        PeerInfo(n, er, peerIsin, peerLogo, peerLtp, peerDc, peerDcp, peerRet1Y, peerAum, peerTe)
                    }
                }
            }

            // Stock-specific fields
            var fullName: String? = null; var headquarters: String? = null; var ceo: String? = null
            var foundedYear: Int? = null; var businessSummary: String? = null; var websiteUrl: String? = null
            var industryName: String? = null; var cappedType: String? = null
            var marketCap: Double? = null; var peRatio: Double? = null; var pbRatio: Double? = null
            var divYield: Double? = null; var epsTtm: Double? = null; var roe: Double? = null
            var debtToEquity: Double? = null; var faceValue: Double? = null
            var netProfitMargin: Double? = null; var operatingProfitMargin: Double? = null
            var bookValue: Double? = null; var industryPe: Double? = null
            var fundamentalItems: List<FundamentalItem>? = null
            var stockPeers: List<StockPeerInfo>? = null
            var financials: StockFinancials? = null
            var newsItems: List<StockNewsItem>? = null
            var eventsItems: List<StockEventItem>? = null
            var shareholding: Map<String, ShareholdingQuarter>? = null
            var brandLogos: List<BrandInfo>? = null
            var isFnoEnabled = false

            if (data.has("stockData") && !data.get("stockData").isJsonNull) {
                val sd = data.getAsJsonObject("stockData")
                isFnoEnabled = header.get("isFnoEnabled")?.takeIf { !it.isJsonNull }?.asBoolean ?: false
                industryName = header.get("industryName")?.takeIf { !it.isJsonNull }?.asString

                sd.getAsJsonObject("details")?.let { d ->
                    fullName = d.get("fullName")?.takeIf { !it.isJsonNull }?.asString
                    headquarters = d.get("headquarters")?.takeIf { !it.isJsonNull }?.asString
                    ceo = d.get("ceo")?.takeIf { !it.isJsonNull }?.asString
                    foundedYear = d.get("foundedYear")?.takeIf { !it.isJsonNull }?.asInt
                    businessSummary = d.get("businessSummary")?.takeIf { !it.isJsonNull }?.asString
                    websiteUrl = d.get("websiteUrl")?.takeIf { !it.isJsonNull }?.asString
                    if (description == null) description = businessSummary
                }

                sd.getAsJsonObject("stats")?.let { s ->
                    cappedType = s.get("cappedType")?.takeIf { !it.isJsonNull }?.asString
                    marketCap = s.get("marketCap")?.takeIf { !it.isJsonNull }?.asDouble
                    peRatio = s.get("peRatio")?.takeIf { !it.isJsonNull }?.asDouble
                    pbRatio = s.get("pbRatio")?.takeIf { !it.isJsonNull }?.asDouble
                    divYield = s.get("divYield")?.takeIf { !it.isJsonNull }?.asDouble
                    epsTtm = s.get("epsTtm")?.takeIf { !it.isJsonNull }?.asDouble
                    roe = s.get("roe")?.takeIf { !it.isJsonNull }?.asDouble
                    debtToEquity = s.get("debtToEquity")?.takeIf { !it.isJsonNull }?.asDouble
                    faceValue = s.get("faceValue")?.takeIf { !it.isJsonNull }?.asDouble
                    netProfitMargin = s.get("netProfitMargin")?.takeIf { !it.isJsonNull }?.asDouble
                    operatingProfitMargin = s.get("operatingProfitMargin")?.takeIf { !it.isJsonNull }?.asDouble
                    bookValue = s.get("bookValue")?.takeIf { !it.isJsonNull }?.asDouble
                    industryPe = s.get("industryPe")?.takeIf { !it.isJsonNull }?.asDouble
                }

                sd.getAsJsonArray("fundamentals")?.let { arr ->
                    fundamentalItems = (0 until arr.size()).mapNotNull { i ->
                        val f = arr.get(i).asJsonObject
                        val n = f.get("name")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                        val sn = f.get("shortName")?.takeIf { !it.isJsonNull }?.asString ?: n
                        val v = f.get("value")?.takeIf { !it.isJsonNull }?.asString ?: "N/A"
                        FundamentalItem(n, sn, v)
                    }
                }

                sd.getAsJsonArray("brandDtos")?.let { arr ->
                    brandLogos = (0 until arr.size()).mapNotNull { i ->
                        val b = arr.get(i).asJsonObject
                        val n = b.get("name")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                        val l = b.get("logoUrl")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                        BrandInfo(n, l)
                    }.filter { it.logoUrl.startsWith("http") }
                }

                sd.getAsJsonArray("financialStatement")?.let { arr ->
                    var yr: Map<String, Double>? = null; var yp: Map<String, Double>? = null; var ynw: Map<String, Double>? = null
                    var qr: Map<String, Double>? = null; var qp: Map<String, Double>? = null
                    for (i in 0 until arr.size()) {
                        val f = arr.get(i).asJsonObject
                        val title = f.get("title")?.takeIf { !it.isJsonNull }?.asString ?: continue
                        val yearly = f.getAsJsonObject("yearly")
                        val quarterly = f.getAsJsonObject("quarterly")
                        when (title) {
                            "Revenue" -> { yr = yearly?.entrySet()?.associate { it.key to it.value.asDouble }; qr = quarterly?.entrySet()?.associate { it.key to it.value.asDouble } }
                            "Profit"  -> { yp = yearly?.entrySet()?.associate { it.key to it.value.asDouble }; qp = quarterly?.entrySet()?.associate { it.key to it.value.asDouble } }
                            "Net Worth" -> ynw = yearly?.entrySet()?.associate { it.key to it.value.asDouble }
                        }
                    }
                    financials = StockFinancials(yr, yp, ynw, qr, qp)
                }

                sd.getAsJsonObject("shareHoldingPattern")?.let { shp ->
                    val m = mutableMapOf<String, ShareholdingQuarter>()
                    for (q in shp.keySet()) {
                        val qo = shp.getAsJsonObject(q)
                        val pr = qo?.getAsJsonObject("promoters")?.getAsJsonObject("individual")?.get("percent")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                        val mf = qo?.getAsJsonObject("mutualFunds")?.get("percent")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                        val fi = qo?.getAsJsonObject("foreignInstitutions")?.get("percent")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                        val ins = qo?.getAsJsonObject("otherDomesticInstitutions")?.getAsJsonObject("insurance")?.get("percent")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                        val ret = qo?.getAsJsonObject("retailAndOthers")?.get("percent")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
                        m[q] = ShareholdingQuarter(pr, mf, fi, ins, ret)
                    }
                    if (m.isNotEmpty()) shareholding = m
                }

                sd.getAsJsonObject("similarAssets")?.getAsJsonArray("peerList")?.let { arr ->
                    val lpd = if (data.has("livePriceData")) data.getAsJsonObject("livePriceData") else null
                    stockPeers = (0 until arr.size()).mapNotNull { i ->
                        val po = arr.get(i).asJsonObject
                        val ch = po.getAsJsonObject("companyHeader") ?: return@mapNotNull null
                        val sid = ch.get("searchId")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                        val dn = ch.get("displayName")?.takeIf { !it.isJsonNull }?.asString ?: sid
                        val sn2 = ch.get("shortName")?.takeIf { !it.isJsonNull }?.asString ?: dn
                        val logo2 = ch.get("logoUrl")?.takeIf { !it.isJsonNull }?.asString
                        val mc2 = po.get("marketCap")?.takeIf { !it.isJsonNull }?.asDouble
                        val pe2 = po.get("peRatio")?.takeIf { !it.isJsonNull }?.asDouble
                        val pb2 = po.get("pbRatio")?.takeIf { !it.isJsonNull }?.asDouble
                        val nsc = ch.get("nseScriptCode")?.takeIf { !it.isJsonNull }?.asString
                        val bsc = ch.get("bseScriptCode")?.takeIf { !it.isJsonNull }?.asString
                        val psym = nsc ?: bsc
                        var pl: Double? = null; var pc: Double? = null; var pcp: Double? = null; var pyh: Double? = null; var pyl: Double? = null
                        if (lpd != null && psym != null && lpd.has(psym)) {
                            val pp = lpd.getAsJsonObject(psym)
                            pl = pp.get("ltp")?.takeIf { !it.isJsonNull }?.asDouble
                            pc = pp.get("dayChange")?.takeIf { !it.isJsonNull }?.asDouble
                            pcp = pp.get("dayChangePerc")?.takeIf { !it.isJsonNull }?.asDouble
                            pyh = pp.get("yearHighPrice")?.takeIf { !it.isJsonNull }?.asDouble
                            pyl = pp.get("yearLowPrice")?.takeIf { !it.isJsonNull }?.asDouble
                        }
                        StockPeerInfo(sid, dn, sn2, logo2, mc2, pe2, pb2, psym, pl, pc, pcp, pyh, pyl)
                    }
                }
            }

            data.getAsJsonArray("newsData")?.let { arr ->
                newsItems = (0 until arr.size()).mapNotNull { i ->
                    val n = arr.get(i).asJsonObject
                    val id = n.get("id")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                    val title = n.get("title")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                    StockNewsItem(id, title, n.get("url")?.takeIf { !it.isJsonNull }?.asString, n.get("pubDate")?.takeIf { !it.isJsonNull }?.asString, n.get("source")?.takeIf { !it.isJsonNull }?.asString)
                }
            }

            data.getAsJsonArray("eventsData")?.let { arr ->
                eventsItems = (0 until arr.size()).mapNotNull { i ->
                    val ev = arr.get(i).asJsonObject
                    val title = ev.get("eventTitle")?.takeIf { !it.isJsonNull }?.asString ?: return@mapNotNull null
                    val detailObj = ev.getAsJsonObject("eventDetail")
                    StockEventItem(title, ev.get("eventType")?.takeIf { !it.isJsonNull }?.asString, ev.get("primaryDate")?.takeIf { !it.isJsonNull }?.asString, ev.get("description")?.takeIf { !it.isJsonNull }?.asString, ev.get("corporateEventFilter")?.takeIf { !it.isJsonNull }?.asString, detailObj?.get("value")?.takeIf { !it.isJsonNull }?.asString, detailObj?.get("description")?.takeIf { !it.isJsonNull }?.asString)
                }
            }

            StockQuote(
                symbol = symbol, shortName = displayName, price = ltp, change = dayChange, changePercent = dayChangePerc,
                currency = "INR", open = openP, high = highP, low = lowP, previousClose = close,
                exchange = if (nseCode != null) "NSE" else if (bseCode != null) "BSE" else null,
                logoUrl = header.get("logoUrl")?.takeIf { !it.isJsonNull }?.asString,
                searchId = header.get("searchId")?.takeIf { !it.isJsonNull }?.asString,
                yearHigh = yearHigh, yearLow = yearLow,
                instrumentType = header.get("type")?.takeIf { !it.isJsonNull }?.asString,
                source = "groww.in", gsin = header.get("isin")?.takeIf { !it.isJsonNull }?.asString,
                aum = aum, expenseRatio = expenseRatio, trackingError = trackingError, nav = nav,
                return1M = return1M, return3M = return3M, return6M = return6M,
                return1Y = return1Y, returnAll = returnAll,
                description = description, peers = etfPeers,
                fundManagers = fundManagers, amc = etfAmc, foundationDate = foundationDate,
                benchmarkIndex = benchmarkIndex, etfCategory = etfCategory,
                fullName = fullName, headquarters = headquarters, ceo = ceo, foundedYear = foundedYear,
                businessSummary = businessSummary, websiteUrl = websiteUrl, industryName = industryName,
                cappedType = cappedType, marketCap = marketCap, peRatio = peRatio, pbRatio = pbRatio,
                divYield = divYield, epsTtm = epsTtm, roe = roe, debtToEquity = debtToEquity,
                faceValue = faceValue, netProfitMargin = netProfitMargin, operatingProfitMargin = operatingProfitMargin,
                bookValue = bookValue, industryPe = industryPe,
                fundamentals = fundamentalItems, stockPeers = stockPeers, financials = financials,
                newsItems = newsItems, eventsItems = eventsItems, shareholdingPattern = shareholding,
                brandLogos = brandLogos, isFnoEnabled = isFnoEnabled,
                nseScriptCode = nseCode, bseScriptCode = bseCode,
                volume = volume, totalBuyQty = totalBuyQty, totalSellQty = totalSellQty
            )
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

    private fun StockQuoteItem.toStockQuote(): StockQuote {
        val resolvedSymbol = nseScriptCode ?: bseScriptCode ?: symbol ?: ""
        val resolvedName = companyName ?: companyShortName ?: name ?: resolvedSymbol
        val resolvedPrice = ltp ?: price
        val resolvedChange = dayChange ?: change
        val resolvedChangePercent = dayChangePerc ?: changePercent.replace("%", "").replace("+", "").toDoubleOrNull() ?: 0.0
        
        return StockQuote(
            symbol = resolvedSymbol,
            shortName = resolvedName,
            price = resolvedPrice,
            change = resolvedChange,
            changePercent = resolvedChangePercent,
            currency = currency ?: "INR",
            open = open,
            high = high,
            low = low,
            previousClose = previousClose ?: close,
            exchange = exchange ?: if (nseScriptCode != null) "NSE" else if (bseScriptCode != null) "BSE" else null,
            latestTradingDay = latestTradingDay,
            logoUrl = logoUrl,
            searchId = searchId,
            yearHigh = yearHigh,
            yearLow = yearLow,
            instrumentType = instrumentType,
            source = source,
            gsin = gsin
        )
    }
}
