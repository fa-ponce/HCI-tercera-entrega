package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
fun ParlanteSheet(
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
    var status by remember { mutableStateOf("stopped") }
    var volume by remember { mutableIntStateOf(5) }
    var genre by remember { mutableStateOf("pop") }
    var songTitle by remember { mutableStateOf("") }
    var songArtist by remember { mutableStateOf("") }

    val genres = listOf(
        "clasica" to "Clasica", "country" to "Country", "dance" to "Dance",
        "latina" to "Latina", "pop" to "Pop", "rock" to "Rock",
        "folk" to "Folk", "jazz" to "Jazz", "blues" to "Blues"
    )

    val isPlaying = status == "playing"
    val isPaused = status == "paused"
    val isStopped = status == "stopped"

    LaunchedEffect(device.id) {
        if (!routineMode) {
            repo.getDevice(device.id).onSuccess { d ->
                val s = d.state
                status = (s["status"] as? String) ?: "stopped"
                volume = ((s["volume"] as? Double)?.toInt() ?: 5).coerceIn(0, 10)
                genre = (s["genre"] as? String) ?: "pop"
                val song = s["song"] as? Map<*, *>
                songTitle = (song?.get("title") as? String) ?: ""
                songArtist = (song?.get("artist") as? String) ?: ""
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
            SheetHeader(title = device.name, subtitle = "Parlante", deviceId = if (!routineMode) device.id else null, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Now playing (normal mode only)
                if (!routineMode && songTitle.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.inverseSurface,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val statusLabel = when (status) {
                                "playing" -> "Reproduciendo"
                                "paused" -> "Pausado"
                                else -> "Detenido"
                            }
                            Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f))
                            Text(songTitle, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.inverseOnSurface)
                            Text(songArtist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f))
                        }
                    }
                }

                // Transport controls
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Reproduccion")
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous
                            IconButton(
                                onClick = {
                                    if (!routineMode) scope.launch { repo.executeAction(device.id, "previousSong") }
                                },
                                enabled = routineMode || isPlaying
                            ) { Icon(Icons.Rounded.SkipPrevious, "Anterior", Modifier.size(28.dp)) }

                            // Stop
                            IconButton(
                                onClick = {
                                    if (routineMode) { status = "stopped" } else {
                                        scope.launch { repo.executeAction(device.id, "stop"); status = "stopped" }
                                    }
                                },
                                enabled = routineMode || !isStopped
                            ) { Icon(Icons.Rounded.Stop, "Detener", Modifier.size(28.dp), tint = MaterialTheme.colorScheme.error) }

                            // Play / Resume
                            FilledIconButton(
                                onClick = {
                                    val action = if (isPaused) "resume" else "play"
                                    if (routineMode) { status = "playing" } else {
                                        scope.launch { repo.executeAction(device.id, action); status = "playing" }
                                    }
                                },
                                modifier = Modifier.size(52.dp),
                                enabled = routineMode || !isPlaying
                            ) { Icon(Icons.Rounded.PlayArrow, "Reproducir", Modifier.size(32.dp)) }

                            // Pause
                            IconButton(
                                onClick = {
                                    if (routineMode) { status = "paused" } else {
                                        scope.launch { repo.executeAction(device.id, "pause"); status = "paused" }
                                    }
                                },
                                enabled = routineMode || isPlaying
                            ) { Icon(Icons.Rounded.Pause, "Pausar", Modifier.size(28.dp), tint = MaterialTheme.colorScheme.tertiary) }

                            // Next
                            IconButton(
                                onClick = {
                                    if (!routineMode) scope.launch { repo.executeAction(device.id, "nextSong") }
                                },
                                enabled = routineMode || isPlaying
                            ) { Icon(Icons.Rounded.SkipNext, "Siguiente", Modifier.size(28.dp)) }
                        }
                    }
                }

                // Volume
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            SheetSectionLabel("Volumen")
                            Text("$volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    if (volume > 0) {
                                        volume--
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "volumeDown") }
                                    }
                                },
                                enabled = volume > 0
                            ) { Icon(Icons.Rounded.VolumeDown, null) }
                            Slider(
                                value = volume.toFloat(),
                                onValueChange = { volume = it.toInt() },
                                onValueChangeFinished = {
                                    if (!routineMode) scope.launch { repo.executeAction(device.id, "setVolume", mapOf("volume" to volume)) }
                                },
                                valueRange = 0f..10f,
                                steps = 9,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (volume < 10) {
                                        volume++
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "volumeUp") }
                                    }
                                },
                                enabled = volume < 10
                            ) { Icon(Icons.Rounded.VolumeUp, null) }
                        }
                    }
                }

                // Genre
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetSectionLabel("Genero")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            genres.forEach { (value, label) ->
                                FilterChip(
                                    selected = genre == value,
                                    onClick = {
                                        genre = value
                                        if (!routineMode) scope.launch { repo.executeAction(device.id, "setGenre", mapOf("genre" to value)) }
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
                            val primaryAction = if (isPlaying) "play" else "stop"
                            onAddToRoutine?.invoke(listOf(
                                DeviceAction(primaryAction),
                                DeviceAction("setVolume", mapOf("volume" to volume)),
                                DeviceAction("setGenre", mapOf("genre" to genre))
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
