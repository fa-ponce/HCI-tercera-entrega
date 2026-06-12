package com.example.smarthome.ui.components.sheets

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
fun PuertaSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    var isLoading by remember { mutableStateOf(!routineMode) }
    var isOpen by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var selectedDoor by remember { mutableStateOf("close") }
    var selectedLock by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                isOpen = d.state["status"] == "opened"
                isLocked = d.state["lock"] == "locked"
                selectedDoor = if (isOpen) "open" else "close"
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
                val routineActions = mutableListOf(DeviceAction(selectedDoor))
                selectedLock?.let { routineActions.add(DeviceAction(it)) }
                onAddToRoutine?.invoke(routineActions)
                onDismiss()
            }
        } else null
    ) {
        // Status badges (only in normal mode)
        if (!routineMode) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                val openColor = if (isOpen) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                val openText = if (isOpen) stringResource(R.string.sheet_door_open_f) else stringResource(R.string.sheet_door_closed_f)
                Surface(color = openColor, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f)) {
                    Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text(openText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
                val lockColor = if (isLocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                val lockText = if (isLocked) stringResource(R.string.sheet_locked_f) else stringResource(R.string.sheet_unlocked_f)
                Surface(color = lockColor, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f)) {
                    Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                        Text(lockText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Actions
        SheetSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetSectionLabel(stringResource(R.string.sheet_actions))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    DoorActionButton(
                        label = stringResource(R.string.sheet_open_action),
                        active = if (routineMode) selectedDoor == "open" else isOpen,
                        enabled = routineMode || !isOpen,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (routineMode) {
                            selectedDoor = "open"
                        } else {
                            actions.onExecuteAction("open", emptyMap(), null)
                            isOpen = true; isLocked = false; selectedDoor = "open"
                        }
                    }
                    DoorActionButton(
                        label = stringResource(R.string.sheet_close_action),
                        active = if (routineMode) selectedDoor == "close" else !isOpen,
                        enabled = routineMode || isOpen,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (routineMode) {
                            selectedDoor = "close"
                        } else {
                            actions.onExecuteAction("close", emptyMap(), null)
                            isOpen = false; selectedDoor = "close"
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    DoorActionButton(
                        label = stringResource(R.string.sheet_lock_action),
                        active = if (routineMode) selectedLock == "lock" else isLocked,
                        enabled = routineMode || (!isLocked && !isOpen),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (routineMode) {
                            selectedLock = if (selectedLock == "lock") null else "lock"
                        } else {
                            actions.onExecuteAction("lock", emptyMap(), null)
                            isLocked = true
                        }
                    }
                    DoorActionButton(
                        label = stringResource(R.string.sheet_unlock_action),
                        active = if (routineMode) selectedLock == "unlock" else !isLocked,
                        enabled = routineMode || isLocked,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (routineMode) {
                            selectedLock = if (selectedLock == "unlock") null else "unlock"
                        } else {
                            actions.onExecuteAction("unlock", emptyMap(), null)
                            isLocked = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoorActionButton(
    label: String,
    active: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        onClick = { if (enabled) onClick() },
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        enabled = enabled
    ) {
        Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
