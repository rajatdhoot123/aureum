package kwiktwik.ratewatch.app.ui.startup

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun StartupScreen(
    navController: NavController,
    viewModel: StartupViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        Log.d("RateWatch", "StartupScreen: LaunchedEffect started, reading onboarding status...")
        try {
            val completed = viewModel.isOnboardingCompleted()
            Log.d("RateWatch", "StartupScreen: Got value = $completed, navigating...")
            
            if (completed) {
                navController.navigate("home") {
                    popUpTo("startup") { inclusive = true }
                }
            } else {
                navController.navigate("onboarding") {
                    popUpTo("startup") { inclusive = true }
                }
            }
        } catch (e: Exception) {
            Log.e("RateWatch", "StartupScreen: ERROR while reading onboarding status", e)
        }
    }

    // Brief loading indicator while reading preferences from disk
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
