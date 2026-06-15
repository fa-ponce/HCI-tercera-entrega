package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
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

    val isLoading = rememberDeviceLoading(device, routineMode, actions) { d ->
        val s = d.state
        isOn = s["status"] == "on"
        temperature = ((s["temperature"] as? Double)?.toFloat() ?: 90f)
        val rawHeat = (s["heat"] as? String) ?: "convencional"
        heat = heatNorm[rawHeat] ?: rawHeat
        grill = (s["grill"] as? String) ?: "off"
        convection = (s["convection"] as? String) ?: "off"
    }

    BaseDeviceSheet(
        device = device,
        routineMode = routineMode,
        onDismiss = onDismiss,
        actions = actions,
        homes = homes,
        rooms = rooms,
        isLoading = isLoading,
        onAddToRoutine = if (routineMode) {
            {
                onAddToRoutine?.invoke(listOf(
                    DeviceAction(if (isOn) "turnOn" else "turnOff"),
                    DeviceAction("setTemperature", mapOf("temperature" to temperature.toInt())),
                    DeviceAction("setHeat", mapOf("heat" to heat)),
                    DeviceAction("setGrill", mapOf("grill" to grill)),
                    DeviceAction("setConvection", mapOf("convection" to convection))
                ))
                onDismiss()
            }
        } else null
    ) {
        SheetSectionCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isOn) stringResource(R.string.common_on) else stringResource(R.string.common_off), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Switch(checked = isOn, onCheckedChange = { v ->
                    isOn = v
                    if (!routineMode) actions.onExecuteAction(if (v) "turnOn" else "turnOff", emptyMap(), null)
                })
            }
        }

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

        // Fuente de calor, grill y convección comparten exactamente la misma fila de
        // chips, así que las tres usan el helper común SheetChipSelector.
        SheetChipSelector(stringResource(R.string.sheet_heat_source), heatOpts, heat) { v ->
            heat = v
            if (!routineMode) actions.onExecuteAction("setHeat", mapOf("heat" to v), null)
        }

        SheetChipSelector(stringResource(R.string.sheet_grill_mode), grillOpts, grill) { v ->
            grill = v
            if (!routineMode) actions.onExecuteAction("setGrill", mapOf("grill" to v), null)
        }

        SheetChipSelector(stringResource(R.string.sheet_convection_mode), convectionOpts, convection) { v ->
            convection = v
            if (!routineMode) actions.onExecuteAction("setConvection", mapOf("convection" to v), null)
        }
    }
}
