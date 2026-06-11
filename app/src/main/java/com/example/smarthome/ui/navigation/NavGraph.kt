package com.example.smarthome.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.smarthome.R
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

private data class BottomNavItem(val route: String, @StringRes val labelRes: Int, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, R.string.nav_home, Icons.Rounded.Home),
    BottomNavItem(Routes.HOMES, R.string.nav_homes, Icons.Rounded.Apartment),
    BottomNavItem(Routes.DEVICES, R.string.nav_devices, Icons.Rounded.Devices),
    BottomNavItem(Routes.ROUTINES, R.string.nav_routines, Icons.Rounded.Schedule),
    BottomNavItem(Routes.CONSUMPTION, R.string.nav_consumption, Icons.Rounded.BarChart),
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        appViewModel.errorEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        appViewModel.forceLogout.collect {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // El login no muestra navegación; el resto deja que el componente adaptativo
    // elija barra (teléfono) / rail (landscape) / drawer (tablet) según el ancho.
    val showNav = currentRoute in bottomNavRoutes ||
        currentRoute == Routes.HOME_DETAIL || currentRoute == Routes.ROOM
    val layoutType = if (!showNav) {
        NavigationSuiteType.None
    } else {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    }

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            bottomNavItems.forEach { item ->
                item(
                    icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                    label = { Text(stringResource(item.labelRes)) },
                    selected = currentRoute == item.route ||
                        ((currentRoute == Routes.HOME_DETAIL || currentRoute == Routes.ROOM) && item.route == Routes.HOMES),
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(Routes.HOME) { saveState = false }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination
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

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) { data ->
                Snackbar(snackbarData = data)
            }
        }
    }
}
