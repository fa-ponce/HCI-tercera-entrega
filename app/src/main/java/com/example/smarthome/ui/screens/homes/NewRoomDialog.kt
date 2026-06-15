package com.example.smarthome.ui.screens.homes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.domain.roomTypeLabelRes

internal val ROOM_TYPES = listOf(
    "Living", "Dormitorio", "Cocina", "Baño", "Garaje", "Estudio", "Comedor", "Lavadero"
)

/** Etiqueta visible (traducida) de un tipo de habitación; si no es conocido, el valor crudo. */
@Composable
internal fun roomTypeLabel(type: String?): String =
    roomTypeLabelRes(type)?.let { stringResource(it) } ?: (type ?: "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewRoomDialog(
    homes: List<HomeDto>,
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, floor: Int, homeId: String?, onResult: (Boolean, String?) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf(ROOM_TYPES[0]) }
    var typeExpanded by remember { mutableStateOf(false) }
    var floor by remember { mutableStateOf(1) }
    var selectedHome by remember { mutableStateOf<HomeDto?>(null) }
    var homeExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.homes_new_room), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text(stringResource(R.string.common_name_required)) },
                    placeholder = { Text(stringResource(R.string.homes_room_name_placeholder)) },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { msg -> { Text(msg) } },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = roomTypeLabel(selectedType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.common_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        ROOM_TYPES.forEach { type ->
                            DropdownMenuItem(text = { Text(roomTypeLabel(type)) }, onClick = { selectedType = type; typeExpanded = false })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = homeExpanded, onExpandedChange = { homeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedHome?.name ?: stringResource(R.string.homes_no_home),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.common_home)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(homeExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = homeExpanded, onDismissRequest = { homeExpanded = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.homes_no_home)) }, onClick = { selectedHome = null; homeExpanded = false })
                        homes.forEach { home ->
                            DropdownMenuItem(text = { Text(home.name) }, onClick = { selectedHome = home; homeExpanded = false })
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.common_floor), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (floor > 1) floor-- }, modifier = Modifier.size(36.dp)) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text("$floor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { floor++ }, modifier = Modifier.size(36.dp)) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }

                if (saveError != null) {
                    Text(saveError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            val errMin = stringResource(R.string.common_name_min_chars)
            val errMax = stringResource(R.string.common_name_max_chars)
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.length < 3 -> nameError = errMin
                        trimmed.length > 100 -> nameError = errMax
                        !isSaving -> {
                            isSaving = true
                            saveError = null
                            onCreate(trimmed, selectedType, floor, selectedHome?.id) { ok, err ->
                                isSaving = false
                                if (!ok) saveError = err
                            }
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
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
