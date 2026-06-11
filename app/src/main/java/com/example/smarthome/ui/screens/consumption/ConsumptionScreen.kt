package com.example.smarthome.ui.screens.consumption

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
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.domain.deviceConsumptionW
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.navigation.Routes
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()
    val costoKwh by appViewModel.costoKwh.collectAsState()

    val allDevices = remember(devices) { devices.values.flatten() }
    val onDevices = remember(allDevices) { allDevices.filter { isDeviceOn(it.type.id, it.state) } }
    val totalW = remember(onDevices) { onDevices.sumOf { deviceConsumptionW(it.type.id) } }
    val kwhPerHour = totalW / 1000f
    val costoHora = costoKwh?.let { it * kwhPerHour }
    val costoDia = costoHora?.let { it * 24f }

    data class HomeConsumption(val name: String, val watts: Int, val activeDevices: Int)

    val perHome = remember(homes, rooms, devices) {
        homes.map { home ->
            val homeDevices = (rooms[home.id] ?: emptyList())
                .flatMap { devices[it.id] ?: emptyList() }
            val onHomeDevices = homeDevices.filter { isDeviceOn(it.type.id, it.state) }
            HomeConsumption(
                name = home.name,
                watts = onHomeDevices.sumOf { deviceConsumptionW(it.type.id) },
                activeDevices = onHomeDevices.size
            )
        }
    }

    val maxW = remember(perHome) { perHome.maxOfOrNull { it.watts } ?: 1 }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.appBarGradient(),
                title = {
                    Text(
                        stringResource(R.string.nav_consumption),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = stringResource(R.string.common_profile), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = gradientTopBarColors()
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Stats 2x2
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ConsumptionStatCard(
                        icon = Icons.Rounded.BarChart,
                        title = stringResource(R.string.consumption_current),
                        value = if (totalW >= 1000) "${"%.2f".format(totalW / 1000f)} kW"
                                else "$totalW W",
                        subtitle = stringResource(R.string.consumption_devices_on, onDevices.size),
                        modifier = Modifier.weight(1f)
                    )
                    ConsumptionStatCard(
                        icon = Icons.Rounded.Schedule,
                        title = stringResource(R.string.consumption_per_hour),
                        value = "${"%.3f".format(kwhPerHour)} kWh/h",
                        subtitle = stringResource(R.string.consumption_if_continues),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ConsumptionStatCard(
                        icon = Icons.Rounded.CalendarToday,
                        title = stringResource(R.string.consumption_cost_hour),
                        value = costoHora?.let { "$${"%.2f".format(it)}" } ?: "—",
                        subtitle = if (costoKwh != null) stringResource(R.string.consumption_by_tariff)
                                   else stringResource(R.string.consumption_no_tariff),
                        highlight = costoKwh != null,
                        modifier = Modifier.weight(1f)
                    )
                    ConsumptionStatCard(
                        icon = Icons.Rounded.CalendarToday,
                        title = stringResource(R.string.consumption_cost_day),
                        value = costoDia?.let { "$${"%.2f".format(it)}" } ?: "—",
                        subtitle = if (costoKwh != null) stringResource(R.string.consumption_if_24h)
                                   else stringResource(R.string.consumption_no_tariff),
                        highlight = costoKwh != null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Por casa
            if (perHome.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.consumption_per_home),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(perHome) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Apartment,
                                null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        item.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        if (item.watts >= 1000) "${"%.1f".format(item.watts / 1000f)} kW"
                                        else "${item.watts} W",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { if (maxW > 0) item.watts.toFloat() / maxW else 0f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    stringResource(R.string.consumption_devices_on, item.activeDevices),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            if (allDevices.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.consumption_no_devices), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumptionStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        colors = if (highlight) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) else CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    icon, null,
                    modifier = Modifier.size(16.dp),
                    tint = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.outline
                )
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.outline
            )
        }
    }
}
