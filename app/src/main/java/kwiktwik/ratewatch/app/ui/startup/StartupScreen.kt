package kwiktwik.ratewatch.app.ui.startup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kwiktwik.ratewatch.app.ui.navigation.Screen

@Composable
fun StartupScreen(
    navController: NavController,
    viewModel: StartupViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        val completed = viewModel.isOnboardingCompleted()
        if (completed) {
            navController.navigate(Screen.Home.route) {
                popUpTo("startup") { inclusive = true }
            }
        } else {
            navController.navigate("onboarding") {
                popUpTo("startup") { inclusive = true }
            }
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
