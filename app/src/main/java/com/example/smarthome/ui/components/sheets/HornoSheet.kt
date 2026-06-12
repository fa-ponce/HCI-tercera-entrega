package com.example.smarthome.ui.components.sheets

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
fun HornoSheet(
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
    var temperature by remember { mutableFloatStateOf(90f) }
    var heat by remember { mutableStateOf("convencional") }
    var grill by remember { mutableStateOf("off") }
    var convection by remember { mutableStateOf("off") }

    val heatOpts = listOf(
        "convencional" to R.string.sheet_heat_conventional,
        "abajo" to R.string.sheet_heat_bottom,
        "arriba" to R.string.sheet_heat_top
    )
    val grillOpts = listOf(
        "off" to R.string.common_off,
        "eco" to R.string.sheet_eco,
        "large" to R.string.sheet_grill_full
    )
    val convectionOpts = listOf(
        "off" to R.string.common_off,
        "eco" to R.string.sheet_eco,
        "conventional" to R.string.sheet_heat_conventional
    )

    val heatNorm = mapOf("conventional" to "convencional", "top" to "arriba", "bottom" to "abajo")

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                val s = d.state
                isOn = s["status"] == "on"
                temperature = ((s["temperature"] as? Double)?.toFloat() ?: 90f)
                val rawHeat = (s["heat"] as? String) ?: "convencional"
                heat = heatNorm[rawHeat] ?: rawHeat
                grill = (s["grill"] as? String) ?: "off"
                convection = (s["convection"] as? String) ?: "off"
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
                subtitle = stringResource(R.string.device_type_oven),
                onRename = if (!routineMode) { newName, cb -> actions.onRename(newName, cb) } else null
            )

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Power
                SheetSectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                            valueRange = 90f..230f
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.sheet_oven_temp_min), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(stringResource(R.string.sheet_oven_temp_max), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Heat source
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel(stringResource(R.string.sheet_heat_source))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            heatOpts.forEach { (value, labelRes) ->
                                FilterChip(
                                    selected = heat == value,
                                    onClick = {
                                        heat = value
                                        if (!routineMode) actions.onExecuteAction("setHeat", mapOf("heat" to value), null)
                                    },
                                    label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Grill
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel(stringResource(R.string.sheet_grill_mode))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            grillOpts.forEach { (value, labelRes) ->
                                FilterChip(
                                    selected = grill == value,
                                    onClick = {
                                        grill = value
                                        if (!routineMode) actions.onExecuteAction("setGrill", mapOf("grill" to value), null)
                                    },
                                    label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Convection
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel(stringResource(R.string.sheet_convection_mode))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            convectionOpts.forEach { (value, labelRes) ->
                                FilterChip(
                                    selected = convection == value,
                                    onClick = {
                                        convection = value
                                        if (!routineMode) actions.onExecuteAction("setConvection", mapOf("convection" to value), null)
                                    },
                                    label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) }
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
                                DeviceAction("setHeat", mapOf("heat" to heat)),
                                DeviceAction("setGrill", mapOf("grill" to grill)),
                                DeviceAction("setConvection", mapOf("convection" to convection))
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
