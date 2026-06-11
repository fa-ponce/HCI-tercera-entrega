package com.example.smarthome

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.runBlocking
import java.util.Locale

class MainActivity : ComponentActivity() {

    companion object {
        // Idioma del teléfono, capturado antes de cualquier override manual.
        private val systemLocale: Locale = Locale.getDefault()
    }

    /**
     * Aplica el idioma elegido manualmente (si lo hay) al contexto base de la
     * Activity. Hacerlo acá -y no solo en Compose- garantiza que también lo
     * hereden los diálogos y bottom sheets, que crean su propia ventana a
     * partir del contexto de la Activity. null = idioma del teléfono (RNF1).
     * Al cambiar el idioma desde el perfil se recrea la Activity.
     */
    override fun attachBaseContext(newBase: Context) {
        val language = runBlocking { ServiceLocator.userPreferences.getAppLanguageOnce() }
        if (language == null) {
            Locale.setDefault(systemLocale)
            super.attachBaseContext(newBase)
        } else {
            val locale = Locale.forLanguageTag(language)
            Locale.setDefault(locale)
            val config = Configuration(newBase.resources.configuration).apply { setLocale(locale) }
            super.attachBaseContext(newBase.createConfigurationContext(config))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)
            val darkMode by appViewModel.darkMode.collectAsState()
            SmarthomeTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }

                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* El resultado no afecta el flujo: NotificationHelper ya valida el permiso. */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

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
