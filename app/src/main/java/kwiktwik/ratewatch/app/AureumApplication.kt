package kwiktwik.ratewatch.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AureumApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("Aureum", "=== Application onCreate ===")
    }
}
