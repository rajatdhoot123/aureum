package kwiktwik.ratewatch.app.data.model

import com.google.gson.Gson
import java.util.UUID

enum class AlertAssetType(val displayName: String) {
    GOLD_24K("Gold 24K (per 10g)"),
    GOLD_22K("Gold 22K (per 10g)"),
    SILVER("Silver (per kg)")
}

enum class AlertCondition(val displayName: String) {
    ABOVE("goes above"),
    BELOW("goes below")
}

data class PriceAlert(
    val id: String = UUID.randomUUID().toString(),
    val assetType: AlertAssetType,
    val condition: AlertCondition,
    val targetPrice: Double,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggeredAt: Long? = null
) {
    companion object {
        private val gson = Gson()

        fun fromJsonSet(jsonSet: Set<String>): List<PriceAlert> =
            jsonSet.mapNotNull { json ->
                runCatching { gson.fromJson(json, PriceAlert::class.java) }.getOrNull()
            }.sortedByDescending { it.createdAt }

        fun toJsonSet(alerts: List<PriceAlert>): Set<String> =
            alerts.map { gson.toJson(it) }.toSet()
    }

    fun isTriggered(currentPrice: Double): Boolean = when (condition) {
        AlertCondition.ABOVE -> currentPrice >= targetPrice
        AlertCondition.BELOW -> currentPrice <= targetPrice
    }
}