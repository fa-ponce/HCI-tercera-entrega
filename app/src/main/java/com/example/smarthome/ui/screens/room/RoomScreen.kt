package com.example.smarthome.ui.screens.room

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.smarthome.domain.deviceConsumptionW
import com.example.smarthome.domain.deviceTypeName
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.UserPreferencesViewModel
import com.example.smarthome.ui.components.DeviceGridCard
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import com.example.smarthome.ui.components.sheets.DeviceSheetActions
import com.example.smarthome.ui.components.sheets.DeviceSheetRouter
import com.example.smarthome.ui.navigation.Routes
import com.example.smarthome.ui.screens.devices.AddDeviceDialog
import androidx.compose.foundation.layout.WindowInsets

// Acentos de las stat cards, mismos colores que la web (HabitacionView.vue)
private val StatGreen = Color(0xFF16A34A)
private val StatRed = Color(0xFFC0392B)
private val StatNeutral = Color(0xFF666666)
private val StatWarm = Color(0xFFC9A227)

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
        .sumOf { deviceConsumptionW(it.type.id) }
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
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 164.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 12.dp,
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            val fullSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

            // Header como la web: nombre + subtítulo y botones Agregar/Editar
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
                    Text(
                        room?.name ?: stringResource(R.string.common_room),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.room_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(11.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.room_add_device), fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.common_edit), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Stats como la web: Encendidos / Apagados / Dispositivos / Consumo / Costo
            item(span = fullSpan) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RoomStatCard(Icons.Rounded.Lightbulb, StatGreen, "$totalOn", stringResource(R.string.common_on_plural), Modifier.weight(1f))
                        RoomStatCard(Icons.Rounded.DoNotDisturbOn, StatRed, "$totalOff", stringResource(R.string.common_off_plural), Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RoomStatCard(Icons.Rounded.Devices, StatNeutral, "${roomDevices.size}", stringResource(R.string.nav_devices), Modifier.weight(1f))
                        RoomStatCard(Icons.Rounded.Bolt, StatWarm, "$totalW W", stringResource(R.string.room_consumption_hour), Modifier.weight(1f))
                    }
                    RoomStatCard(
                        Icons.Rounded.AttachMoney, MaterialTheme.colorScheme.primary,
                        costoHora?.let { "$${"%.2f".format(it)}" } ?: "—",
                        stringResource(R.string.room_cost_hour),
                        Modifier.fillMaxWidth()
                    )
                }
            }

            // Sección de dispositivos
            item(span = fullSpan) {
                Text(
                    stringResource(R.string.nav_devices),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (roomDevices.isEmpty()) {
                item(span = fullSpan) {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.room_no_devices),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                appViewModel.createDevice(name, typeId, roomId, marca) { success ->
                    if (success) showAddDialog = false
                    onResult(success)
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

/** Stat card al estilo de la web: ícono en cuadrado redondeado tintado + valor grande + etiqueta. */
@Composable
private fun RoomStatCard(
    icon: ImageVector,
    accent: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(11.dp))
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
