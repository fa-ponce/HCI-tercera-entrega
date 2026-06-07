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
fun HeladeraSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ServiceLocator.deviceRepository }

    var isLoading by remember { mutableStateOf(!routineMode) }
    var fridgeTemp by remember { mutableFloatStateOf(4f) }
    var freezerTemp by remember { mutableFloatStateOf(-8f) }
    var modo by remember { mutableStateOf("normal") }

    val modos = listOf(
        Triple("normal", "Normal", "Operacion estandar"),
        Triple("fiesta", "Fiesta", "Reduce temp. del freezer"),
        Triple("vacaciones", "Vacaciones", "Ahorra energia")
    )

    LaunchedEffect(device.id) {
        if (!routineMode) {
            repo.getDevice(device.id).onSuccess { d ->
                val s = d.state
                fridgeTemp = ((s["temperature"] as? Double)?.toFloat() ?: 4f)
                freezerTemp = ((s["freezerTemperature"] as? Double)?.toFloat() ?: -8f)
                modo = (s["mode"] as? String) ?: "normal"
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
            SheetHeader(title = device.name, subtitle = "Heladera", deviceId = if (!routineMode) device.id else null, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Fridge temperature
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SheetSectionLabel("Temperatura Heladera")
                            Text("${fridgeTemp.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = fridgeTemp,
                            onValueChange = { fridgeTemp = it },
                            onValueChangeFinished = {
                                if (!routineMode) scope.launch { repo.executeAction(device.id, "setTemperature", mapOf("temperature" to fridgeTemp.toInt())) }
                            },
                            valueRange = 2f..8f
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("2°C", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("8°C", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Freezer temperature
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SheetSectionLabel("Temperatura Freezer")
                            Text("${freezerTemp.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Slider(
                            value = freezerTemp,
                            onValueChange = { freezerTemp = it },
                            onValueChangeFinished = {
                                if (!routineMode) scope.launch { repo.executeAction(device.id, "setFreezerTemperature", mapOf("temperature" to freezerTemp.toInt())) }
                            },
                            valueRange = -20f..-8f,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary, activeTrackColor = MaterialTheme.colorScheme.secondary)
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("-20°C", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("-8°C", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Mode
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SheetSectionLabel("Modo de operacion")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            modos.forEach { (value, label, desc) ->
                                val active = modo == value
                                Surface(
                                    onClick = {
                                        if (modo != value) {
                                            modo = value
                                            if (!routineMode) scope.launch { repo.executeAction(device.id, "setMode", mapOf("mode" to value)) }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (active) ButtonDefaults.outlinedButtonBorder(true) else null
                                ) {
                                    Column(
                                        Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                                        Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
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
                            onAddToRoutine?.invoke(listOf(
                                DeviceAction("setTemperature", mapOf("temperature" to fridgeTemp.toInt())),
                                DeviceAction("setFreezerTemperature", mapOf("temperature" to freezerTemp.toInt())),
                                DeviceAction("setMode", mapOf("mode" to modo))
                            ))
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}
