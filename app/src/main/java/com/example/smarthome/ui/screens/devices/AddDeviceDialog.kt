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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceTypeDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.domain.deviceTypeName
import com.example.smarthome.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceDialog(
    appViewModel: AppViewModel,
    onDismiss: () -> Unit,
    onCreate: (name: String, typeId: String, roomId: String?, marca: String, onResult: (Boolean) -> Unit) -> Unit,
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
    var typeError by remember { mutableStateOf<String?>(null) }

    var homeExpanded by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf(lockedHome) }

    var roomExpanded by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf(lockedRoom) }

    val availableRooms = remember(selectedHome, rooms) {
        selectedHome?.let { rooms[it.id] } ?: emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.device_new), fontWeight = FontWeight.Bold) },
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
                    label = { Text(stringResource(R.string.device_name_label)) },
                    placeholder = { Text(stringResource(R.string.device_name_placeholder)) },
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
                        value = selectedType?.let { stringResource(deviceTypeName(it.id)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.device_type_label)) },
                        placeholder = { Text(stringResource(R.string.device_type_placeholder)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        isError = typeError != null,
                        supportingText = typeError?.let { msg -> { Text(msg) } },
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
                                text = { Text(stringResource(deviceTypeName(type.id))) },
                                onClick = {
                                    selectedType = type
                                    typeError = null
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
                        value = selectedHome?.name ?: stringResource(R.string.homes_no_home),
                        onValueChange = {},
                        readOnly = true,
                        enabled = !locked,
                        label = { Text(stringResource(R.string.common_home)) },
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
                            text = { Text(stringResource(R.string.homes_no_home)) },
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
                        value = selectedRoom?.name ?: stringResource(R.string.device_no_room),
                        onValueChange = {},
                        readOnly = true,
                        enabled = selectedHome != null && !locked,
                        label = { Text(stringResource(R.string.common_room)) },
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
                            text = { Text(stringResource(R.string.device_no_room_free)) },
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
                    label = { Text(stringResource(R.string.device_brand)) },
                    placeholder = { Text(stringResource(R.string.device_brand_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val errMin = stringResource(R.string.common_name_min_chars)
            val errMax = stringResource(R.string.common_name_max_chars)
            val errType = stringResource(R.string.device_type_required_err)
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    nameError = when {
                        trimmed.length < 3 -> errMin
                        trimmed.length > 100 -> errMax
                        else -> null
                    }
                    typeError = if (selectedType == null) errType else null
                    if (nameError != null || typeError != null) return@TextButton
                    if (!isSaving) {
                        isSaving = true
                        onCreate(trimmed, selectedType?.id ?: return@TextButton, selectedRoom?.id, marca.trim()) { success ->
                            if (!success) isSaving = false
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(stringResource(R.string.common_create), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
