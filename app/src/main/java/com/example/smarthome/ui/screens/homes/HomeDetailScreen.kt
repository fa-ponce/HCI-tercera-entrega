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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.domain.roomTypeIcon
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.components.AppClickablePanel
import com.example.smarthome.ui.components.AppFabListBottomPadding
import com.example.smarthome.ui.components.AppIconShape
import com.example.smarthome.ui.components.AppMetricCard
import com.example.smarthome.ui.components.AppScreenHorizontalPadding
import com.example.smarthome.ui.components.AppSectionHeader
import com.example.smarthome.ui.components.DeviceGridCard
import com.example.smarthome.ui.components.sheets.DeviceSheetActions
import com.example.smarthome.ui.components.sheets.DeviceSheetRouter
import com.example.smarthome.ui.navigation.Routes
import com.example.smarthome.ui.theme.StatWarm
import com.example.smarthome.ui.theme.SuccessGreen
import androidx.compose.foundation.layout.WindowInsets

@Composable
fun HomeDetailScreen(
    homeId: String,
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    HomeDetailContent(
        homeId = homeId,
        appViewModel = appViewModel,
        navController = navController,
        showBackButton = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDetailContent(
    homeId: String,
    appViewModel: AppViewModel,
    navController: NavHostController,
    showBackButton: Boolean = true,
    onHomeDeleted: () -> Unit = { navController.popBackStack() }
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()
    val powerByType by appViewModel.powerByType.collectAsState()

    val home = homes.find { it.id == homeId }
    val homeRooms = rooms[homeId] ?: emptyList()

    // Dispositivos de la casa con el nombre de su habitación, para mostrarlos en grilla.
    val homeDeviceItems = homeRooms.flatMap { room ->
        (devices[room.id] ?: emptyList()).map { device -> device to room.name }
    }
    val allHomeDevices = homeDeviceItems.map { it.first }
    val totalOn = allHomeDevices.count { isDeviceOn(it.type.id, it.state) }
    val totalW = allHomeDevices.filter { isDeviceOn(it.type.id, it.state) }
        .sumOf { powerByType[it.type.id] ?: 0 }

    var selectedDevice by remember { mutableStateOf<DeviceDto?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var isRenameSaving by remember { mutableStateOf(false) }
    var isDeleteSaving by remember { mutableStateOf(false) }
    var renameError by remember { mutableStateOf<String?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.appBarGradient(),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            home?.name ?: stringResource(R.string.common_home),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = { renameText = home?.name ?: ""; showRenameDialog = true }) {
                            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.common_edit_name), tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                        }
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Rounded.ArrowBack, stringResource(R.string.common_back), tint = MaterialTheme.colorScheme.onPrimary)
                        }
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
            ExtendedFloatingActionButton(
                onClick = { showAddRoomDialog = true },
                icon = { Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.onPrimary) },
                text = { Text(stringResource(R.string.homes_new_room), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary
            )
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

            // Info de la casa
            item(span = fullSpan) {
                val subtitle = listOfNotNull(
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
            item(span = fullSpan) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBox(
                        value = "${homeRooms.size}",
                        label = stringResource(R.string.common_rooms),
                        icon = Icons.Rounded.MeetingRoom,
                        accent = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        value = "$totalOn",
                        label = stringResource(R.string.common_on_plural),
                        icon = Icons.Rounded.PowerSettingsNew,
                        accent = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        value = "${totalW} W",
                        label = stringResource(R.string.nav_consumption),
                        icon = Icons.Rounded.Bolt,
                        accent = StatWarm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Habitaciones
            if (homeRooms.isNotEmpty()) {
                item(span = fullSpan) {
                    SectionTitle(Icons.Rounded.MeetingRoom, stringResource(R.string.common_rooms), homeRooms.size)
                }
                items(homeRooms, key = { it.id }) { room ->
                    val roomDevices = devices[room.id] ?: emptyList()
                    val roomOn = roomDevices.count { isDeviceOn(it.type.id, it.state) }
                    RoomGridCard(
                        name = room.name,
                        roomType = room.metadata?.type,
                        deviceCount = roomDevices.size,
                        onCount = roomOn,
                        offCount = roomDevices.size - roomOn,
                        onClick = { navController.navigate(Routes.room(room.id)) }
                    )
                }
            } else {
                item(span = fullSpan) {
                    Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.homes_no_rooms), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Dispositivos de la casa
            if (homeDeviceItems.isNotEmpty()) {
                item(span = fullSpan) {
                    SectionTitle(Icons.Rounded.Devices, stringResource(R.string.nav_devices), homeDeviceItems.size)
                }
                items(homeDeviceItems, key = { it.first.id }) { (device, roomName) ->
                    DeviceGridCard(
                        device = device,
                        subtitle = roomName,
                        onToggle = { appViewModel.toggleDevice(device) },
                        onClick = { selectedDevice = device }
                    )
                }
            }

            item(span = fullSpan) {
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.homes_delete_home), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
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

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.homes_rename_home), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it; renameError = null },
                        label = { Text(stringResource(R.string.common_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (renameError != null) {
                        Text(renameError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = renameText.trim()
                        if (newName.isNotEmpty() && home != null && !isRenameSaving) {
                            isRenameSaving = true
                            renameError = null
                            scope.launch {
                                ServiceLocator.homeRepository.updateHome(
                                    id = homeId,
                                    name = newName,
                                    type = home.metadata?.type ?: "",
                                    address = home.metadata?.address ?: "",
                                    city = home.metadata?.city ?: ""
                                ).onSuccess { updated ->
                                    appViewModel.updateHome(updated)
                                    showRenameDialog = false
                                }.onFailure { renameError = it.message }
                                isRenameSaving = false
                            }
                        }
                    },
                    enabled = !isRenameSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isRenameSaving) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.common_save), color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }, enabled = !isRenameSaving) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.homes_delete_home), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.common_delete_confirm, home?.name ?: ""))
                    if (deleteError != null) {
                        Text(deleteError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isDeleteSaving) {
                            isDeleteSaving = true
                            deleteError = null
                            scope.launch {
                                ServiceLocator.homeRepository.deleteHome(homeId)
                                    .onSuccess {
                                        appViewModel.removeHome(homeId)
                                        onHomeDeleted()
                                    }
                                    .onFailure { deleteError = it.message }
                                isDeleteSaving = false
                            }
                        }
                    },
                    enabled = !isDeleteSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleteSaving) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.common_delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !isDeleteSaving) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            homeId = homeId,
            appViewModel = appViewModel,
            onDismiss = { showAddRoomDialog = false }
        )
    }
}

// Los tipos de habitación viven en ROOM_TYPES (NewRoomDialog.kt), mismo paquete.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRoomDialog(
    homeId: String,
    appViewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf(ROOM_TYPES[0]) }
    var typeExpanded by remember { mutableStateOf(false) }
    var floor by remember { mutableStateOf(1) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val existingRooms by appViewModel.rooms.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.homes_new_room), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text(stringResource(R.string.common_name_required)) },
                    placeholder = { Text(stringResource(R.string.homes_room_name_placeholder)) },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { msg -> { Text(msg) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Tipo
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = roomTypeLabel(selectedType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.common_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        ROOM_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(roomTypeLabel(type)) },
                                onClick = { selectedType = type; typeExpanded = false }
                            )
                        }
                    }
                }

                // Piso
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.common_floor), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = { if (floor > 1) floor-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text(
                        "$floor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { floor++ },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }

                if (saveError != null) {
                    Text(saveError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            val errMin = stringResource(R.string.common_name_min_chars)
            val errMax = stringResource(R.string.common_name_max_chars)
            val errDup = stringResource(R.string.err_already_exists)
            Button(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.length < 3 -> { nameError = errMin; return@Button }
                        trimmed.length > 100 -> { nameError = errMax; return@Button }
                    }
                    // No permitimos otra habitación con el mismo nombre en esta casa.
                    if ((existingRooms[homeId] ?: emptyList()).any { it.name.equals(trimmed, ignoreCase = true) }) {
                        nameError = errDup; return@Button
                    }
                    if (!isSaving) {
                        isSaving = true
                        saveError = null
                        scope.launch {
                            ServiceLocator.homeRepository.createRoom(trimmed, selectedType, floor, homeId)
                                .onSuccess { room ->
                                    appViewModel.addRoom(homeId, room)
                                    onDismiss()
                                }
                                .onFailure { saveError = it.message }
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.common_create), color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String, count: Int) {
    AppSectionHeader(
        title = title,
        icon = icon,
        count = "$count",
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun RoomGridCard(
    name: String,
    roomType: String?,
    deviceCount: Int,
    onCount: Int,
    offCount: Int,
    onClick: () -> Unit
) {
    AppClickablePanel(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = AppIconShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        roomTypeIcon(roomType), null,
                        Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DeviceStatusBadge("$onCount", SuccessGreen, Icons.Rounded.PowerSettingsNew)
                DeviceStatusBadge("$offCount", MaterialTheme.colorScheme.error, Icons.Rounded.PowerSettingsNew, slashed = true)
                DeviceStatusBadge("$deviceCount", MaterialTheme.colorScheme.primary, Icons.Rounded.Devices)
            }
        }
    }
}

@Composable
private fun DeviceStatusBadge(
    text: String,
    accent: Color,
    icon: ImageVector,
    slashed: Boolean = false
) {
    Surface(shape = com.example.smarthome.ui.components.AppPillShape, color = accent.copy(alpha = 0.14f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(14.dp)) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(13.dp))
                if (slashed) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(15.dp)) {
                        drawLine(
                            color = accent,
                            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.82f),
                            end = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.18f),
                            strokeWidth = 1.7.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = accent)
        }
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    AppMetricCard(value = value, label = label, icon = icon, accent = accent, modifier = modifier)
}
