package com.prathamchikitse.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.prathamchikitse.ui.chat.EmergencyChatScreen
import com.prathamchikitse.ui.detail.EmergencyDetailScreen
import com.prathamchikitse.ui.home.HomeScreen
import com.prathamchikitse.ui.hospitals.HospitalScreen
import com.prathamchikitse.ui.onboarding.LanguageOnboardingScreen
import com.prathamchikitse.ui.settings.SettingsScreen
import com.prathamchikitse.ui.sos.SOSScreen
import com.prathamchikitse.ui.splash.SplashScreen

/** Sealed navigation routes with type-safe argument helpers. */
sealed class Screen(val route: String) {
    data object Splash   : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Home     : Screen("home")
    data object Hospitals: Screen("hospitals")
    data object Settings : Screen("settings")
    data object SOS      : Screen("sos")

    data object EmergencyDetail : Screen("emergency/{emergencyId}") {
        fun createRoute(id: String) = "emergency/$id"
        const val ARG_ID = "emergencyId"
    }

    data object EmergencyChat : Screen("chat/{emergencyId}") {
        fun createRoute(id: String) = "chat/$id"
        const val ARG_ID = "emergencyId"
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    onLanguageChange: (String) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onShowOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onSplashComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            LanguageOnboardingScreen(
                onComplete = { languageCode ->
                    onLanguageChange(languageCode)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onEmergencyClick = { id ->
                    navController.navigate(Screen.EmergencyDetail.createRoute(id))
                },
                onHospitalsClick = { navController.navigate(Screen.Hospitals.route) },
                onSettingsClick  = { navController.navigate(Screen.Settings.route) },
                onSOSClick       = { navController.navigate(Screen.SOS.route) }
            )
        }

        composable(
            route = Screen.EmergencyDetail.route,
            arguments = listOf(
                navArgument(Screen.EmergencyDetail.ARG_ID) { type = NavType.StringType }
            )
        ) { backStack ->
            val id = backStack.arguments?.getString(Screen.EmergencyDetail.ARG_ID) ?: ""
            EmergencyDetailScreen(
                emergencyId = id,
                onBack = { navController.popBackStack() },
                onHospitalsClick = { navController.navigate(Screen.Hospitals.route) },
                onChatClick = { navController.navigate(Screen.EmergencyChat.createRoute(id)) }
            )
        }

        composable(
            route = Screen.EmergencyChat.route,
            arguments = listOf(
                navArgument(Screen.EmergencyChat.ARG_ID) { type = NavType.StringType }
            )
        ) { backStack ->
            val id = backStack.arguments?.getString(Screen.EmergencyChat.ARG_ID) ?: ""
            EmergencyChatScreen(
                emergencyId = id,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Hospitals.route) {
            HospitalScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLanguageChange = onLanguageChange
            )
        }

        composable(Screen.SOS.route) {
            SOSScreen(onBack = { navController.popBackStack() })
        }
    }
}
