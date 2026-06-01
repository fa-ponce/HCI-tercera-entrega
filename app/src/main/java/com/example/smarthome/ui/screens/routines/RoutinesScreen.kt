package com.example.smarthome.ui.screens.routines

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.smarthome.data.api.models.RoutineDto
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.ui.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val routines by appViewModel.routines.collectAsState()
    val scope = rememberCoroutineScope()

    var running by remember { mutableStateOf(setOf<String>()) }
    var builderRoutine by remember { mutableStateOf<RoutineDto?>(null) }
    var showBuilder by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rutinas") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { builderRoutine = null; showBuilder = true },
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("Nueva rutina") }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (routines.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay rutinas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(routines) { routine ->
                val tipo = routine.metadata?.tipoTrigger ?: "manual"
                val isRunning = routine.id in running

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = when (tipo) {
                                "hora" -> MaterialTheme.colorScheme.tertiaryContainer
                                "evento" -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (tipo) {
                                        "hora" -> Icons.Rounded.Schedule
                                        "evento" -> Icons.Rounded.Home
                                        else -> Icons.Rounded.Bolt
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    tint = when (tipo) {
                                        "hora" -> MaterialTheme.colorScheme.onTertiaryContainer
                                        "evento" -> MaterialTheme.colorScheme.onSecondaryContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                routine.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val desc = routine.metadata?.descripcion
                            val trigger = routine.metadata?.trigger
                            val sub = listOfNotNull(trigger, desc).firstOrNull()
                            if (!sub.isNullOrEmpty()) {
                                Text(
                                    sub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                when (tipo) {
                                    "hora" -> "Programada"
                                    "evento" -> "Por evento"
                                    else -> "Manual"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(
                            onClick = { builderRoutine = routine; showBuilder = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, "Editar", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        }

                        FilledIconButton(
                            onClick = {
                                if (!isRunning) {
                                    scope.launch {
                                        running = running + routine.id
                                        appViewModel.executeRoutine(routine.id)
                                        delay(1500)
                                        running = running - routine.id
                                    }
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isRunning)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                "Ejecutar",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBuilder) {
        RoutineBuilderSheet(
            routine = builderRoutine,
            appViewModel = appViewModel,
            onDismiss = { showBuilder = false; builderRoutine = null }
        )
    }
}
