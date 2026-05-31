package com.example.smarthome.ui.screens.homes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.domain.deviceConsumptionW
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.navigation.Routes
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDetailScreen(
    homeId: String,
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()

    val home = homes.find { it.id == homeId }
    val homeRooms = rooms[homeId] ?: emptyList()

    val allHomeDevices = homeRooms.flatMap { devices[it.id] ?: emptyList() }
    val totalOn = allHomeDevices.count { isDeviceOn(it.type.id, it.state) }
    val totalW = allHomeDevices.filter { isDeviceOn(it.type.id, it.state) }
        .sumOf { deviceConsumptionW(it.type.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(home?.name ?: "Casa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, "Volver")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Info de la casa
            item {
                val subtitle = listOfNotNull(
                    home?.metadata?.type,
                    home?.metadata?.city,
                    home?.metadata?.address
                ).joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBox(
                        value = "${homeRooms.size}",
                        label = "Habitaciones",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        value = "$totalOn",
                        label = "Encendidos",
                        highlight = true,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        value = if (totalW >= 1000) "${"%.1f".format(totalW / 1000f)}kW" else "${totalW}W",
                        label = "Consumo",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Habitaciones
            if (homeRooms.isNotEmpty()) {
                item {
                    Text(
                        "Habitaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                item {
                    // Grid 2 columnas dentro de LazyColumn
                    val chunked = homeRooms.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        chunked.forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                pair.forEach { room ->
                                    val roomDevices = devices[room.id] ?: emptyList()
                                    val roomOn = roomDevices.count { isDeviceOn(it.type.id, it.state) }
                                    Card(
                                        onClick = { navController.navigate(Routes.room(room.id)) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(Modifier.padding(14.dp)) {
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        Icons.Rounded.MeetingRoom, null,
                                                        Modifier.size(20.dp),
                                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(10.dp))
                                            Text(
                                                room.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Rounded.Devices, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                                                Text(
                                                    "${roomDevices.size} disp. · $roomOn enc.",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }
                                }
                                // Relleno si el par tiene solo 1 elemento
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text("Esta casa no tiene habitaciones.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
