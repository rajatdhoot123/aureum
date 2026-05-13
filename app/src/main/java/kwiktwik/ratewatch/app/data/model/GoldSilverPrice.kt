package kwiktwik.ratewatch.app.data.model

import com.google.gson.annotations.SerializedName

data class GoldSilverResponse(
    val success: Boolean = true,
    val data: List<CityPrice> = emptyList(),
    @SerializedName("lastScrapeAt") val lastScrapeAt: String? = null,
    val source: String? = null
)

data class CityPrice(
    val city: String = "",
    @SerializedName("gold22kPer10g") val gold22kPer10g: Int? = null,
    @SerializedName("gold24kPer10g") val gold24kPer10g: Int? = null,
    @SerializedName("silverPerKg") val silverPerKg: Int? = null,
    @SerializedName("gold22kChange") val gold22kChange: String? = null,
    @SerializedName("gold24kChange") val gold24kChange: String? = null,
    @SerializedName("silverChange") val silverChange: String? = null,
    @SerializedName("scrapedAt") val scrapedAt: String? = null
)
