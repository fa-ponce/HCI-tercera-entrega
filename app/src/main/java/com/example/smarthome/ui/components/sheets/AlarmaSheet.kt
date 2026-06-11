package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import kotlinx.coroutines.launch

private const val DEFAULT_CODE = "1234"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmaSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null,
    onDeviceDeleted: ((String) -> Unit)? = null,
    onDeviceRoomChanged: ((DeviceDto) -> Unit)? = null,
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ServiceLocator.deviceRepository }

    var isLoading by remember { mutableStateOf(!routineMode) }
    var status by remember { mutableStateOf(if (routineMode) "armedAway" else "disarmed") }
    var codeInput by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var codeVerified by remember { mutableStateOf(routineMode) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(device.id) {
        if (!routineMode) {
            repo.getDevice(device.id).onSuccess { d ->
                status = (d.state["status"] as? String) ?: "disarmed"
            }
            isLoading = false
        }
    }

    val statusActionMap = mapOf("armStay" to "armedStay", "armAway" to "armedAway", "disarm" to "disarmed")
    val actionStatusMap = mapOf("armedStay" to "armStay", "armedAway" to "armAway", "disarmed" to "disarm")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SheetHeader(title = device.name, subtitle = stringResource(R.string.device_type_alarm), deviceId = if (!routineMode) device.id else null, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
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
                                                scope.launch {
                                                    isSaving = true
                                                    repo.executeAction(device.id, action, mapOf("securityCode" to codeInput))
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
                                            horizontalAlignment = Alignment.CenterHorizontally,
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
                                onAddToRoutine?.invoke(listOf(DeviceAction(actionName)))
                                onDismiss()
                            }
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SheetRoomLinkButton(device = device, homes = homes, rooms = rooms, modifier = Modifier.weight(1f), onDeviceUpdated = onDeviceRoomChanged)
                            SheetDeleteButton(deviceId = device.id, onDismiss = onDismiss, modifier = Modifier.weight(1f), onDeleted = onDeviceDeleted)
                        }
                    }
                }
            }
        }
    }
}
