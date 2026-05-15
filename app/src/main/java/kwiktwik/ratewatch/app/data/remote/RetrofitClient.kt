package kwiktwik.ratewatch.app.data.remote

import kwiktwik.ratewatch.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds OkHttp + Retrofit instances for the app's APIs.
 * Instances are provided via Hilt in [kwiktwik.ratewatch.app.di.AppModule].
 */
object RetrofitClient {

    private fun buildBaseUrl(raw: String): String =
        if (raw.endsWith("/")) "${raw}scraper/" else "$raw/scraper/"

    fun buildOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(AppIdInterceptor(BuildConfig.APPLICATION_ID))
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Sonar-Android/${BuildConfig.VERSION_NAME}")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun buildGoldSilverApi(client: OkHttpClient): GoldSilverApi =
        Retrofit.Builder()
            .baseUrl(buildBaseUrl(BuildConfig.GOLD_SILVER_BASE_URL))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoldSilverApi::class.java)

    fun buildStocksApi(client: OkHttpClient): StocksApi =
        Retrofit.Builder()
            .baseUrl(buildBaseUrl(BuildConfig.STOCK_API_BASE_URL))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StocksApi::class.java)
}
