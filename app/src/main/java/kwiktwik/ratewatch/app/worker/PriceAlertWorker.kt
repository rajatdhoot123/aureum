package kwiktwik.ratewatch.app.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kwiktwik.ratewatch.app.R
import kwiktwik.ratewatch.app.data.model.AlertAssetType
import kwiktwik.ratewatch.app.data.model.PriceAlert
import kwiktwik.ratewatch.app.data.remote.GoldSilverApi
import kwiktwik.ratewatch.app.data.repository.PreferencesRepository

@HiltWorker
class PriceAlertWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val prefs: PreferencesRepository,
    private val goldSilverApi: GoldSilverApi
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "PriceAlertWorker"
        const val CHANNEL_ID = "price_alerts"
        private var notificationId = 1000
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Price alert check started")

        val alerts = prefs.getAlerts().filter { it.enabled }
        if (alerts.isEmpty()) {
            Log.d(TAG, "No active alerts, skipping")
            return Result.success()
        }

        // Fetch current gold/silver prices
        val prices = try {
            goldSilverApi.getLatestPrices()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch prices", e)
            return Result.retry()
        }

        val cityPrice = prices.data.firstOrNull() ?: return Result.success()
        val updatedAlerts = mutableListOf<PriceAlert>()

        for (alert in alerts) {
            val currentPrice = when (alert.assetType) {
                AlertAssetType.GOLD_24K -> cityPrice.gold24kPer10g?.toDouble()
                AlertAssetType.GOLD_22K -> cityPrice.gold22kPer10g?.toDouble()
                AlertAssetType.SILVER -> cityPrice.silverPerKg?.toDouble()
            }

            if (currentPrice != null && alert.isTriggered(currentPrice)) {
                sendNotification(alert, currentPrice)
                // Mark as triggered and disable to avoid repeat notifications
                updatedAlerts.add(alert.copy(enabled = false, lastTriggeredAt = System.currentTimeMillis()))
            } else {
                updatedAlerts.add(alert)
            }
        }

        // Persist any updates (triggered alerts get disabled)
        val untouchedAlerts = prefs.getAlerts().filter { existing -> alerts.none { it.id == existing.id } }
        prefs.updateAlerts(untouchedAlerts + updatedAlerts)

        Log.d(TAG, "Price alert check completed")
        return Result.success()
    }

    private fun sendNotification(alert: PriceAlert, currentPrice: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val title = "${alert.assetType.displayName} Alert"
        val body = "${alert.assetType.displayName} ${alert.condition.displayName} " +
                "₹${"%,.0f".format(alert.targetPrice)}! Current: ₹${"%,.0f".format(currentPrice)}"

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(notificationId++, notification)
    }

}