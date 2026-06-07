package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HornoSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null,
    onDeviceDeleted: ((String) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ServiceLocator.deviceRepository }

    var isLoading by remember { mutableStateOf(!routineMode) }
    var isOn by remember { mutableStateOf(false) }
    var temperature by remember { mutableFloatStateOf(90f) }
    var heat by remember { mutableStateOf("convencional") }
    var grill by remember { mutableStateOf("off") }
    var convection by remember { mutableStateOf("off") }

    val heatOpts = listOf("convencional" to "Convencional", "abajo" to "Abajo", "arriba" to "Arriba")
    val grillOpts = listOf("off" to "Apagado", "eco" to "Economico", "large" to "Completo")
    val convectionOpts = listOf("off" to "Apagado", "eco" to "Economico", "conventional" to "Convencional")

    // API returns English values for heat, normalize on load
    val heatNorm = mapOf("conventional" to "convencional", "top" to "arriba", "bottom" to "abajo")

    LaunchedEffect(device.id) {
        if (!routineMode) {
            repo.getDevice(device.id).onSuccess { d ->
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
            SheetHeader(title = device.name, subtitle = "Horno", deviceId = if (!routineMode) device.id else null, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Power
                SheetSectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isOn) "Encendido" else "Apagado", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Switch(checked = isOn, onCheckedChange = { v ->
                            isOn = v
                            if (!routineMode) scope.launch { repo.executeAction(device.id, if (v) "turnOn" else "turnOff") }
                        })
                    }
                }

                // Temperature
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SheetSectionLabel("Temperatura")
                            Text("${temperature.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = temperature,
                            onValueChange = { temperature = it },
                            onValueChangeFinished = {
                                if (!routineMode) scope.launch { repo.executeAction(device.id, "setTemperature", mapOf("temperature" to temperature.toInt())) }
                            },
                            valueRange = 90f..230f
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("90°C", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("230°C", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Heat source
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Fuente de calor")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            heatOpts.forEach { (value, label) ->
                                FilterChip(
                                    selected = heat == value,
                                    onClick = {
                                        heat = value
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setHeat", mapOf("heat" to value)) }
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Grill
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Modo grill")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            grillOpts.forEach { (value, label) ->
                                FilterChip(
                                    selected = grill == value,
                                    onClick = {
                                        grill = value
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setGrill", mapOf("grill" to value)) }
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Convection
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Modo conveccion")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            convectionOpts.forEach { (value, label) ->
                                FilterChip(
                                    selected = convection == value,
                                    onClick = {
                                        convection = value
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setConvection", mapOf("convection" to value)) }
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
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
                    SheetDeleteButton(deviceId = device.id, onDismiss = onDismiss, onDeleted = onDeviceDeleted)
                }
            }
        }
    }
}
