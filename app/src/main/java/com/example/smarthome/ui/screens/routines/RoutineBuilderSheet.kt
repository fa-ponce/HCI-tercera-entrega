package com.example.smarthome.ui.screens.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.DeviceRef
import com.example.smarthome.data.api.models.RoutineActionDto
import com.example.smarthome.data.api.models.RoutineDto
import com.example.smarthome.data.api.models.RoutineMetaDto
import com.example.smarthome.data.api.models.RoutineRequest
import com.example.smarthome.domain.deviceIcon
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.components.sheets.DeviceAction
import com.example.smarthome.ui.components.sheets.DeviceSheetRouter
import kotlinx.coroutines.launch

// ── State for one selected device entry ──────────────────────────────────────

class SelectedEntry(
    val device: DeviceDto,
    val homeName: String,
    val roomName: String,
    actions: List<DeviceAction>,
    duration: String = ""
) {
    var actions: List<DeviceAction> by mutableStateOf(actions)
    var duration: String by mutableStateOf(duration)
}

// ── Action label map ──────────────────────────────────────────────────────────

private val ACTION_LABEL_RES = mapOf(
    "turnOn" to R.string.routine_action_turn_on, "turnOff" to R.string.routine_action_turn_off,
    "up" to R.string.routine_action_open, "down" to R.string.routine_action_close,
    "setLevel" to R.string.routine_action_level,
    "open" to R.string.routine_action_open, "close" to R.string.routine_action_close,
    "lock" to R.string.routine_action_lock, "unlock" to R.string.routine_action_unlock,
    "armAway" to R.string.routine_action_arm, "armStay" to R.string.sheet_mode_home,
    "disarm" to R.string.sheet_disarm,
    "start" to R.string.sheet_start, "dock" to R.string.sheet_dock,
    "play" to R.string.sheet_play, "stop" to R.string.sheet_stop,
    "setTemperature" to R.string.routine_action_temp,
    "setFreezerTemperature" to R.string.routine_action_freezer_temp,
    "setMode" to R.string.sheet_mode, "setFanSpeed" to R.string.routine_action_speed,
    "setVerticalSwing" to R.string.routine_action_vblades,
    "setHorizontalSwing" to R.string.routine_action_hblades,
    "setBrightness" to R.string.routine_action_brightness, "setColor" to R.string.sheet_color,
    "setHeat" to R.string.routine_action_heat, "setGrill" to R.string.routine_action_grill,
    "setConvection" to R.string.routine_action_convection,
    "setVolume" to R.string.sheet_volume, "setGenre" to R.string.sheet_genre,
    "dispense" to R.string.sheet_dispense
)

@Composable
private fun labelActions(actions: List<DeviceAction>): String {
    val context = LocalContext.current
    return actions.take(3).joinToString(" · ") { a ->
        val base = ACTION_LABEL_RES[a.actionName]?.let { context.getString(it) } ?: a.actionName
        when {
            a.params.isEmpty() -> base
            "temperature" in a.params -> "$base ${a.params["temperature"]}°C"
            "brightness" in a.params -> "$base ${a.params["brightness"]}%"
            "level" in a.params -> "$base ${a.params["level"]}%"
            "volume" in a.params -> "$base ${a.params["volume"]}"
            "color" in a.params -> base
            else -> "$base: ${a.params.values.firstOrNull()}"
        }
    }
}

// ── Main composable ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineBuilderSheet(
    routine: RoutineDto? = null,
    appViewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val routineRepo = remember { ServiceLocator.routineRepository }

    val isEditing = routine != null

    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()

    // Flat device lookup: deviceId → DeviceDto
    val deviceById = remember(devices) {
        devices.values.flatten().associateBy { it.id }
    }

    // Flat device list with context for search
    data class DeviceItem(val device: DeviceDto, val homeName: String, val roomName: String)

    val noRoomLabel = stringResource(R.string.device_no_room)
    val allDevices = remember(homes, rooms, devices) {
        val inRooms = homes.flatMap { home ->
            (rooms[home.id] ?: emptyList()).flatMap { room ->
                (devices[room.id] ?: emptyList()).map { device ->
                    DeviceItem(device, home.name, room.name)
                }
            }
        }
        val free = (devices["free"] ?: emptyList()).map { device ->
            DeviceItem(device, "", noRoomLabel)
        }
        inRooms + free
    }

    // ── Form state ────────────────────────────────────────────────────────────
    var name by remember { mutableStateOf(routine?.name ?: "") }
    var description by remember { mutableStateOf(routine?.metadata?.descripcion ?: "") }
    var tipoTrigger by remember { mutableStateOf(
        if (routine?.metadata?.tipoTrigger == "hora") "hora" else "manual"
    )}
    var hora by remember { mutableStateOf(
        if (routine?.metadata?.tipoTrigger == "hora") (routine.metadata?.trigger ?: "08:00") else "08:00"
    )}
    var nameError by remember { mutableStateOf(false) }

    // ── Selected devices ──────────────────────────────────────────────────────
    val seleccionados = remember {
        mutableStateListOf<SelectedEntry>().also { list ->
            if (routine != null) {
                // Group actions by device id
                val byDevice = mutableMapOf<String, MutableList<Pair<String, Map<String, Any>>>>()
                for (a in routine.actions) {
                    val id = a.device.id
                    val paramsMap = (a.params.firstOrNull() as? Map<*, *>)
                        ?.entries
                        ?.associate { (k, v) -> k.toString() to (v ?: "") as Any }
                        ?: emptyMap()
                    byDevice.getOrPut(id) { mutableListOf() }.add(a.actionName to paramsMap)
                }
                // Build entries
                val routineDeviceParams = routine.metadata?.let { m ->
                    @Suppress("UNCHECKED_CAST")
                    (m as? RoutineMetaDto)
                } // duration not stored separately in metadata here; skip

                for ((deviceId, actionPairs) in byDevice) {
                    val device = deviceById[deviceId] ?: continue
                    val context = allDevices.find { it.device.id == deviceId }
                    list.add(
                        SelectedEntry(
                            device = device,
                            homeName = context?.homeName ?: "",
                            roomName = context?.roomName ?: "",
                            actions = actionPairs.map { (name, params) -> DeviceAction(name, params) }
                        )
                    )
                }
            }
        }
    }

    val selectedIds by remember { derivedStateOf { seleccionados.map { it.device.id }.toSet() } }

    // ── Device search ─────────────────────────────────────────────────────────
    var busqueda by remember { mutableStateOf("") }

    val filteredDevices = remember(allDevices, selectedIds, busqueda) {
        val q = busqueda.trim().lowercase()
        allDevices
            .filter { it.device.id !in selectedIds }
            .filter { item ->
                q.isEmpty() ||
                    item.device.name.lowercase().contains(q) ||
                    item.homeName.lowercase().contains(q) ||
                    item.roomName.lowercase().contains(q)
            }
    }

    // ── Sub-sheet state (device config in routineMode) ────────────────────────
    var subSheetEntry by remember { mutableStateOf<DeviceItem?>(null) }

    // ── Saving ────────────────────────────────────────────────────────────────
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    fun buildRequest(): RoutineRequest {
        val trigger = if (tipoTrigger == "hora") hora else "Manual"
        val actions = seleccionados.flatMap { entry ->
            entry.actions.map { action ->
                val params: List<Any?> = if (action.params.isEmpty()) emptyList() else listOf(action.params)
                val durationVal = entry.duration.trim().toIntOrNull()
                RoutineActionDto(
                    device = DeviceRef(entry.device.id),
                    actionName = action.actionName,
                    params = params,
                    duration = durationVal?.times(60) // convert minutes → seconds
                )
            }
        }
        return RoutineRequest(
            name = name.trim(),
            actions = actions,
            metadata = RoutineMetaDto(
                descripcion = description.trim().ifEmpty { null },
                trigger = trigger,
                tipoTrigger = tipoTrigger,
                enabled = routine?.metadata?.enabled ?: false
            )
        )
    }

    fun submit() {
        if (name.isBlank()) { nameError = true; return }
        scope.launch {
            isSaving = true
            saveError = null
            val request = buildRequest()
            if (isEditing) {
                routineRepo.updateRoutine(routine!!.id, request)
                    .onSuccess { updated -> appViewModel.updateRoutine(updated); onDismiss() }
                    .onFailure { saveError = it.message }
            } else {
                routineRepo.createRoutine(request)
                    .onSuccess { created -> appViewModel.addRoutine(created); onDismiss() }
                    .onFailure { saveError = it.message }
            }
            isSaving = false
        }
    }

    // ── Sheet UI ──────────────────────────────────────────────────────────────

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            item {
                Text(
                    if (isEditing) stringResource(R.string.routine_edit) else stringResource(R.string.routine_new),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Name
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text(stringResource(R.string.common_name_required)) },
                    placeholder = { Text(stringResource(R.string.routine_name_placeholder)) },
                    isError = nameError,
                    supportingText = if (nameError) { { Text(stringResource(R.string.routine_name_required_err)) } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.routine_description)) },
                    placeholder = { Text(stringResource(R.string.routine_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Trigger type
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.routine_trigger_type), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("hora" to stringResource(R.string.routine_trigger_time), "manual" to stringResource(R.string.routine_manual)).forEach { (value, label) ->
                            FilterChip(
                                selected = tipoTrigger == value,
                                onClick = { tipoTrigger = value },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }

            // Time picker (only when "hora")
            if (tipoTrigger == "hora") {
                item {
                    OutlinedTextField(
                        value = hora,
                        onValueChange = { hora = it },
                        label = { Text(stringResource(R.string.routine_time_label)) },
                        placeholder = { Text("08:00") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        leadingIcon = { Icon(Icons.Rounded.Schedule, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Devices section header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.DevicesOther, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(R.string.routine_devices_involved),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (seleccionados.isNotEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                "${seleccionados.size}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Selected device entries
            items(seleccionados, key = { it.device.id }) { entry ->
                SelectedDeviceCard(
                    entry = entry,
                    onEdit = {
                        subSheetEntry = DeviceItem(entry.device, entry.homeName, entry.roomName)
                    },
                    onRemove = { seleccionados.remove(entry) }
                )
            }

            // Search + device list
            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = { Text(stringResource(R.string.routine_search_device)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = if (busqueda.isNotEmpty()) {
                        { IconButton(onClick = { busqueda = "" }) { Icon(Icons.Rounded.Clear, null) } }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filteredDevices.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (allDevices.isEmpty()) stringResource(R.string.routine_no_devices)
                            else if (busqueda.isNotEmpty()) stringResource(R.string.routine_no_search_results, busqueda)
                            else stringResource(R.string.routine_all_added),
                            color = MaterialTheme.colorScheme.outline,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            items(filteredDevices, key = { it.device.id }) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = {
                        subSheetEntry = item
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(deviceIcon(item.device.type.id), null, Modifier.size(20.dp))
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(item.device.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${item.homeName} › ${item.roomName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Icon(Icons.Rounded.ChevronRight, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Footer
            item {
                val footerError = saveError ?: deleteError
                if (footerError != null) {
                    Text(
                        footerError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditing) {
                        OutlinedButton(
                            onClick = {
                                if (!isDeleting) {
                                    isDeleting = true
                                    deleteError = null
                                    scope.launch {
                                        routineRepo.deleteRoutine(routine!!.id)
                                            .onSuccess { appViewModel.removeRoutine(routine.id); onDismiss() }
                                            .onFailure { deleteError = it.message }
                                        isDeleting = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isDeleting,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                            )
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.error)
                            } else {
                                Icon(Icons.Rounded.Delete, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.common_delete))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.common_cancel)) }
                    }
                    Button(
                        onClick = { submit() },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving && !isDeleting
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(if (isEditing) stringResource(R.string.routine_save_changes) else stringResource(R.string.routine_create))
                        }
                    }
                }
            }
        }
    }

    // Sub-sheet: device config in routineMode
    subSheetEntry?.let { item ->
        DeviceSheetRouter(
            device = item.device,
            routineMode = true,
            onDismiss = { subSheetEntry = null },
            onAddToRoutine = { actions ->
                val existing = seleccionados.indexOfFirst { it.device.id == item.device.id }
                if (existing == -1) {
                    seleccionados.add(
                        SelectedEntry(
                            device = item.device,
                            homeName = item.homeName,
                            roomName = item.roomName,
                            actions = actions
                        )
                    )
                } else {
                    seleccionados[existing].actions = actions
                }
                subSheetEntry = null
            }
        )
    }
}

// ── Selected device card ──────────────────────────────────────────────────────

@Composable
private fun SelectedDeviceCard(
    entry: SelectedEntry,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        border = ButtonDefaults.outlinedButtonBorder(true).copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Main row (icon, name, actions, edit, remove)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEdit() }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(deviceIcon(entry.device.type.id), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(entry.device.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (entry.actions.isNotEmpty()) {
                        Text(labelActions(entry.actions), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Edit, stringResource(R.string.common_edit), Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.routine_remove), Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                }
            }

            // Duration row
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.routine_duration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                OutlinedTextField(
                    value = entry.duration,
                    onValueChange = { v ->
                        if (v.isEmpty() || v.all { it.isDigit() }) entry.duration = v
                    },
                    placeholder = { Text(stringResource(R.string.routine_no_limit), style = MaterialTheme.typography.labelSmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(90.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                Text(stringResource(R.string.routine_min), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                if (entry.duration.isNotEmpty()) {
                    IconButton(onClick = { entry.duration = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.Close, null, Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
