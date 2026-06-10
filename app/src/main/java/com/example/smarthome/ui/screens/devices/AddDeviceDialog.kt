package com.example.smarthome.ui.screens.devices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.api.models.DeviceTypeDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceDialog(
    appViewModel: AppViewModel,
    onDismiss: () -> Unit,
    onCreate: (name: String, typeId: String, roomId: String?, marca: String) -> Unit,
    // Si se pasan, el diálogo queda FIJADO a esa casa/habitación (no se pueden cambiar).
    lockedHome: HomeDto? = null,
    lockedRoom: RoomDto? = null
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val deviceTypes by appViewModel.deviceTypes.collectAsState()

    LaunchedEffect(Unit) { appViewModel.loadDeviceTypes() }

    // Cuando hay habitación fija, los selectores de casa y habitación quedan bloqueados.
    val locked = lockedRoom != null

    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var marca by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<DeviceTypeDto?>(null) }

    var homeExpanded by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf(lockedHome) }

    var roomExpanded by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf(lockedRoom) }

    val availableRooms = remember(selectedHome, rooms) {
        selectedHome?.let { rooms[it.id] } ?: emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo dispositivo", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Nombre del dispositivo *") },
                    placeholder = { Text("Ej: Lampara LED") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { msg -> { Text(msg) } },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // Tipo de dispositivo
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de dispositivo *") },
                        placeholder = { Text("Seleccionar tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        deviceTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Casa
                ExposedDropdownMenuBox(
                    expanded = homeExpanded && !locked,
                    onExpandedChange = { if (!locked) homeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedHome?.name ?: "Sin casa",
                        onValueChange = {},
                        readOnly = true,
                        enabled = !locked,
                        label = { Text("Casa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeExpanded && !locked) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = homeExpanded,
                        onDismissRequest = { homeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin casa") },
                            onClick = {
                                selectedHome = null
                                selectedRoom = null
                                homeExpanded = false
                            }
                        )
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
                Spacer(Modifier.height(12.dp))

                // Habitación (solo si hay casa seleccionada)
                ExposedDropdownMenuBox(
                    expanded = roomExpanded && selectedHome != null && !locked,
                    onExpandedChange = { if (selectedHome != null && !locked) roomExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRoom?.name ?: "Sin habitación",
                        onValueChange = {},
                        readOnly = true,
                        enabled = selectedHome != null && !locked,
                        label = { Text("Habitación") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded && selectedHome != null && !locked) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = roomExpanded && selectedHome != null,
                        onDismissRequest = { roomExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin habitación (dispositivo libre)") },
                            onClick = {
                                selectedRoom = null
                                roomExpanded = false
                            }
                        )
                        availableRooms.forEach { room ->
                            DropdownMenuItem(
                                text = { Text(room.name) },
                                onClick = {
                                    selectedRoom = room
                                    roomExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Marca/Modelo
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text("Marca/Modelo") },
                    placeholder = { Text("Ej: Phillips Hue") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.length < 3 -> { nameError = "El nombre debe tener al menos 3 caracteres"; return@TextButton }
                        trimmed.length > 100 -> { nameError = "El nombre no puede superar 100 caracteres"; return@TextButton }
                        selectedType == null -> return@TextButton
                    }
                    if (!isSaving) {
                        isSaving = true
                        onCreate(trimmed, selectedType!!.id, selectedRoom?.id, marca.trim())
                    }
                },
                enabled = selectedType != null && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text("Crear", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}
