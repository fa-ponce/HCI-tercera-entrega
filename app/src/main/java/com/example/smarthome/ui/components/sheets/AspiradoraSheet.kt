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
    var isLoading by remember { mutableStateOf(!routineMode) }
    var status by remember { mutableStateOf("docked") }
    var mode by remember { mutableStateOf("vacuum") }
    var selectedAction by remember { mutableStateOf("start") }
    var isSaving by remember { mutableStateOf(false) }

    val statusLabels = mapOf(
        "active" to R.string.action_cleaning, "paused" to R.string.action_paused,
        "docked" to R.string.action_docked, "inactive" to R.string.sheet_inactive
    )
    val modoOpts = listOf("vacuum" to R.string.sheet_vacuum, "mop" to R.string.sheet_mop)

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                status = (d.state["status"] as? String) ?: "docked"
                mode = (d.state["mode"] as? String) ?: "vacuum"
                selectedAction = if (status == "docked" || status == "inactive") "start" else "dock"
            }
            isLoading = false
        }
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
                onAddToRoutine?.invoke(listOf(
                    DeviceAction(selectedAction),
                    DeviceAction("setMode", mapOf("mode" to mode))
                ))
                onDismiss()
            }
        } else null
    ) {
        // Status badge
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

        // Control buttons
        SheetSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetSectionLabel(stringResource(R.string.sheet_controls))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    // Start
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

                    // Pause (only in normal mode)
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

                    // Dock
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

        // Mode
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
    }
}
