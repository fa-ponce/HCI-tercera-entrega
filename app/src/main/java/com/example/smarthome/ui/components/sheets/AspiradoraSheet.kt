package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AspiradoraSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    var status by remember { mutableStateOf("docked") }
    var mode by remember { mutableStateOf("vacuum") }
    var selectedAction by remember { mutableStateOf("start") }
    var isSaving by remember { mutableStateOf(false) }
    var locationRoomId by remember { mutableStateOf<String?>(actions.onGetVacuumPrefs?.invoke(device.id)?.first) }
    var dockingRoomId by remember { mutableStateOf<String?>(actions.onGetVacuumPrefs?.invoke(device.id)?.second) }

    val statusLabels = mapOf(
        "active" to R.string.action_cleaning, "paused" to R.string.action_paused,
        "docked" to R.string.action_docked, "inactive" to R.string.sheet_inactive
    )
    val modoOpts = listOf("vacuum" to R.string.sheet_vacuum, "mop" to R.string.sheet_mop)

    val allRooms = remember(rooms) { rooms.values.flatten() }
    val noRoomLabel = stringResource(R.string.sheet_no_room_assigned)

    LaunchedEffect(device.id) {
        actions.onLoadVacuumPrefsAsync?.invoke(device.id)?.let { (loc, dock) ->
            if (locationRoomId == null) locationRoomId = loc
            if (dockingRoomId == null) dockingRoomId = dock
        }
    }

    val isLoading = rememberDeviceLoading(device, routineMode, actions) { d ->
        status = (d.state["status"] as? String) ?: "docked"
        mode = (d.state["mode"] as? String) ?: "vacuum"
        selectedAction = if (status == "docked" || status == "inactive") "start" else "dock"
    }

    BaseDeviceSheet(
        device = device,
        routineMode = routineMode,
        onDismiss = onDismiss,
        actions = actions,
        homes = homes,
        rooms = rooms,
        isLoading = isLoading,
        onAddToRoutine = if (routineMode) {
            {
                val routineActions = mutableListOf(
                    DeviceAction(selectedAction),
                    DeviceAction("setMode", mapOf("mode" to mode))
                )
                locationRoomId?.let { id ->
                    val name = allRooms.find { it.id == id }?.name ?: ""
                    routineActions += DeviceAction("setLocation", mapOf("room" to mapOf("id" to id, "name" to name)))
                }
                dockingRoomId?.let { id ->
                    val name = allRooms.find { it.id == id }?.name ?: ""
                    routineActions += DeviceAction("setDockingStation", mapOf("room" to mapOf("id" to id, "name" to name)))
                }
                onAddToRoutine?.invoke(routineActions)
                onDismiss()
            }
        } else null
    ) {
        if (!routineMode) {
            val statusColor = when (status) {
                "active" -> MaterialTheme.colorScheme.primaryContainer
                "paused" -> MaterialTheme.colorScheme.tertiaryContainer
                "docked" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Surface(color = statusColor, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                Text(
                    statusLabels[status]?.let { stringResource(it) } ?: status,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        SheetSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetSectionLabel(stringResource(R.string.sheet_controls))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    val startActive = if (routineMode) selectedAction == "start" else status == "active"
                    Surface(
                        onClick = {
                            if (routineMode) {
                                selectedAction = "start"
                            } else if (!isSaving && status != "active") {
                                isSaving = true
                                actions.onExecuteAction("start", emptyMap()) { _ ->
                                    status = "active"; selectedAction = "start"; isSaving = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        color = if (startActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (startActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        enabled = !isSaving
                    ) {
                        Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.sheet_start), style = MaterialTheme.typography.labelLarge, fontWeight = if (startActive) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    if (!routineMode) {
                        Surface(
                            onClick = {
                                if (!isSaving && status == "active") {
                                    isSaving = true
                                    actions.onExecuteAction("pause", emptyMap()) { _ ->
                                        status = "paused"; isSaving = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            color = if (status == "paused") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (status == "paused") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            enabled = !isSaving && status == "active"
                        ) {
                            Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                                Text(stringResource(R.string.sheet_pause), style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    val dockActive = if (routineMode) selectedAction == "dock" else status == "docked"
                    Surface(
                        onClick = {
                            if (routineMode) {
                                selectedAction = "dock"
                            } else if (!isSaving && status != "docked") {
                                isSaving = true
                                actions.onExecuteAction("dock", emptyMap()) { _ ->
                                    status = "docked"; selectedAction = "dock"; isSaving = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        color = if (dockActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (dockActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        enabled = !isSaving
                    ) {
                        Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.sheet_dock), style = MaterialTheme.typography.labelMedium, fontWeight = if (dockActive) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        SheetSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetSectionLabel(stringResource(R.string.sheet_mode))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    modoOpts.forEach { (value, labelRes) ->
                        FilterChip(
                            selected = mode == value,
                            onClick = {
                                mode = value
                                if (!routineMode) actions.onExecuteAction("setMode", mapOf("mode" to value), null)
                            },
                            label = { Text(stringResource(labelRes)) },
                            modifier = Modifier.weight(1f),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = mode == value, selectedBorderColor = MaterialTheme.colorScheme.primary, selectedBorderWidth = 1.5.dp)
                        )
                    }
                }
            }
        }

        if (allRooms.isNotEmpty()) {
            SheetSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SheetSectionLabel(stringResource(R.string.common_rooms))

                    RoomDropdown(
                        label = stringResource(R.string.sheet_vacuum_location),
                        selectedRoomId = locationRoomId,
                        rooms = allRooms,
                        noRoomLabel = noRoomLabel,
                        onSelect = { room ->
                            locationRoomId = room.id
                            if (!routineMode) {
                                actions.onSaveVacuumPrefs?.invoke(device.id, room.id, dockingRoomId)
                            }
                        }
                    )

                    RoomDropdown(
                        label = stringResource(R.string.sheet_vacuum_dock_room),
                        selectedRoomId = dockingRoomId,
                        rooms = allRooms,
                        noRoomLabel = noRoomLabel,
                        onSelect = { room ->
                            dockingRoomId = room.id
                            if (!routineMode) {
                                actions.onSaveVacuumPrefs?.invoke(device.id, locationRoomId, room.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomDropdown(
    label: String,
    selectedRoomId: String?,
    rooms: List<RoomDto>,
    noRoomLabel: String,
    onSelect: (RoomDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = rooms.find { it.id == selectedRoomId }?.name ?: noRoomLabel

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            rooms.forEach { room ->
                DropdownMenuItem(
                    text = { Text(room.name) },
                    onClick = {
                        onSelect(room)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
