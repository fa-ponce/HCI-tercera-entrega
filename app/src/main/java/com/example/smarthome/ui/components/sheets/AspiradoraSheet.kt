package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun AspiradoraSheet(
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
    var status by remember { mutableStateOf("docked") }
    var mode by remember { mutableStateOf("vacuum") }
    var selectedAction by remember { mutableStateOf("start") }
    var isSaving by remember { mutableStateOf(false) }

    val statusLabels = mapOf("active" to "Limpiando", "paused" to "Pausado", "docked" to "En base", "inactive" to "Inactivo")
    val modoOpts = listOf("vacuum" to "Aspirar", "mop" to "Trapear")

    LaunchedEffect(device.id) {
        if (!routineMode) {
            repo.getDevice(device.id).onSuccess { d ->
                status = (d.state["status"] as? String) ?: "docked"
                mode = (d.state["mode"] as? String) ?: "vacuum"
                selectedAction = if (status == "docked" || status == "inactive") "start" else "dock"
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
            SheetHeader(title = device.name, subtitle = "Aspiradora", deviceId = if (!routineMode) device.id else null, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
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
                            statusLabels[status] ?: status,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Control buttons
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Controles")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            // Start
                            val startActive = if (routineMode) selectedAction == "start" else status == "active"
                            Surface(
                                onClick = {
                                    if (routineMode) { selectedAction = "start" } else if (!isSaving && status != "active") {
                                        scope.launch {
                                            isSaving = true
                                            repo.executeAction(device.id, "start")
                                            status = "active"; selectedAction = "start"; isSaving = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                color = if (startActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                enabled = !isSaving
                            ) {
                                Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                                    Text("Iniciar", style = MaterialTheme.typography.labelLarge, fontWeight = if (startActive) FontWeight.Bold else FontWeight.Normal)
                                }
                            }

                            // Pause (only in normal mode)
                            if (!routineMode) {
                                Surface(
                                    onClick = {
                                        if (!isSaving && status == "active") {
                                            scope.launch {
                                                isSaving = true
                                                repo.executeAction(device.id, "pause")
                                                status = "paused"; isSaving = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (status == "paused") MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    enabled = !isSaving && status == "active"
                                ) {
                                    Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                                        Text("Pausar", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }

                            // Dock
                            val dockActive = if (routineMode) selectedAction == "dock" else status == "docked"
                            Surface(
                                onClick = {
                                    if (routineMode) { selectedAction = "dock" } else if (!isSaving && status != "docked") {
                                        scope.launch {
                                            isSaving = true
                                            repo.executeAction(device.id, "dock")
                                            status = "docked"; selectedAction = "dock"; isSaving = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                color = if (dockActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                enabled = !isSaving
                            ) {
                                Box(Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                                    Text("Volver a base", style = MaterialTheme.typography.labelMedium, fontWeight = if (dockActive) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }

                // Mode
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Modo")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            modoOpts.forEach { (value, label) ->
                                FilterChip(
                                    selected = mode == value,
                                    onClick = {
                                        mode = value
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setMode", mapOf("mode" to value)) }
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                if (routineMode) {
                    SheetRoutineFooter(
                        onCancel = onDismiss,
                        onAdd = {
                            onAddToRoutine?.invoke(listOf(
                                DeviceAction(selectedAction),
                                DeviceAction("setMode", mapOf("mode" to mode))
                            ))
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
