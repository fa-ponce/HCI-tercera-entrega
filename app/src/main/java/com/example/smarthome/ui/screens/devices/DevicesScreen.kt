package com.example.smarthome.ui.screens.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DevicesOther
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.components.AppFabListBottomPadding
import com.example.smarthome.ui.components.AppMetricCard
import com.example.smarthome.ui.components.AppScreenHorizontalPadding
import com.example.smarthome.ui.components.AppSearchField
import com.example.smarthome.ui.components.ConnectionErrorView
import com.example.smarthome.ui.components.DeviceGridCard
import com.example.smarthome.ui.components.FullScreenLoading
import com.example.smarthome.ui.components.sheets.DeviceSheetActions
import com.example.smarthome.ui.components.sheets.DeviceSheetRouter
import com.example.smarthome.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()
    val standaloneRooms by appViewModel.standaloneRooms.collectAsState()
    val isLoading by appViewModel.isLoading.collectAsState()
    val error by appViewModel.error.collectAsState()

    var search by remember { mutableStateOf("") }
    var homeFilter by remember { mutableStateOf<String?>(null) }
    var roomFilter by remember { mutableStateOf<String?>(null) }
    var showFilter by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf<DeviceDto?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Lista plana con contexto de ubicación de cada dispositivo.
    data class DeviceItem(
        val device: DeviceDto,
        val homeName: String,
        val roomName: String,
        val roomId: String
    )

    val freeLabel = stringResource(R.string.device_free_label)
    val noHomeLabel = stringResource(R.string.homes_no_home)
    val allItems = remember(homes, rooms, devices, standaloneRooms) {
        val roomItems = homes.flatMap { home ->
            (rooms[home.id] ?: emptyList()).flatMap { room ->
                (devices[room.id] ?: emptyList()).map { device ->
                    DeviceItem(device, home.name, room.name, room.id)
                }
            }
        }
        // Dispositivos en habitaciones sin casa.
        val standaloneItems = standaloneRooms.flatMap { room ->
            (devices[room.id] ?: emptyList()).map { device ->
                DeviceItem(device, noHomeLabel, room.name, room.id)
            }
        }
        val freeItems = (devices["free"] ?: emptyList()).map { device ->
            DeviceItem(device, "", freeLabel, "free")
        }
        roomItems + standaloneItems + freeItems
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

    if (showAddDialog) {
        AddDeviceDialog(
            appViewModel = appViewModel,
            onDismiss = { showAddDialog = false },
            onCreate = { name, typeId, roomId, marca, onResult ->
                appViewModel.createDevice(name, typeId, roomId, marca) { success, error ->
                    if (success) showAddDialog = false
                    onResult(success, error)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.appBarGradient(),
                title = {
                    Text(
                        stringResource(R.string.nav_devices),
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.onPrimary) },
                text = {
                    Text(
                        stringResource(R.string.device_new),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        if (!isLoading && allItems.isEmpty() && error != null) {
            ConnectionErrorView(
                message = error.orEmpty(),
                onRetry = { appViewModel.retryLoad() },
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }
        if (isLoading && allItems.isEmpty()) {
            FullScreenLoading(Modifier.padding(innerPadding))
            return@Scaffold
        }
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

            // Resumen general (ancho completo), con los acentos de la web:
            // total azul, encendidos verde, apagados rojo.
            item(span = fullSpan) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill("${allItems.size}", stringResource(R.string.device_total), Icons.Rounded.DevicesOther, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    StatPill("$totalOn", stringResource(R.string.common_on_plural), Icons.Rounded.PowerSettingsNew, OnGreen, Modifier.weight(1f))
                    StatPill("$totalOff", stringResource(R.string.common_off_plural), Icons.Rounded.PowerSettingsNew, MaterialTheme.colorScheme.error, Modifier.weight(1f), slashed = true)
                }
            }

            // Búsqueda + filtro (ancho completo)
            item(span = fullSpan) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AppSearchField(
                            value = search,
                            onValueChange = { search = it },
                            placeholder = stringResource(R.string.device_search_placeholder),
                            modifier = Modifier.weight(1f)
                        )
                        FilterButton(
                            expanded = showFilter,
                            onExpandedChange = { showFilter = it },
                            homes = homes.map { it.name },
                            rooms = availableRooms.map { it.name },
                            homeFilter = homeFilter,
                            roomFilter = roomFilter,
                            onSelectHome = { homeFilter = it; roomFilter = null },
                            onSelectRoom = { roomFilter = it },
                            onClear = { homeFilter = null; roomFilter = null }
                        )
                    }

                    // Chips de filtros activos, removibles individualmente.
                    val activeChips = listOfNotNull(
                        homeFilter?.let { "casa" to it },
                        roomFilter?.let { "room" to it }
                    )
                    if (activeChips.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeChips.forEach { (kind, label) ->
                                FilterChipPill(
                                    icon = if (kind == "casa") Icons.Rounded.Home else Icons.Rounded.MeetingRoom,
                                    label = label,
                                    onRemove = {
                                        if (kind == "casa") { homeFilter = null; roomFilter = null }
                                        else roomFilter = null
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item(span = fullSpan) { EmptyState() }
            }

            items(filtered, key = { it.device.id }) { item ->
                val free = item.roomId == "free"
                val subtitle = when {
                    free -> stringResource(R.string.device_no_room)
                    item.homeName.isNotEmpty() -> "${item.homeName} · ${item.roomName}"
                    else -> item.roomName
                }
                DeviceGridCard(
                    device = item.device,
                    subtitle = subtitle,
                    free = free,
                    onToggle = { appViewModel.toggleDevice(item.device) },
                    onClick = { selectedDevice = item.device }
                )
            }
        }
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

/** Verde "encendido" compartido con la web (#16a34a). */
private val OnGreen = Color(0xFF16A34A)

@Composable
private fun StatPill(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
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

/**
 * Botón de filtro mejorado: un botón cuadrado relleno que se tiñe con el color
 * primario y muestra una insignia con la cantidad de filtros activos. Abre un
 * menú organizado por casa y habitación, con marcas de verificación.
 */
@Composable
private fun FilterButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    homes: List<String>,
    rooms: List<String>,
    homeFilter: String?,
    roomFilter: String?,
    onSelectHome: (String) -> Unit,
    onSelectRoom: (String?) -> Unit,
    onClear: () -> Unit
) {
    val activeCount = listOfNotNull(homeFilter, roomFilter).size
    val active = activeCount > 0

    Box {
        Surface(
            onClick = { onExpandedChange(!expanded) },
            shape = RoundedCornerShape(16.dp),
            color = if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                BadgedBox(
                    badge = {
                        if (active) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) { Text("$activeCount") }
                        }
                    }
                ) {
                    Icon(
                        Icons.Rounded.FilterList,
                        contentDescription = stringResource(R.string.device_filter),
                        tint = if (active) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            MenuLabel(stringResource(R.string.common_home))
            DropdownMenuItem(
                text = { Text(stringResource(R.string.device_all_homes)) },
                leadingIcon = { Icon(Icons.Rounded.Home, null) },
                trailingIcon = { if (homeFilter == null) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary) },
                onClick = { onClear(); onExpandedChange(false) }
            )
            homes.forEach { home ->
                DropdownMenuItem(
                    text = { Text(home) },
                    leadingIcon = { Icon(Icons.Rounded.Home, null) },
                    trailingIcon = { if (homeFilter == home) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = { onSelectHome(home); onExpandedChange(false) }
                )
            }

            if (homeFilter != null && rooms.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                MenuLabel(stringResource(R.string.common_room))
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.device_all_rooms)) },
                    trailingIcon = { if (roomFilter == null) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary) },
                    onClick = { onSelectRoom(null); onExpandedChange(false) }
                )
                rooms.forEach { room ->
                    DropdownMenuItem(
                        text = { Text(room) },
                        leadingIcon = { Icon(Icons.Rounded.MeetingRoom, null) },
                        trailingIcon = { if (roomFilter == room) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = { onSelectRoom(room); onExpandedChange(false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun FilterChipPill(
    icon: ImageVector,
    label: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.size(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Rounded.Clear,
                    stringResource(R.string.device_filter_remove),
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.SearchOff,
                    null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.size(14.dp))
        Text(
            stringResource(R.string.device_none_found),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.size(4.dp))
        Text(
            stringResource(R.string.device_adjust_filters),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
