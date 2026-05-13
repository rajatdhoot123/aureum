package kwiktwik.ratewatch.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface YahooFinanceApi {
    @GET("v7/finance/quote")
    suspend fun getQuotes(@Query("symbols") symbols: String): YahooQuoteResponse
}

data class YahooQuoteResponse(
    val quoteResponse: QuoteResponse?
)

data class QuoteResponse(
    val result: List<YahooQuote>?,
    val error: Any?
)

data class YahooQuote(
    val symbol: String?,
    @SerializedName("shortName") val shortName: String?,
    @SerializedName("regularMarketPrice") val regularMarketPrice: Double?,
    @SerializedName("regularMarketChange") val regularMarketChange: Double?,
    @SerializedName("regularMarketChangePercent") val regularMarketChangePercent: Double?,
    @SerializedName("currency") val currency: String?
)
