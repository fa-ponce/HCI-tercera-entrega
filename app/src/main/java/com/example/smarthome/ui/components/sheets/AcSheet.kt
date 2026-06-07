package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.horizontalScroll
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
fun AcSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ServiceLocator.deviceRepository }

    var isLoading by remember { mutableStateOf(!routineMode) }
    var isOn by remember { mutableStateOf(false) }
    var temperature by remember { mutableFloatStateOf(24f) }
    var mode by remember { mutableStateOf("frio") }
    var verticalSwing by remember { mutableStateOf("auto") }
    var horizontalSwing by remember { mutableStateOf("auto") }
    var fanSpeed by remember { mutableStateOf("auto") }

    val modos = listOf("ventilacion", "frio", "calor")
    val modoLabels = mapOf("ventilacion" to "Ventilacion", "frio" to "Frio", "calor" to "Calor")
    val verticalOpts = listOf("auto", "22", "45", "67", "90")
    val horizontalOpts = listOf("auto", "-90", "-45", "0", "45", "90")
    val fanOpts = listOf("auto", "25", "50", "75", "100")
    val fanLabels = mapOf("auto" to "Auto", "25" to "25%", "50" to "50%", "75" to "75%", "100" to "100%")

    LaunchedEffect(device.id) {
        if (!routineMode) {
            repo.getDevice(device.id).onSuccess { d ->
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
            SheetHeader(title = device.name, subtitle = "Aire acondicionado", deviceId = if (!routineMode) device.id else null, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })

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
                        SheetSectionLabel("Modo")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            modos.forEach { m ->
                                FilterChip(
                                    selected = mode == m,
                                    onClick = {
                                        mode = m
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setMode", mapOf("mode" to m)) }
                                    },
                                    label = { Text(modoLabels[m] ?: m) }
                                )
                            }
                        }
                    }
                }

                // Vertical swing
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Aspas verticales")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            verticalOpts.forEach { v ->
                                FilterChip(
                                    selected = verticalSwing == v,
                                    onClick = {
                                        verticalSwing = v
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setVerticalSwing", mapOf("verticalSwing" to v)) }
                                    },
                                    label = { Text(if (v == "auto") "Auto" else "$v°", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Horizontal swing
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Aspas horizontales")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            horizontalOpts.forEach { h ->
                                FilterChip(
                                    selected = horizontalSwing == h,
                                    onClick = {
                                        horizontalSwing = h
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setHorizontalSwing", mapOf("horizontalSwing" to h)) }
                                    },
                                    label = { Text(if (h == "auto") "Auto" else "$h°", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Fan speed
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Velocidad del ventilador")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fanOpts.forEach { f ->
                                FilterChip(
                                    selected = fanSpeed == f,
                                    onClick = {
                                        fanSpeed = f
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setFanSpeed", mapOf("fanSpeed" to f)) }
                                    },
                                    label = { Text(fanLabels[f] ?: f, style = MaterialTheme.typography.labelSmall) }
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
                }
            }
        }
    }
}
