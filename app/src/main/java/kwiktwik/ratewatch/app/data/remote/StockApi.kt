package kwiktwik.ratewatch.app.data.remote

import kwiktwik.ratewatch.app.data.model.StockApiResponse
import retrofit2.http.GET

interface StockApi {
    @GET("scraper/stock/latest")
    suspend fun getLatestStockPrices(): StockApiResponse
}