package com.example.smarthome.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smarthome.ui.screens.login.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = Routes.LOGIN) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Fase 5 reemplaza este stub con HomeScreen
        composable(Routes.HOME) {
            Text("Home")
        }
    }
}
