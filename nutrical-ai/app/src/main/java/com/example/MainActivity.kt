package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CalculationScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NutriViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NutriViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                NutriCalApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NutriCalApp(viewModel: NutriViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                AppScreen.AUTH -> AuthScreen(
                    onAuthSuccess = { email, isSignUp ->
                        viewModel.authenticate(email, isSignUp)
                    }
                )
                AppScreen.ONBOARDING -> OnboardingScreen(
                    viewModel = viewModel,
                    onFinish = {
                        viewModel.nextOnboardingStep()
                    }
                )
                AppScreen.CALCULATING -> CalculationScreen(
                    viewModel = viewModel
                )
                AppScreen.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel
                )
                AppScreen.SCANNER -> ScannerScreen(
                    viewModel = viewModel
                )
                AppScreen.HISTORY -> HistoryScreen(
                    viewModel = viewModel
                )
                AppScreen.PROFILE -> ProfileScreen(
                    viewModel = viewModel
                )
                AppScreen.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
