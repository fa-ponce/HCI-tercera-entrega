package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto

private const val DEFAULT_CODE = "1234"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmaSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    var isLoading by remember { mutableStateOf(!routineMode) }
    var status by remember { mutableStateOf(if (routineMode) "armedAway" else "disarmed") }
    var codeInput by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var codeVerified by remember { mutableStateOf(routineMode) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                status = (d.state["status"] as? String) ?: "disarmed"
            }
            isLoading = false
        }
    }

    val actionStatusMap = mapOf("armedStay" to "armStay", "armedAway" to "armAway", "disarmed" to "disarm")

    BaseDeviceSheet(
        device = device,
        routineMode = routineMode,
        onDismiss = onDismiss,
        actions = actions,
        homes = homes,
        rooms = rooms,
        isLoading = isLoading,
        showFooter = false
    ) {
        // Security code input (only in normal mode before verified)
        if (!codeVerified) {
            SheetSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SheetSectionLabel(stringResource(R.string.sheet_security_code))
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it; codeError = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.sheet_enter_code)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = codeError,
                        supportingText = if (codeError) { { Text(stringResource(R.string.sheet_wrong_code)) } } else null,
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (codeInput == DEFAULT_CODE) codeVerified = true
                            else codeError = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.sheet_confirm_code))
                    }
                }
            }
        }

        // Actions (visible after code verified or in routine mode)
        if (codeVerified) {
            SheetSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SheetSectionLabel(stringResource(R.string.sheet_actions))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("armStay", "armedStay", stringResource(R.string.sheet_mode_home)),
                            Triple("armAway", "armedAway", stringResource(R.string.sheet_mode_away)),
                            Triple("disarm", "disarmed", stringResource(R.string.sheet_disarm))
                        ).forEach { (action, targetStatus, label) ->
                            val active = status == targetStatus
                            Surface(
                                onClick = {
                                    if (routineMode) {
                                        status = targetStatus
                                    } else if (!active && !isSaving) {
                                        isSaving = true
                                        actions.onExecuteAction(action, mapOf("securityCode" to codeInput)) { _ ->
                                            status = targetStatus
                                            isSaving = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                enabled = !isSaving
                            ) {
                                Column(
                                    Modifier.padding(10.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                    )
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
                        val actionName = actionStatusMap[status] ?: "armAway"
                        onAddToRoutine?.invoke(
                            listOf(DeviceAction(actionName, mapOf("securityCode" to DEFAULT_CODE)))
                        )
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
