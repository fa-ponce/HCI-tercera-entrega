package com.example.smarthome.ui.screens.devices

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.components.DeviceCard
import androidx.compose.foundation.layout.WindowInsets
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.ui.components.sheets.DeviceSheetRouter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()

    var search by remember { mutableStateOf("") }
    var homeFilter by remember { mutableStateOf<String?>(null) }
    var roomFilter by remember { mutableStateOf<String?>(null) }
    var showFilter by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<DeviceDto?>(null) }

    // Construir lista plana con contexto
    data class DeviceItem(
        val device: com.example.smarthome.data.api.models.DeviceDto,
        val homeName: String,
        val roomName: String,
        val roomId: String
    )

    val allItems = remember(homes, rooms, devices) {
        homes.flatMap { home ->
            (rooms[home.id] ?: emptyList()).flatMap { room ->
                (devices[room.id] ?: emptyList()).map { device ->
                    DeviceItem(device, home.name, room.name, room.id)
                }
            }
        }
    }

    val filtered = remember(allItems, search, homeFilter, roomFilter) {
        allItems.filter { item ->
            val matchesSearch = search.isBlank() ||
                item.device.name.contains(search, ignoreCase = true) ||
                item.homeName.contains(search, ignoreCase = true) ||
                item.roomName.contains(search, ignoreCase = true)
            val matchesHome = homeFilter == null || item.homeName == homeFilter
            val matchesRoom = roomFilter == null || item.roomName == roomFilter
            matchesSearch && matchesHome && matchesRoom
        }
    }

    val totalOn = allItems.count { isDeviceOn(it.device.type.id, it.device.state) }
    val totalOff = allItems.size - totalOn

    val availableRooms = remember(rooms, homeFilter) {
        if (homeFilter == null) rooms.values.flatten()
        else {
            val homeId = homes.find { it.name == homeFilter }?.id ?: return@remember emptyList()
            rooms[homeId] ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispositivos") },
                actions = {
                    Box {
                        IconButton(onClick = { showFilter = !showFilter }) {
                            Icon(Icons.Rounded.FilterList, "Filtrar")
                        }
                        DropdownMenu(
                            expanded = showFilter,
                            onDismissRequest = { showFilter = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todas las casas") },
                                onClick = { homeFilter = null; roomFilter = null; showFilter = false }
                            )
                            homes.forEach { home ->
                                DropdownMenuItem(
                                    text = { Text(home.name) },
                                    onClick = { homeFilter = home.name; roomFilter = null; showFilter = false }
                                )
                            }
                            if (homeFilter != null) {
                                availableRooms.forEach { room ->
                                    DropdownMenuItem(
                                        text = { Text("  ${room.name}") },
                                        onClick = { roomFilter = room.name; showFilter = false }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Stats
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("${allItems.size}", "Total", Modifier.weight(1f))
                    StatPill(
                        "$totalOn", "Encendidos",
                        Modifier.weight(1f),
                        highlight = true
                    )
                    StatPill("$totalOff", "Apagados", Modifier.weight(1f))
                }
            }

            // Búsqueda + filtro activo
            item {
                Column {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Buscar dispositivo…") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        trailingIcon = if (search.isNotEmpty()) {
                            { IconButton(onClick = { search = "" }) { Icon(Icons.Rounded.Clear, "Limpiar") } }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val activeFilter = listOfNotNull(homeFilter, roomFilter).joinToString(" › ")
                    if (activeFilter.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Filtro: $activeFilter",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            IconButton(
                                onClick = { homeFilter = null; roomFilter = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Rounded.Clear, "Quitar filtro", Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No se encontraron dispositivos.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(filtered) { item ->
                val subtitle = "${item.homeName} · ${item.roomName}"
                DeviceCard(
                    device = item.device,
                    subtitle = subtitle,
                    onToggle = { appViewModel.toggleDevice(item.device) },
                    onClick = { selectedDevice = item.device }
                )
            }
        }
    }

    selectedDevice?.let { device ->
        DeviceSheetRouter(
            device = device,
            onDismiss = { selectedDevice = null }
        )
    }
}

@Composable
private fun StatPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = if (highlight) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) else CardDefaults.cardColors()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.outline
            )
        }
    }
}
