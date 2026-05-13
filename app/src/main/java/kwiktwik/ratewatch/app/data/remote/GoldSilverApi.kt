package kwiktwik.ratewatch.app.data.remote

import kwiktwik.ratewatch.app.data.model.GoldSilverResponse
import retrofit2.http.GET
import retrofit2.http.POST

interface GoldSilverApi {
    /**
     * Latest city-wise gold (22K/24K) & silver prices from Goodreturns.in.
     * Supported paths on the unified scraper: /metals/latest, /gold/latest, /metal/latest, /scraper/metals/latest
     */
    @GET("scraper/metals/latest")
    suspend fun getLatestPrices(): GoldSilverResponse

    /**
     * Force an immediate scrape of gold/silver rates (bypasses schedule).
     * Rate limit recommendation: do not call more than once every 5 minutes.
     */
    @POST("scraper/metals/scrape")
    suspend fun triggerScrape(): GoldSilverResponse
}
