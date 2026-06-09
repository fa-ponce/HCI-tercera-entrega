package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.data.api.models.RoomRef
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    deviceId: String? = null,
    onRenamed: ((String) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var displayTitle by remember(title) { mutableStateOf(title) }
    var showDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        if (deviceId != null) {
            IconButton(onClick = { editText = displayTitle; showDialog = true }) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Editar nombre",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDialog) {
        var isSaving by remember { mutableStateOf(false) }
        var saveError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!isSaving) showDialog = false },
            title = { Text("Renombrar dispositivo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it; saveError = null },
                        label = { Text("Nombre") },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (saveError != null) {
                        Text(saveError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = editText.trim()
                        if (newName.isNotEmpty() && !isSaving) {
                            isSaving = true
                            saveError = null
                            scope.launch {
                                ServiceLocator.deviceRepository.updateDevice(deviceId!!, newName)
                                    .onSuccess {
                                        displayTitle = newName
                                        onRenamed?.invoke(newName)
                                        showDialog = false
                                    }
                                    .onFailure { saveError = it.message }
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }, enabled = !isSaving) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun SheetSectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun SheetDeleteButton(
    deviceId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onDeleted: ((String) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    Button(
        onClick = { showConfirm = true },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text("Eliminar", color = Color.White, fontWeight = FontWeight.SemiBold)
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showConfirm = false },
            title = { Text("Eliminar dispositivo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("¿Estás seguro que querés eliminar este dispositivo? Esta acción no se puede deshacer.")
                    if (deleteError != null) {
                        Text(deleteError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isDeleting) {
                            isDeleting = true
                            deleteError = null
                            scope.launch {
                                ServiceLocator.deviceRepository.deleteDevice(deviceId)
                                    .onSuccess {
                                        onDeleted?.invoke(deviceId)
                                        onDismiss()
                                    }
                                    .onFailure { deleteError = it.message }
                                isDeleting = false
                            }
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }, enabled = !isDeleting) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetRoomLinkButton(
    device: DeviceDto,
    homes: List<HomeDto>,
    rooms: Map<String, List<RoomDto>>,
    modifier: Modifier = Modifier,
    onDeviceUpdated: ((DeviceDto) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val isLinked = device.room != null
    var showUnlinkConfirm by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf<HomeDto?>(null) }
    var homeExpanded by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<RoomDto?>(null) }
    var roomExpanded by remember { mutableStateOf(false) }
    var isUnlinking by remember { mutableStateOf(false) }
    var isLinking by remember { mutableStateOf(false) }
    var unlinkError by remember { mutableStateOf<String?>(null) }
    var linkError by remember { mutableStateOf<String?>(null) }

    OutlinedButton(
        onClick = { if (isLinked) showUnlinkConfirm = true else showLinkDialog = true },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isLinked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(
            1.dp,
            if (isLinked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    ) {
        Text(if (isLinked) "Desvincular" else "Vincular", fontWeight = FontWeight.SemiBold)
    }

    if (showUnlinkConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isUnlinking) showUnlinkConfirm = false },
            title = { Text("Desvincular dispositivo", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("El dispositivo se desvinculará de su habitación actual y quedará libre.")
                    if (unlinkError != null) {
                        Text(unlinkError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isUnlinking) {
                            isUnlinking = true
                            unlinkError = null
                            scope.launch {
                                ServiceLocator.deviceRepository.removeDeviceFromRoom(device.id)
                                    .onSuccess {
                                        onDeviceUpdated?.invoke(device.copy(room = null))
                                        showUnlinkConfirm = false
                                    }
                                    .onFailure { unlinkError = it.message }
                                isUnlinking = false
                            }
                        }
                    },
                    enabled = !isUnlinking,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isUnlinking) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Desvincular", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkConfirm = false }, enabled = !isUnlinking) { Text("Cancelar") }
            }
        )
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false; selectedHome = null; selectedRoom = null },
            title = { Text("Vincular a habitación", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (linkError != null) {
                        Text(linkError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    ExposedDropdownMenuBox(
                        expanded = homeExpanded,
                        onExpandedChange = { homeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedHome?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Casa") },
                            placeholder = { Text("Seleccionar casa") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(homeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = homeExpanded,
                            onDismissRequest = { homeExpanded = false }
                        ) {
                            homes.forEach { home ->
                                DropdownMenuItem(
                                    text = { Text(home.name) },
                                    onClick = {
                                        selectedHome = home
                                        selectedRoom = null
                                        homeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedHome != null) {
                        val availableRooms = rooms[selectedHome!!.id] ?: emptyList()
                        ExposedDropdownMenuBox(
                            expanded = roomExpanded,
                            onExpandedChange = { roomExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedRoom?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Habitación") },
                                placeholder = { Text("Seleccionar habitación") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roomExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = roomExpanded,
                                onDismissRequest = { roomExpanded = false }
                            ) {
                                availableRooms.forEach { room ->
                                    DropdownMenuItem(
                                        text = { Text(room.name) },
                                        onClick = { selectedRoom = room; roomExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val room = selectedRoom ?: return@Button
                        if (!isLinking) {
                            isLinking = true
                            linkError = null
                            scope.launch {
                                ServiceLocator.deviceRepository.addDeviceToRoom(room.id, device.id)
                                    .onSuccess {
                                        onDeviceUpdated?.invoke(device.copy(room = RoomRef(room.id)))
                                        selectedHome = null
                                        selectedRoom = null
                                        showLinkDialog = false
                                    }
                                    .onFailure { linkError = it.message }
                                isLinking = false
                            }
                        }
                    },
                    enabled = selectedRoom != null && !isLinking
                ) {
                    if (isLinking) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Vincular")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLinkDialog = false; selectedHome = null; selectedRoom = null },
                    enabled = !isLinking
                ) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun SheetRoutineFooter(onCancel: () -> Unit, onAdd: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text("Cancelar")
        }
        Button(onClick = onAdd, modifier = Modifier.weight(1f)) {
            Text("Agregar a rutina")
        }
    }
}

@Composable
fun <T> PillGroup(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = { Text(label(option), style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}
