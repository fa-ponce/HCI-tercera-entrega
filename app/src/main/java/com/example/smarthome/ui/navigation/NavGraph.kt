package com.example.smarthome.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.screens.consumption.ConsumptionScreen
import com.example.smarthome.ui.screens.devices.DevicesScreen
import com.example.smarthome.ui.screens.history.HistoryScreen
import com.example.smarthome.ui.screens.home.HomeScreen
import com.example.smarthome.ui.screens.homes.HomeDetailScreen
import com.example.smarthome.ui.screens.homes.HomesScreen
import com.example.smarthome.ui.screens.login.LoginScreen
import com.example.smarthome.ui.screens.room.RoomScreen
import com.example.smarthome.ui.screens.profile.ProfileScreen
import com.example.smarthome.ui.screens.routines.RoutinesScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val HOMES = "homes"
    const val HOME_DETAIL = "home_detail/{homeId}"
    const val ROOM = "room/{roomId}"
    const val DEVICES = "devices"
    const val ROUTINES = "routines"
    const val CONSUMPTION = "consumption"
    const val HISTORY = "history"
    const val PROFILE = "profile"

    fun homeDetail(homeId: String) = "home_detail/$homeId"
    fun room(roomId: String) = "room/$roomId"
}

private data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Inicio", Icons.Rounded.Home),
    BottomNavItem(Routes.HOMES, "Casas", Icons.Rounded.Apartment),
    BottomNavItem(Routes.DEVICES, "Dispositivos", Icons.Rounded.Devices),
    BottomNavItem(Routes.ROUTINES, "Rutinas", Icons.Rounded.Schedule),
    BottomNavItem(Routes.CONSUMPTION, "Consumo", Icons.Rounded.BarChart),
)

private val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun NavGraph(
    navController: NavHostController,
    appViewModel: AppViewModel,
    startDestination: String = Routes.LOGIN
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavRoutes || currentRoute == Routes.HOME_DETAIL) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route ||
                                (currentRoute == Routes.HOME_DETAIL && item.route == Routes.HOMES),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.HOME) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Only apply bottom padding from outer scaffold; each screen's TopAppBar handles top insets
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        appViewModel.loadAll()
                        appViewModel.subscribeRealtime()
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(appViewModel = appViewModel, navController = navController)
            }

            composable(Routes.HOMES) {
                HomesScreen(appViewModel = appViewModel, navController = navController)
            }

            composable(
                route = Routes.HOME_DETAIL,
                arguments = listOf(navArgument("homeId") { type = NavType.StringType })
            ) { backStack ->
                HomeDetailScreen(
                    homeId = backStack.arguments?.getString("homeId") ?: "",
                    appViewModel = appViewModel,
                    navController = navController
                )
            }

            composable(
                route = Routes.ROOM,
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStack ->
                RoomScreen(
                    roomId = backStack.arguments?.getString("roomId") ?: "",
                    appViewModel = appViewModel,
                    navController = navController
                )
            }

            composable(Routes.DEVICES) {
                DevicesScreen(appViewModel = appViewModel, navController = navController)
            }

            composable(Routes.ROUTINES) {
                RoutinesScreen(appViewModel = appViewModel, navController = navController)
            }

            composable(Routes.CONSUMPTION) {
                ConsumptionScreen(appViewModel = appViewModel, navController = navController)
            }

            composable(Routes.HISTORY) {
                HistoryScreen(appViewModel = appViewModel, navController = navController)
            }

            composable(Routes.PROFILE) {
                ProfileScreen(appViewModel = appViewModel, navController = navController)
            }
        }
    }
}
