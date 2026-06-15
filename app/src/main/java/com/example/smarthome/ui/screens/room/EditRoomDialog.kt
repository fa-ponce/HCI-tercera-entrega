package com.example.smarthome.ui.screens.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.smarthome.R
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.ui.screens.homes.ROOM_TYPES
import com.example.smarthome.ui.screens.homes.roomTypeLabel

// Mismos colores del botón/confirmación de eliminar de la web (.btn-delete)
private val DeleteRed = Color(0xFF8B1A1A)

/**
 * Réplica del ModalEditarHabitacion de la web: nombre editable inline con lápiz,
 * tipo de habitación, casa vinculada (con "Sin casa") y eliminar con
 * confirmación inline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomDialog(
    room: RoomDto,
    homes: List<HomeDto>,
    currentHomeId: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, homeId: String?, onResult: (Boolean, String?) -> Unit) -> Unit,
    onDelete: (onResult: (Boolean, String?) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(room.name) }
    var editingName by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(room.metadata?.type ?: ROOM_TYPES[0]) }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf(homes.find { it.id == currentHomeId }) }
    var homeExpanded by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val busy = isSaving || isDeleting

    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(Modifier.padding(24.dp)) {

                // Header: nombre con lápiz (edición inline) + botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (editingName) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            name.ifBlank { room.name },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { editingName = true }, enabled = !busy) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.common_edit_name),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(onClick = { if (!busy) onDismiss() }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.common_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Tipo de habitación
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = roomTypeLabel(selectedType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.room_type_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        ROOM_TYPES.forEach { type ->
                            DropdownMenuItem(text = { Text(roomTypeLabel(type)) }, onClick = { selectedType = type; typeExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                // Casa vinculada
                ExposedDropdownMenuBox(expanded = homeExpanded, onExpandedChange = { homeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedHome?.name ?: stringResource(R.string.room_no_home_standalone),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.room_linked_home)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(homeExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = homeExpanded, onDismissRequest = { homeExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.room_no_home_standalone)) },
                            onClick = { selectedHome = null; homeExpanded = false }
                        )
                        homes.forEach { home ->
                            DropdownMenuItem(text = { Text(home.name) }, onClick = { selectedHome = home; homeExpanded = false })
                        }
                    }
                }

                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(24.dp))

                if (confirmingDelete) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFF7A5A00), modifier = Modifier.size(16.dp))
                        Text(
                            stringResource(R.string.room_delete_warn),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7A5A00)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { confirmingDelete = false },
                            enabled = !busy,
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.common_cancel)) }
                        Button(
                            onClick = {
                                if (!busy) {
                                    isDeleting = true
                                    error = null
                                    onDelete { ok, err ->
                                        isDeleting = false
                                        if (!ok) { error = err; confirmingDelete = false }
                                    }
                                }
                            },
                            enabled = !busy,
                            colors = ButtonDefaults.buttonColors(containerColor = DeleteRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(stringResource(R.string.common_delete), color = Color.White)
                        }
                    }
                } else {
                    Button(
                        onClick = { confirmingDelete = true },
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = DeleteRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.room_delete), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { if (!busy) onDismiss() },
                            enabled = !busy,
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.common_cancel)) }
                        Button(
                            onClick = {
                                if (!busy) {
                                    isSaving = true
                                    error = null
                                    onSave(name.trim(), selectedType, selectedHome?.id) { ok, err ->
                                        isSaving = false
                                        if (!ok) error = err
                                    }
                                }
                            },
                            enabled = name.isNotBlank() && !busy,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(stringResource(R.string.common_save), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
