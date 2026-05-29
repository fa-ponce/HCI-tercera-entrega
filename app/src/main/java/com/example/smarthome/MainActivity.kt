package com.example.smarthome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.navigation.NavGraph
import com.example.smarthome.ui.navigation.Routes
import com.example.smarthome.ui.theme.SmarthomeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmarthomeTheme {
                val navController = rememberNavController()
                val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
                var startDestination by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val token = ServiceLocator.userPreferences.getTokenOnce()
                    if (token != null) {
                        appViewModel.loadAll()
                        appViewModel.subscribeRealtime()
                        startDestination = Routes.HOME
                    } else {
                        startDestination = Routes.LOGIN
                    }
                }

                startDestination?.let { dest ->
                    NavGraph(
                        navController = navController,
                        appViewModel = appViewModel,
                        startDestination = dest
                    )
                }
            }
        }
    }
}
