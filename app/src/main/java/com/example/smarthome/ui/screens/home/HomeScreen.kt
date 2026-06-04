package com.example.smarthome.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.LogEntryDto
import com.example.smarthome.domain.deviceConsumptionW
import com.example.smarthome.domain.deviceIcon
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.navigation.Routes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()
    val routines by appViewModel.routines.collectAsState()
    val isLoading by appViewModel.isLoading.collectAsState()
    val userName by appViewModel.userName.collectAsState()

    val allDevices = remember(devices) { devices.values.flatten() }
    val onDevices = remember(allDevices) { allDevices.filter { isDeviceOn(it.type.id, it.state) } }
    val offDevices = remember(allDevices) { allDevices.filter { !isDeviceOn(it.type.id, it.state) } }
    val totalW = remember(onDevices) { onDevices.sumOf { deviceConsumptionW(it.type.id) } }
    val deviceById = remember(allDevices) { allDevices.associateBy { it.id } }

    var recentLogs by remember { mutableStateOf<List<LogEntryDto>>(emptyList()) }
    LaunchedEffect(Unit) {
        ServiceLocator.historyRepository.getLogs(10, 0).onSuccess { recentLogs = it }
    }

    val saludo = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 6..11 -> "Buenos días"
            in 12..19 -> "Buenas tardes"
            else -> "Buenas noches"
        }
    }
    val nombre = userName?.split(" ")?.firstOrNull() ?: "Usuario"
    val fecha = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "AR"))
            .format(Date()).replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "SmartHome",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3A5A90)
                )
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        if (isLoading && homes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Saludo
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "$saludo, $nombre",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        fecha,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StatChip(
                            value = "${homes.size}",
                            label = "Casas",
                            icon = Icons.Rounded.Apartment,
                            onClick = { navController.navigate(Routes.HOMES) }
                        )
                    }
                    item {
                        StatChip(
                            value = "${rooms.values.sumOf { it.size }}",
                            label = "Habitaciones",
                            icon = Icons.Rounded.MeetingRoom,
                            onClick = { navController.navigate(Routes.HOMES) }
                        )
                    }
                    item {
                        StatChip(
                            value = "${onDevices.size}",
                            label = "Dispositivos",
                            icon = Icons.Rounded.Devices,
                            onClick = { navController.navigate(Routes.DEVICES) }
                        )
                    }
                    item {
                        StatChip(
                            value = if (totalW >= 1000) "${"%.1f".format(totalW / 1000f)} kW" else "$totalW W",
                            label = "Consumo",
                            icon = Icons.Rounded.BarChart,
                            onClick = { navController.navigate(Routes.CONSUMPTION) }
                        )
                    }
                }
            }

            // Rutinas rápidas
            if (routines.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Rutinas rápidas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(100.dp)
                        ) {
                            items(routines.take(6)) { routine ->
                                val tipo = routine.metadata?.tipoTrigger ?: "manual"
                                Card(
                                    onClick = { appViewModel.executeRoutine(routine.id) },
                                    modifier = Modifier
                                        .width(110.dp)
                                        .fillMaxHeight(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = when (tipo) {
                                                "hora" -> Icons.Rounded.Schedule
                                                else -> Icons.Rounded.PlayArrow
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            routine.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 2,
                                            fontWeight = FontWeight.Medium,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Estado de dispositivos
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Estado de dispositivos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (allDevices.isNotEmpty()) {
                            androidx.compose.material3.TextButton(
                                onClick = { navController.navigate(Routes.DEVICES) }
                            ) { Text("Ver todos") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "${onDevices.size}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Encendidos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (onDevices.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    onDevices.take(3).forEach { dev ->
                                        Text(
                                            "• ${dev.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    if (onDevices.size > 3) {
                                        Text(
                                            "+ ${onDevices.size - 3} más",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "${offDevices.size}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Apagados",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (offDevices.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    offDevices.take(3).forEach { dev ->
                                        Text(
                                            "• ${dev.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (offDevices.size > 3) {
                                        Text(
                                            "+ ${offDevices.size - 3} más",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Actividad reciente
            if (recentLogs.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Actividad reciente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            androidx.compose.material3.TextButton(
                                onClick = { navController.navigate(Routes.HISTORY) }
                            ) { Text("Ver historial") }
                        }
                        Spacer(Modifier.height(8.dp))
                        Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                            Column(Modifier.padding(vertical = 4.dp)) {
                                recentLogs.take(5).forEach { log ->
                                    val (_, hora) = formatTimestamp(log.timestamp)
                                    val device = deviceById[log.deviceId]
                                    val deviceName = device?.name ?: "Dispositivo"
                                    val typeId = device?.type?.id ?: ""
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = deviceIcon(typeId),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                deviceName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                translateAction(log.event),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            hora,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(110.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(ts: String?): Pair<String, String> {
    if (ts == null) return Pair("—", "—")
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(ts) ?: return Pair(ts.take(10), "")
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        Pair(SimpleDateFormat("dd/MM", Locale.getDefault()).format(date), timeFmt.format(date))
    } catch (e: Exception) {
        Pair(ts.take(10), "")
    }
}

private fun translateAction(event: String?): String {
    val map = mapOf(
        "turnOn" to "Encendido", "turnOff" to "Apagado",
        "open" to "Abierto", "close" to "Cerrado",
        "armAway" to "Armado", "disarm" to "Desarmado",
        "play" to "Reproduciendo", "stop" to "Detenido",
        "startCleaning" to "Limpiando", "dock" to "En base",
        "up" to "Subido", "down" to "Bajado",
        "lock" to "Bloqueado", "unlock" to "Desbloqueado",
        "start" to "Iniciado", "pause" to "Pausado"
    )
    return map[event] ?: event ?: "—"
}
