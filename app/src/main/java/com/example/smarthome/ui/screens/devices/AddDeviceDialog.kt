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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onCreate: (name: String, typeId: String, roomId: String?, marca: String) -> Unit
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val deviceTypes by appViewModel.deviceTypes.collectAsState()

    LaunchedEffect(Unit) { appViewModel.loadDeviceTypes() }

    var name by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }

    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<DeviceTypeDto?>(null) }

    var homeExpanded by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf<HomeDto?>(null) }

    var roomExpanded by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<RoomDto?>(null) }

    val availableRooms = remember(selectedHome, rooms) {
        selectedHome?.let { rooms[it.id] } ?: emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Dispositivo", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Dispositivo") },
                    placeholder = { Text("Ej: Lampara LED") },
                    singleLine = true,
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
                        label = { Text("Tipo de Dispositivo") },
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
                    expanded = homeExpanded,
                    onExpandedChange = { homeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedHome?.name ?: "Sin casa",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Casa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeExpanded) },
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
                    expanded = roomExpanded && selectedHome != null,
                    onExpandedChange = { if (selectedHome != null) roomExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRoom?.name ?: "Sin habitación",
                        onValueChange = {},
                        readOnly = true,
                        enabled = selectedHome != null,
                        label = { Text("Habitación") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roomExpanded && selectedHome != null) },
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
                    if (name.isNotBlank() && selectedType != null) {
                        onCreate(name.trim(), selectedType!!.id, selectedRoom?.id, marca.trim())
                    }
                },
                enabled = name.isNotBlank() && selectedType != null
            ) {
                Text("Crear", color = Color(0xFF3A5A90), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
