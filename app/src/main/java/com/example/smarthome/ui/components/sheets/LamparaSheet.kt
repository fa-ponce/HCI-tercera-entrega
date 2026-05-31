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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LamparaSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ServiceLocator.deviceRepository }

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
            repo.getDevice(device.id).onSuccess { d ->
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
            SheetHeader(title = device.name, subtitle = "Lampara")

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
                            if (isOn) "Encendida" else "Apagada",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isOn,
                            onCheckedChange = { value ->
                                isOn = value
                                if (!routineMode) {
                                    scope.launch {
                                        repo.executeAction(device.id, if (value) "turnOn" else "turnOff")
                                    }
                                }
                            }
                        )
                    }
                }

                // Color picker
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Color", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Column {
                                Text("Color actual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Text(
                                    colorToHex(color),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Hue slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tono", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Slider(
                                value = hue,
                                onValueChange = { hue = it },
                                onValueChangeFinished = {
                                    if (!routineMode) {
                                        scope.launch {
                                            repo.executeAction(device.id, "setColor", mapOf("color" to colorToHex(color)))
                                        }
                                    }
                                },
                                valueRange = 0f..360f
                            )
                        }

                        // Saturation slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Saturacion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Slider(
                                value = saturation,
                                onValueChange = { saturation = it },
                                onValueChangeFinished = {
                                    if (!routineMode) {
                                        scope.launch {
                                            repo.executeAction(device.id, "setColor", mapOf("color" to colorToHex(color)))
                                        }
                                    }
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
                            Text("Intensidad", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${brightness.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = brightness,
                            onValueChange = { brightness = it },
                            onValueChangeFinished = {
                                if (!routineMode) {
                                    scope.launch {
                                        repo.executeAction(device.id, "setBrightness", mapOf("brightness" to brightness.toInt()))
                                    }
                                }
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
