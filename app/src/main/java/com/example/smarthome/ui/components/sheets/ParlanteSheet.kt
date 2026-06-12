package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
fun ParlanteSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    var isLoading by remember { mutableStateOf(!routineMode) }
    var status by remember { mutableStateOf("stopped") }
    var volume by remember { mutableIntStateOf(5) }
    var genre by remember { mutableStateOf("pop") }
    var songTitle by remember { mutableStateOf("") }
    var songArtist by remember { mutableStateOf("") }

    val genres = listOf(
        "clasica" to R.string.sheet_genre_classical, "country" to R.string.sheet_genre_country,
        "dance" to R.string.sheet_genre_dance, "latina" to R.string.sheet_genre_latin,
        "pop" to R.string.sheet_genre_pop, "rock" to R.string.sheet_genre_rock,
        "folk" to R.string.sheet_genre_folk, "jazz" to R.string.sheet_genre_jazz,
        "blues" to R.string.sheet_genre_blues
    )

    val isPlaying = status == "playing"
    val isPaused = status == "paused"
    val isStopped = status == "stopped"

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
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
                val primaryAction = if (isPlaying) "play" else "stop"
                onAddToRoutine?.invoke(listOf(
                    DeviceAction(primaryAction),
                    DeviceAction("setVolume", mapOf("volume" to volume)),
                    DeviceAction("setGenre", mapOf("genre" to genre))
                ))
                onDismiss()
            }
        } else null
    ) {
        // Now playing (normal mode only)
        if (!routineMode && songTitle.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val statusLabel = when (status) {
                        "playing" -> stringResource(R.string.action_playing)
                        "paused" -> stringResource(R.string.action_paused)
                        else -> stringResource(R.string.action_stopped)
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
                SheetSectionLabel(stringResource(R.string.sheet_playback))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    IconButton(
                        onClick = {
                            if (!routineMode) actions.onExecuteAction("previousSong", emptyMap(), null)
                        },
                        enabled = routineMode || isPlaying
                    ) { Icon(Icons.Rounded.SkipPrevious, stringResource(R.string.sheet_previous), Modifier.size(28.dp)) }

                    // Stop
                    IconButton(
                        onClick = {
                            if (routineMode) { status = "stopped" } else {
                                actions.onExecuteAction("stop", emptyMap(), null)
                                status = "stopped"
                            }
                        },
                        enabled = routineMode || !isStopped
                    ) { Icon(Icons.Rounded.Stop, stringResource(R.string.sheet_stop), Modifier.size(28.dp), tint = MaterialTheme.colorScheme.error) }

                    // Play / Resume
                    FilledIconButton(
                        onClick = {
                            val action = if (isPaused) "resume" else "play"
                            if (routineMode) { status = "playing" } else {
                                actions.onExecuteAction(action, emptyMap(), null)
                                status = "playing"
                            }
                        },
                        modifier = Modifier.size(52.dp),
                        enabled = routineMode || !isPlaying
                    ) { Icon(Icons.Rounded.PlayArrow, stringResource(R.string.sheet_play), Modifier.size(32.dp)) }

                    // Pause
                    IconButton(
                        onClick = {
                            if (routineMode) { status = "paused" } else {
                                actions.onExecuteAction("pause", emptyMap(), null)
                                status = "paused"
                            }
                        },
                        enabled = routineMode || isPlaying
                    ) { Icon(Icons.Rounded.Pause, stringResource(R.string.sheet_pause), Modifier.size(28.dp), tint = MaterialTheme.colorScheme.tertiary) }

                    // Next
                    IconButton(
                        onClick = {
                            if (!routineMode) actions.onExecuteAction("nextSong", emptyMap(), null)
                        },
                        enabled = routineMode || isPlaying
                    ) { Icon(Icons.Rounded.SkipNext, stringResource(R.string.sheet_next), Modifier.size(28.dp)) }
                }
            }
        }

        // Volume
        SheetSectionCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SheetSectionLabel(stringResource(R.string.sheet_volume))
                    Text("$volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            if (volume > 0) {
                                volume--
                                if (!routineMode) actions.onExecuteAction("volumeDown", emptyMap(), null)
                            }
                        },
                        enabled = volume > 0
                    ) { Icon(Icons.Rounded.VolumeDown, null) }
                    Slider(
                        value = volume.toFloat(),
                        onValueChange = { volume = it.toInt() },
                        onValueChangeFinished = {
                            if (!routineMode) actions.onExecuteAction("setVolume", mapOf("volume" to volume), null)
                        },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (volume < 10) {
                                volume++
                                if (!routineMode) actions.onExecuteAction("volumeUp", emptyMap(), null)
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
                SheetSectionLabel(stringResource(R.string.sheet_genre))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    genres.forEach { (value, labelRes) ->
                        FilterChip(
                            selected = genre == value,
                            onClick = {
                                genre = value
                                if (!routineMode) actions.onExecuteAction("setGenre", mapOf("genre" to value), null)
                            },
                            label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}
