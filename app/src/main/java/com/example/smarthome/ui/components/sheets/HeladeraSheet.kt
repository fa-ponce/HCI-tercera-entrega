package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.BorderStroke
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
fun HeladeraSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    var fridgeTemp by remember { mutableFloatStateOf(4f) }
    var freezerTemp by remember { mutableFloatStateOf(-8f) }
    var modo by remember { mutableStateOf("normal") }

    val modos = listOf(
        Triple("normal", R.string.sheet_mode_normal, R.string.sheet_mode_normal_desc),
        Triple("fiesta", R.string.sheet_mode_party, R.string.sheet_mode_party_desc),
        Triple("vacaciones", R.string.sheet_mode_vacation, R.string.sheet_mode_vacation_desc)
    )

    val isLoading = rememberDeviceLoading(device, routineMode, actions) { d ->
        val s = d.state
        fridgeTemp = ((s["temperature"] as? Double)?.toFloat() ?: 4f)
        freezerTemp = ((s["freezerTemperature"] as? Double)?.toFloat() ?: -8f)
        modo = (s["mode"] as? String) ?: "normal"
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
                    DeviceAction("setTemperature", mapOf("temperature" to fridgeTemp.toInt())),
                    DeviceAction("setFreezerTemperature", mapOf("temperature" to freezerTemp.toInt())),
                    DeviceAction("setMode", mapOf("mode" to modo))
                ))
                onDismiss()
            }
        } else null
    ) {
        SheetSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SheetSectionLabel(stringResource(R.string.sheet_fridge_temp))
                    Text("${fridgeTemp.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = fridgeTemp,
                    onValueChange = { fridgeTemp = it },
                    onValueChangeFinished = {
                        if (!routineMode) actions.onExecuteAction("setTemperature", mapOf("temperature" to fridgeTemp.toInt()), null)
                    },
                    valueRange = 2f..8f
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.sheet_fridge_temp_min), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(stringResource(R.string.sheet_fridge_temp_max), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        SheetSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SheetSectionLabel(stringResource(R.string.sheet_freezer_temp))
                    Text("${freezerTemp.toInt()}°C", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Slider(
                    value = freezerTemp,
                    onValueChange = { freezerTemp = it },
                    onValueChangeFinished = {
                        if (!routineMode) actions.onExecuteAction("setFreezerTemperature", mapOf("temperature" to freezerTemp.toInt()), null)
                    },
                    valueRange = -20f..-8f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary, activeTrackColor = MaterialTheme.colorScheme.secondary)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.sheet_freezer_temp_min), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(stringResource(R.string.sheet_freezer_temp_max), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        SheetSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SheetSectionLabel(stringResource(R.string.sheet_operation_mode))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    modos.forEach { (value, labelRes, descRes) ->
                        val active = modo == value
                        Surface(
                            onClick = {
                                if (modo != value) {
                                    modo = value
                                    if (!routineMode) actions.onExecuteAction("setMode", mapOf("mode" to value), null)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (active) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Column(
                                Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(stringResource(labelRes), style = MaterialTheme.typography.labelMedium, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                                Text(stringResource(descRes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}
