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
fun PuertaSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ServiceLocator.deviceRepository }

    var isLoading by remember { mutableStateOf(!routineMode) }
    var isOpen by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var selectedDoor by remember { mutableStateOf("close") }
    var selectedLock by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(device.id) {
        if (!routineMode) {
            repo.getDevice(device.id).onSuccess { d ->
                isOpen = d.state["status"] == "opened"
                isLocked = d.state["lock"] == "locked"
                selectedDoor = if (isOpen) "open" else "close"
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
            SheetHeader(title = device.name, subtitle = "Puerta", deviceId = if (!routineMode) device.id else null, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Status badges (only in normal mode)
                if (!routineMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        val openColor = if (isOpen) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                        val openText = if (isOpen) "Abierta" else "Cerrada"
                        Surface(color = openColor, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f)) {
                            Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text(openText, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        val lockColor = if (isLocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                        val lockText = if (isLocked) "Bloqueada" else "Desbloqueada"
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
                        SheetSectionLabel("Acciones")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            DoorActionButton(
                                label = "Abrir",
                                active = if (routineMode) selectedDoor == "open" else isOpen,
                                enabled = routineMode || !isOpen,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (routineMode) { selectedDoor = "open" } else {
                                    scope.launch {
                                        repo.executeAction(device.id, "open")
                                        isOpen = true; isLocked = false; selectedDoor = "open"
                                    }
                                }
                            }
                            DoorActionButton(
                                label = "Cerrar",
                                active = if (routineMode) selectedDoor == "close" else !isOpen,
                                enabled = routineMode || isOpen,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (routineMode) { selectedDoor = "close" } else {
                                    scope.launch {
                                        repo.executeAction(device.id, "close")
                                        isOpen = false; selectedDoor = "close"
                                    }
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            DoorActionButton(
                                label = "Bloquear",
                                active = if (routineMode) selectedLock == "lock" else isLocked,
                                enabled = routineMode || (!isLocked && !isOpen),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (routineMode) { selectedLock = if (selectedLock == "lock") null else "lock" } else {
                                    scope.launch {
                                        repo.executeAction(device.id, "lock")
                                        isLocked = true
                                    }
                                }
                            }
                            DoorActionButton(
                                label = "Desbloquear",
                                active = if (routineMode) selectedLock == "unlock" else !isLocked,
                                enabled = routineMode || isLocked,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (routineMode) { selectedLock = if (selectedLock == "unlock") null else "unlock" } else {
                                    scope.launch {
                                        repo.executeAction(device.id, "unlock")
                                        isLocked = false
                                    }
                                }
                            }
                        }
                    }
                }

                if (routineMode) {
                    SheetRoutineFooter(
                        onCancel = onDismiss,
                        onAdd = {
                            val actions = mutableListOf(DeviceAction(selectedDoor))
                            selectedLock?.let { actions.add(DeviceAction(it)) }
                            onAddToRoutine?.invoke(actions)
                            onDismiss()
                        }
                    )
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
