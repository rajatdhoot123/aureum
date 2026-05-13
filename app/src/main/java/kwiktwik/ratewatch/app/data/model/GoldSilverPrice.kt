package kwiktwik.ratewatch.app.data.model

import com.google.gson.annotations.SerializedName

data class GoldSilverResponse(
    val success: Boolean,
    val data: List<CityPrice>,
    @SerializedName("lastScrapeAt") val lastScrapeAt: String?,
    val source: String?
)

data class CityPrice(
    val city: String,
    @SerializedName("gold22kPer10g") val gold22kPer10g: Int?,
    @SerializedName("gold24kPer10g") val gold24kPer10g: Int?,
    @SerializedName("silverPerKg") val silverPerKg: Int?,
    @SerializedName("gold22kChange") val gold22kChange: String?,
    @SerializedName("gold24kChange") val gold24kChange: String?,
    @SerializedName("silverChange") val silverChange: String?,
    @SerializedName("scrapedAt") val scrapedAt: String?
)
