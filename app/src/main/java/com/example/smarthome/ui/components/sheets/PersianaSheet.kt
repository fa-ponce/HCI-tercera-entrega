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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersianaSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null,
    onDeviceDeleted: ((String) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ServiceLocator.deviceRepository }

    var isLoading by remember { mutableStateOf(!routineMode) }
    var level by remember { mutableFloatStateOf(if (routineMode) 100f else 0f) }
    var selectedAction by remember { mutableStateOf("up") }
    var movementStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(device.id) {
        if (!routineMode) {
            repo.getDevice(device.id).onSuccess { d ->
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
            SheetHeader(title = device.name, subtitle = "Persiana", deviceId = if (!routineMode) device.id else null, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })

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
                            "Estado: $movementStatus",
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
                                    scope.launch {
                                        repo.executeAction(device.id, "up")
                                        level = 100f; selectedAction = "up"
                                    }
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
                            Text("Abrir")
                        }
                        val downActive = if (routineMode) selectedAction == "down" else level <= 0f
                        OutlinedButton(
                            onClick = {
                                if (routineMode) {
                                    selectedAction = "down"; level = 0f
                                } else {
                                    scope.launch {
                                        repo.executeAction(device.id, "down")
                                        level = 0f; selectedAction = "down"
                                    }
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
                            Text("Cerrar")
                        }
                    }
                }

                // Level slider
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SheetSectionLabel("Posicion")
                            Text("${level.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = level,
                            onValueChange = { level = it; selectedAction = "setLevel" },
                            onValueChangeFinished = {
                                if (!routineMode) {
                                    scope.launch {
                                        repo.executeAction(device.id, "setLevel", mapOf("level" to level.toInt()))
                                    }
                                }
                            },
                            valueRange = 0f..100f
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cerrada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("Abierta", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                if (routineMode) {
                    SheetRoutineFooter(
                        onCancel = onDismiss,
                        onAdd = {
                            val actions = if (selectedAction == "setLevel")
                                listOf(DeviceAction("setLevel", mapOf("level" to level.toInt())))
                            else
                                listOf(DeviceAction(selectedAction))
                            onAddToRoutine?.invoke(actions)
                            onDismiss()
                        }
                    )
                } else {
                    SheetDeleteButton(deviceId = device.id, onDismiss = onDismiss, onDeleted = onDeviceDeleted)
                }
            }
        }
    }
}
