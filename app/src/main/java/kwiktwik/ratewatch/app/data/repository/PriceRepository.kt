package kwiktwik.ratewatch.app.data.repository

import kwiktwik.ratewatch.app.data.model.*
import kwiktwik.ratewatch.app.data.remote.RetrofitClient
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

    fun getStockQuotes(symbols: List<String>): Flow<Result<List<StockQuote>>> = flow {
        if (symbols.isEmpty()) {
            emit(Result.success(emptyList()))
            return@flow
        }
        val result = runCatching {
            val csv = symbols.joinToString(",")
            val response = RetrofitClient.yahooFinanceApi.getQuotes(csv)
            response.quoteResponse?.result?.mapNotNull { yahoo ->
                if (yahoo.symbol == null || yahoo.regularMarketPrice == null) return@mapNotNull null
                StockQuote(
                    symbol = yahoo.symbol,
                    shortName = yahoo.shortName ?: yahoo.symbol,
                    price = yahoo.regularMarketPrice,
                    change = yahoo.regularMarketChange ?: 0.0,
                    changePercent = yahoo.regularMarketChangePercent ?: 0.0,
                    currency = yahoo.currency ?: "INR"
                )
            } ?: emptyList()
        }
        emit(result)
    }

    suspend fun getAllPopularStockQuotes(): Result<List<StockQuote>> = withContext(Dispatchers.IO) {
        runCatching {
            val symbols = PopularStocks.all.map { it.symbol }
            val csv = symbols.joinToString(",")
            val response = RetrofitClient.yahooFinanceApi.getQuotes(csv)
            response.quoteResponse?.result?.mapNotNull { yahoo ->
                if (yahoo.symbol == null || yahoo.regularMarketPrice == null) return@mapNotNull null
                StockQuote(
                    symbol = yahoo.symbol,
                    shortName = yahoo.shortName ?: yahoo.symbol,
                    price = yahoo.regularMarketPrice,
                    change = yahoo.regularMarketChange ?: 0.0,
                    changePercent = yahoo.regularMarketChangePercent ?: 0.0
                )
            } ?: emptyList()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(it) }
        )
    }
}
