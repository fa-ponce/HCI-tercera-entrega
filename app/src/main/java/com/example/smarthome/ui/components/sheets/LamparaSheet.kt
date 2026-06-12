package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LamparaSheet(
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
    var brightness by remember { mutableFloatStateOf(100f) }
    var hue by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(0f) }

    val color = remember(hue, saturation) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation / 100f, 1f)))
    }

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                val state = d.state
                isOn = state["status"] == "on"
                brightness = ((state["brightness"] as? Double)?.toFloat()
                    ?: (state["brightness"] as? Number)?.toFloat() ?: 100f)
                val colorStr = (state["color"] as? String) ?: "#ffffff"
                parseHexToHsv(colorStr)?.let { hsv ->
                    hue = hsv[0]
                    saturation = hsv[1] * 100f
                }
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SheetHeader(
                title = device.name,
                subtitle = stringResource(R.string.device_type_lamp),
                onRename = if (!routineMode) { newName, cb -> actions.onRename(newName, cb) } else null
            )

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Power toggle
                SheetSectionCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isOn) stringResource(R.string.sheet_on_f) else stringResource(R.string.sheet_off_f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isOn,
                            onCheckedChange = { value ->
                                isOn = value
                                if (!routineMode) actions.onExecuteAction(if (value) "turnOn" else "turnOff", emptyMap(), null)
                            }
                        )
                    }
                }

                // Color picker
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(stringResource(R.string.sheet_color), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Column {
                                Text(stringResource(R.string.sheet_current_color), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    colorToHex(color),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.sheet_hue), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Slider(
                                value = hue,
                                onValueChange = { hue = it },
                                onValueChangeFinished = {
                                    if (!routineMode) actions.onExecuteAction("setColor", mapOf("color" to colorToHex(color)), null)
                                },
                                valueRange = 0f..360f
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.sheet_saturation), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Slider(
                                value = saturation,
                                onValueChange = { saturation = it },
                                onValueChangeFinished = {
                                    if (!routineMode) actions.onExecuteAction("setColor", mapOf("color" to colorToHex(color)), null)
                                },
                                valueRange = 0f..100f
                            )
                        }
                    }
                }

                // Brightness
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.sheet_brightness), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${brightness.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = brightness,
                            onValueChange = { brightness = it },
                            onValueChangeFinished = {
                                if (!routineMode) actions.onExecuteAction("setBrightness", mapOf("brightness" to brightness.toInt()), null)
                            },
                            valueRange = 0f..100f
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("0%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                if (routineMode) {
                    SheetRoutineFooter(
                        onCancel = onDismiss,
                        onAdd = {
                            onAddToRoutine?.invoke(
                                listOf(
                                    DeviceAction(if (isOn) "turnOn" else "turnOff"),
                                    DeviceAction("setBrightness", mapOf("brightness" to brightness.toInt())),
                                    DeviceAction("setColor", mapOf("color" to colorToHex(color)))
                                )
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
}

private fun parseHexToHsv(hex: String): FloatArray? = runCatching {
    val clean = if (hex.startsWith("#")) hex else "#$hex"
    val argb = android.graphics.Color.parseColor(clean)
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb, hsv)
    hsv
}.getOrNull()

private fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return "#%02x%02x%02x".format((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)
}
