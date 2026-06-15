package com.example.smarthome.ui.screens.consumption

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.domain.calculateConsumption
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.UserPreferencesViewModel
import com.example.smarthome.ui.components.AppClickablePanel
import com.example.smarthome.ui.components.AppEmptyPanel
import com.example.smarthome.ui.components.AppIconShape
import com.example.smarthome.ui.components.AppPanelShape
import com.example.smarthome.ui.components.AppScreenHorizontalPadding
import com.example.smarthome.ui.components.AppSectionHeader
import com.example.smarthome.ui.components.ConnectionErrorView
import com.example.smarthome.ui.components.FullScreenLoading
import com.example.smarthome.ui.navigation.Routes
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionScreen(
    appViewModel: AppViewModel,
    prefsViewModel: UserPreferencesViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()
    val powerByType by appViewModel.powerByType.collectAsState()
    val costoKwh by prefsViewModel.costoKwh.collectAsState()
    val isLoading by appViewModel.isLoading.collectAsState()
    val error by appViewModel.error.collectAsState()

    val allDevices = remember(devices) { devices.values.flatten() }
    val onDevices = remember(allDevices) { allDevices.filter { isDeviceOn(it.type.id, it.state) } }
    val consumption = remember(allDevices, powerByType, costoKwh) {
        calculateConsumption(allDevices, powerByType, costoKwh)
    }
    val totalW = consumption.watts
    val kwhPerHour = consumption.kwhPerHour
    val costoHora = consumption.costPerHour
    val costoDia = consumption.costPerDay

    data class DeviceConsumption(val name: String, val watts: Int, val isOn: Boolean)
    data class HomeConsumption(
        val id: String,
        val name: String,
        val watts: Int,
        val activeDevices: Int,
        val devices: List<DeviceConsumption>
    )

    // Id ficticio para agrupar los dispositivos que no pertenecen a ninguna casa real.
    val noHomeId = "__no_home__"
    val noHomeLabel = stringResource(R.string.homes_no_home)

    val perHome = remember(homes, rooms, devices, powerByType, noHomeLabel) {
        // Desglose por dispositivo: un dispositivo apagado consume 0 W.
        fun breakdown(devs: List<DeviceDto>): List<DeviceConsumption> =
            devs.map { d ->
                val on = isDeviceOn(d.type.id, d.state)
                DeviceConsumption(
                    name = d.name,
                    watts = if (on) (powerByType[d.type.id] ?: 0) else 0,
                    isOn = on
                )
            }.sortedWith(compareByDescending<DeviceConsumption> { it.isOn }.thenByDescending { it.watts })

        fun toHome(id: String, name: String, devs: List<DeviceDto>): HomeConsumption {
            val bd = breakdown(devs)
            return HomeConsumption(id, name, bd.sumOf { it.watts }, bd.count { it.isOn }, bd)
        }

        val realHomes = homes.map { home ->
            val homeDevices = (rooms[home.id] ?: emptyList())
                .flatMap { devices[it.id] ?: emptyList() }
            toHome(home.id, home.name, homeDevices)
        }

        // Dispositivos "sin casa": los que no están en ninguna habitación de una casa real
        // (incluye los libres y los de habitaciones sin casa).
        val homeRoomIds = homes.flatMap { rooms[it.id].orEmpty() }.map { it.id }.toSet()
        val noHomeDevices = devices.filterKeys { it !in homeRoomIds }.values.flatten()
        val noHomeEntry = if (noHomeDevices.isNotEmpty())
            toHome(noHomeId, noHomeLabel, noHomeDevices) else null

        realHomes + listOfNotNull(noHomeEntry)
    }

    val maxW = remember(perHome) { (perHome.maxOfOrNull { it.watts } ?: 0).coerceAtLeast(1) }

    var expandedId by remember { mutableStateOf<String?>(null) }

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
        if (!isLoading && homes.isEmpty() && error != null) {
            ConnectionErrorView(
                message = error.orEmpty(),
                onRetry = { appViewModel.retryLoad() },
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        if (isLoading && homes.isEmpty()) {
            FullScreenLoading(Modifier.padding(innerPadding))
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                start = AppScreenHorizontalPadding,
                end = AppScreenHorizontalPadding,
                bottom = 24.dp
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
                        value = "$totalW W",
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
                    AppSectionHeader(
                        title = stringResource(R.string.consumption_per_home),
                        icon = Icons.Rounded.Apartment
                    )
                }
                items(perHome) { item ->
                    val expanded = expandedId == item.id
                    AppClickablePanel(
                        onClick = { expandedId = if (expanded) null else item.id },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = AppIconShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (item.id == noHomeId) Icons.Rounded.Devices else Icons.Rounded.Apartment,
                                            null,
                                            modifier = Modifier.size(22.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "${item.watts} W",
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AnimatedVisibility(visible = expanded) {
                                Column(Modifier.fillMaxWidth()) {
                                    Spacer(Modifier.height(10.dp))
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    if (item.devices.isEmpty()) {
                                        Text(
                                            stringResource(R.string.consumption_home_empty),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        item.devices.forEach { dev ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 5.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = if (dev.isOn) MaterialTheme.colorScheme.primary
                                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                                        modifier = Modifier.size(8.dp)
                                                    ) {}
                                                    Text(
                                                        dev.name,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Text(
                                                    "${dev.watts} W",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (dev.isOn) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (dev.isOn) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.onSurfaceVariant
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

            if (allDevices.isEmpty()) {
                item {
                    AppEmptyPanel(
                        title = stringResource(R.string.consumption_no_devices),
                        icon = Icons.Rounded.BarChart
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsumptionStatCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = AppPanelShape,
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = AppIconShape,
                    color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon, null,
                            modifier = Modifier.size(18.dp),
                            tint = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (highlight) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
