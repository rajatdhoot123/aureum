package kwiktwik.ratewatch.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import kwiktwik.ratewatch.app.worker.PriceAlertWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AureumApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.d("Aureum", "=== Application onCreate ===")
        createNotificationChannel()
        schedulePriceAlertWork()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PriceAlertWorker.CHANNEL_ID,
                "Price Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when prices hit your target"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun schedulePriceAlertWork() {
        // Android enforces a 15-minute minimum for periodic background work.
        val request = PeriodicWorkRequestBuilder<PriceAlertWorker>(15, TimeUnit.MINUTES)
            .addTag("price_alert_check")
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "price_alert_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        Log.d("Aureum", "Price alert periodic work scheduled")
    }
}
