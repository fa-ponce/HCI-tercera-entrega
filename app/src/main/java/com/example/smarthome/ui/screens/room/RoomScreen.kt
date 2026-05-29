package com.example.smarthome.ui.screens.room

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.smarthome.ui.AppViewModel

@Composable
fun RoomScreen(
    roomId: String,
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Habitación $roomId — Fase 5")
    }
}
