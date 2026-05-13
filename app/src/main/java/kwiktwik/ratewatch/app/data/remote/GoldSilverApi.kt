package kwiktwik.ratewatch.app.data.remote

import kwiktwik.ratewatch.app.data.model.GoldSilverResponse
import retrofit2.http.GET

interface GoldSilverApi {
    @GET("scraper/metals/latest")
    suspend fun getLatestPrices(): GoldSilverResponse
}
