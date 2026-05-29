package com.example.smarthome.ui.screens.consumption

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.smarthome.ui.AppViewModel

@Composable
fun ConsumptionScreen(
    appViewModel: AppViewModel,
    navController: NavHostController,
    innerPadding: PaddingValues = PaddingValues()
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        Text("Consumo — Fase 5")
    }
}
