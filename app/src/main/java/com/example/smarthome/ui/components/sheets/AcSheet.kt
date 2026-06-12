package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    var isLoading by remember { mutableStateOf(!routineMode) }
    var isOn by remember { mutableStateOf(false) }
    var temperature by remember { mutableFloatStateOf(24f) }
    var mode by remember { mutableStateOf("frio") }
    var verticalSwing by remember { mutableStateOf("auto") }
    var horizontalSwing by remember { mutableStateOf("auto") }
    var fanSpeed by remember { mutableStateOf("auto") }

    val modos = listOf("ventilacion", "frio", "calor")
    val verticalOpts = listOf("auto", "22", "45", "67", "90")
    val horizontalOpts = listOf("auto", "-90", "-45", "0", "45", "90")
    val fanOpts = listOf("auto", "25", "50", "75", "100")

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                val s = d.state
                isOn = s["status"] == "on"
                temperature = ((s["temperature"] as? Double)?.toFloat() ?: 24f)
                mode = (s["mode"] as? String) ?: "frio"
                verticalSwing = (s["verticalSwing"] as? String) ?: "auto"
                horizontalSwing = (s["horizontalSwing"] as? String) ?: "auto"
                fanSpeed = (s["fanSpeed"] as? String) ?: "auto"
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
            SheetHeader(
                title = device.name,
                subtitle = stringResource(R.string.device_type_ac),
                onRename = if (!routineMode) { newName, cb -> actions.onRename(newName, cb) } else null
            )

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Power
                SheetSectionCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isOn) stringResource(R.string.common_on) else stringResource(R.string.common_off), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Switch(checked = isOn, onCheckedChange = { v ->
                            isOn = v
                            if (!routineMode) actions.onExecuteAction(if (v) "turnOn" else "turnOff", emptyMap(), null)
                        })
                    }
                }

                // Temperature
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SheetSectionLabel(stringResource(R.string.sheet_temperature))
                            Text("${temperature.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            onValueChangeFinished = {
                                if (!routineMode) actions.onExecuteAction("setTemperature", mapOf("temperature" to temperature.toInt()), null)
                            },
                            valueRange = 18f..38f
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("18°C", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("38°C", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Mode
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel(stringResource(R.string.sheet_mode))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            modos.forEach { m ->
                                FilterChip(
                                    selected = mode == m,
                                    onClick = {
                                        mode = m
                                        if (!routineMode) actions.onExecuteAction("setMode", mapOf("mode" to m), null)
                                    },
                                    label = {
                                        Text(when (m) {
                                            "ventilacion" -> stringResource(R.string.sheet_mode_fan)
                                            "frio" -> stringResource(R.string.sheet_mode_cold)
                                            "calor" -> stringResource(R.string.sheet_mode_heat)
                                            else -> m
                                        })
                                    }
                                )
                            }
                        }
                    }
                }

                // Vertical swing
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel(stringResource(R.string.sheet_vertical_blades))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            verticalOpts.forEach { v ->
                                FilterChip(
                                    selected = verticalSwing == v,
                                    onClick = {
                                        verticalSwing = v
                                        if (!routineMode) actions.onExecuteAction("setVerticalSwing", mapOf("verticalSwing" to v), null)
                                    },
                                    label = { Text(if (v == "auto") stringResource(R.string.sheet_auto) else "$v°", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Horizontal swing
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel(stringResource(R.string.sheet_horizontal_blades))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            horizontalOpts.forEach { h ->
                                FilterChip(
                                    selected = horizontalSwing == h,
                                    onClick = {
                                        horizontalSwing = h
                                        if (!routineMode) actions.onExecuteAction("setHorizontalSwing", mapOf("horizontalSwing" to h), null)
                                    },
                                    label = { Text(if (h == "auto") stringResource(R.string.sheet_auto) else "$h°", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Fan speed
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel(stringResource(R.string.sheet_fan_speed))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fanOpts.forEach { f ->
                                FilterChip(
                                    selected = fanSpeed == f,
                                    onClick = {
                                        fanSpeed = f
                                        if (!routineMode) actions.onExecuteAction("setFanSpeed", mapOf("fanSpeed" to f), null)
                                    },
                                    label = { Text(if (f == "auto") stringResource(R.string.sheet_auto) else "$f%", style = MaterialTheme.typography.labelSmall) }
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
                                DeviceAction(if (isOn) "turnOn" else "turnOff"),
                                DeviceAction("setTemperature", mapOf("temperature" to temperature.toInt())),
                                DeviceAction("setMode", mapOf("mode" to mode)),
                                DeviceAction("setFanSpeed", mapOf("fanSpeed" to fanSpeed)),
                                DeviceAction("setVerticalSwing", mapOf("verticalSwing" to verticalSwing)),
                                DeviceAction("setHorizontalSwing", mapOf("horizontalSwing" to horizontalSwing))
                            ))
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
}
