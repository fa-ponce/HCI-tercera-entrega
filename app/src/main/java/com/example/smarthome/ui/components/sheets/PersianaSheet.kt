package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
fun PersianaSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    var isLoading by remember { mutableStateOf(!routineMode) }
    var level by remember { mutableFloatStateOf(if (routineMode) 100f else 0f) }
    var selectedAction by remember { mutableStateOf("up") }
    var movementStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                level = ((d.state["level"] as? Double)?.toFloat() ?: 0f)
                movementStatus = d.state["status"] as? String
                selectedAction = if (level > 0) "up" else "down"
            }
            isLoading = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SheetHeader(
                title = device.name,
                subtitle = stringResource(R.string.device_type_blinds),
                onRename = if (!routineMode) { newName, cb -> actions.onRename(newName, cb) } else null
            )

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                if (!routineMode && movementStatus != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            stringResource(R.string.sheet_status_fmt, movementStatus ?: ""),
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Open / Close buttons
                SheetSectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        val upActive = if (routineMode) selectedAction == "up" else level >= 100f
                        OutlinedButton(
                            onClick = {
                                if (routineMode) {
                                    selectedAction = "up"; level = 100f
                                } else {
                                    actions.onExecuteAction("up", emptyMap(), null)
                                    level = 100f; selectedAction = "up"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (upActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            enabled = routineMode || level < 100f
                        ) {
                            Icon(Icons.Rounded.KeyboardArrowUp, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.sheet_open_action))
                        }
                        val downActive = if (routineMode) selectedAction == "down" else level <= 0f
                        OutlinedButton(
                            onClick = {
                                if (routineMode) {
                                    selectedAction = "down"; level = 0f
                                } else {
                                    actions.onExecuteAction("down", emptyMap(), null)
                                    level = 0f; selectedAction = "down"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (downActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            enabled = routineMode || level > 0f
                        ) {
                            Icon(Icons.Rounded.KeyboardArrowDown, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.sheet_close_action))
                        }
                    }
                }

                // Level slider
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SheetSectionLabel(stringResource(R.string.sheet_position))
                            Text("${level.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = level,
                            onValueChange = { level = it; selectedAction = "setLevel" },
                            onValueChangeFinished = {
                                if (!routineMode) actions.onExecuteAction("setLevel", mapOf("level" to level.toInt()), null)
                            },
                            valueRange = 0f..100f
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.sheet_door_closed_f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(stringResource(R.string.sheet_door_open_f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                if (routineMode) {
                    SheetRoutineFooter(
                        onCancel = onDismiss,
                        onAdd = {
                            val routineActions = if (selectedAction == "setLevel")
                                listOf(DeviceAction("setLevel", mapOf("level" to level.toInt())))
                            else
                                listOf(DeviceAction(selectedAction))
                            onAddToRoutine?.invoke(routineActions)
                            onDismiss()
                        }
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SheetRoomLinkButton(device = device, homes = homes, rooms = rooms, modifier = Modifier.weight(1f), onUnlink = actions.onUnlink, onLink = actions.onLink)
                        SheetDeleteButton(onDelete = actions.onDelete, onDismiss = onDismiss, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
