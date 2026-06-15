package com.example.smarthome.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.domain.deviceTypeName
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.UserPreferencesViewModel
import com.example.smarthome.ui.components.AppEmptyPanel
import com.example.smarthome.ui.components.AppFabListBottomPadding
import com.example.smarthome.ui.components.AppMetricCard
import com.example.smarthome.ui.components.AppScreenHorizontalPadding
import com.example.smarthome.ui.components.AppSectionHeader
import com.example.smarthome.ui.components.DeviceGridCard
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import com.example.smarthome.ui.components.sheets.DeviceSheetActions
import com.example.smarthome.ui.components.sheets.DeviceSheetRouter
import com.example.smarthome.ui.navigation.Routes
import com.example.smarthome.ui.screens.devices.AddDeviceDialog
import com.example.smarthome.ui.theme.DangerRed
import com.example.smarthome.ui.theme.StatWarm
import com.example.smarthome.ui.theme.SuccessGreen
import androidx.compose.foundation.layout.WindowInsets

// SuccessGreen, DangerRed y StatWarm vienen del theme (ui/theme/Color.kt).
// StatNeutral es propio de esta pantalla.
private val StatNeutral = Color(0xFF666666)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    roomId: String,
    appViewModel: AppViewModel,
    prefsViewModel: UserPreferencesViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()
    val powerByType by appViewModel.powerByType.collectAsState()
    val standaloneRooms by appViewModel.standaloneRooms.collectAsState()
    val costoKwh by prefsViewModel.costoKwh.collectAsState()

    var selectedDevice by remember { mutableStateOf<DeviceDto?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val room = (rooms.values.flatten() + standaloneRooms).find { it.id == roomId }
    val homeId = rooms.entries.find { (_, list) -> list.any { it.id == roomId } }?.key
    val home = (homeId ?: room?.home?.id)?.let { hid -> homes.find { it.id == hid } }
    val roomDevices = devices[roomId] ?: emptyList()

    val totalOn = roomDevices.count { isDeviceOn(it.type.id, it.state) }
    val totalOff = roomDevices.size - totalOn
    val totalW = roomDevices.filter { isDeviceOn(it.type.id, it.state) }
        .sumOf { powerByType[it.type.id] ?: 0 }
    val costoHora = costoKwh?.let { (totalW / 1000f) * it }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.appBarGradient(),
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = stringResource(R.string.common_profile), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = gradientTopBarColors()
            )
        },
        floatingActionButton = {
            if (room != null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.onPrimary) },
                    text = {
                        Text(
                            stringResource(R.string.room_add_device),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 164.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                start = AppScreenHorizontalPadding,
                end = AppScreenHorizontalPadding,
                bottom = AppFabListBottomPadding
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val fullSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

            // Header como la web: casa, nombre y subtítulo.
            item(span = fullSpan) {
                Column {
                    // Como la web: casa chica arriba (breadcrumb), nombre de la
                    // habitación grande (h1) y subtítulo debajo.
                    if (home != null) {
                        Text(
                            home.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            room?.name ?: stringResource(R.string.common_room),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.common_edit_name),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.room_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats como la web: Encendidos / Apagados / Dispositivos / Consumo / Costo
            item(span = fullSpan) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RoomStatCardCompact(Icons.Rounded.PowerSettingsNew, SuccessGreen, "$totalOn", stringResource(R.string.common_on_plural), Modifier.weight(1f))
                        RoomStatCardCompact(Icons.Rounded.PowerSettingsNew, DangerRed, "$totalOff", stringResource(R.string.common_off_plural), Modifier.weight(1f), slashed = true)
                        RoomStatCardCompact(Icons.Rounded.Devices, StatNeutral, "${roomDevices.size}", stringResource(R.string.nav_devices), Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RoomStatCard(Icons.Rounded.Bolt, StatWarm, "$totalW W", stringResource(R.string.room_consumption_hour), Modifier.weight(1f))
                        RoomStatCard(
                            Icons.Rounded.AttachMoney, MaterialTheme.colorScheme.primary,
                            costoHora?.let { "$${"%.2f".format(it)}" } ?: "—",
                            stringResource(R.string.room_cost_hour),
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            // Sección de dispositivos
            item(span = fullSpan) {
                AppSectionHeader(
                    title = stringResource(R.string.nav_devices),
                    count = "${roomDevices.size}"
                )
            }

            if (roomDevices.isEmpty()) {
                item(span = fullSpan) {
                    AppEmptyPanel(title = stringResource(R.string.room_no_devices))
                }
            }

            items(roomDevices, key = { it.id }) { device ->
                DeviceGridCard(
                    device = device,
                    subtitle = stringResource(deviceTypeName(device.type.id)),
                    onToggle = { appViewModel.toggleDevice(device) },
                    onClick = { selectedDevice = device }
                )
            }
        }
    }

    if (showAddDialog && room != null) {
        AddDeviceDialog(
            appViewModel = appViewModel,
            onDismiss = { showAddDialog = false },
            onCreate = { name, typeId, roomId, marca, onResult ->
                appViewModel.createDevice(name, typeId, roomId, marca) { success, error ->
                    if (success) showAddDialog = false
                    onResult(success, error)
                }
            },
            lockedHome = home,
            lockedRoom = room
        )
    }

    if (showEditDialog && room != null) {
        EditRoomDialog(
            room = room,
            homes = homes,
            currentHomeId = homeId ?: home?.id,
            onDismiss = { showEditDialog = false },
            onSave = { name, type, homeId, onResult ->
                appViewModel.saveRoom(room, name, type, homeId) { ok, err ->
                    if (ok) showEditDialog = false
                    onResult(ok, err)
                }
            },
            onDelete = { onResult ->
                appViewModel.deleteRoomFreeingDevices(room) { ok, err ->
                    if (ok) {
                        showEditDialog = false
                        navController.popBackStack()
                    }
                    onResult(ok, err)
                }
            }
        )
    }

    selectedDevice?.let { device ->
        DeviceSheetRouter(
            device = device,
            onDismiss = { selectedDevice = null },
            actions = DeviceSheetActions(
                onExecuteAction = { action, params, cb -> appViewModel.executeDeviceAction(device.id, action, params, cb) },
                onRename = { newName, cb -> appViewModel.renameDevice(device.id, newName, cb) },
                onDelete = { cb -> appViewModel.deleteDevice(device.id, cb) },
                onLink = { roomId, cb -> appViewModel.linkDeviceToRoom(device.id, roomId, cb) },
                onUnlink = { cb -> appViewModel.linkDeviceToRoom(device.id, null, cb) },
                onLoad = { id -> appViewModel.loadDevice(id) }
            ),
            homes = homes,
            rooms = rooms
        )
    }
}

/** Variante compacta vertical para la fila de 3 (Encendidos / Apagados / Dispositivos). */
@Composable
private fun RoomStatCardCompact(
    icon: ImageVector,
    accent: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    slashed: Boolean = false
) {
    AppMetricCard(
        value = value,
        label = label,
        icon = icon,
        accent = accent,
        modifier = modifier,
        slashed = slashed,
        expanded = true
    )
}

/** Stat card al estilo de la web: ícono en cuadrado redondeado tintado + valor grande + etiqueta. */
@Composable
private fun RoomStatCard(
    icon: ImageVector,
    accent: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    AppMetricCard(
        value = value,
        label = label,
        icon = icon,
        accent = accent,
        modifier = modifier,
        expanded = true
    )
}
