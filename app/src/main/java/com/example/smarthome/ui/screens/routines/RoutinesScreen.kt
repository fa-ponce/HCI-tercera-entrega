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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.CenterAlignedTopAppBar
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.components.ConnectionErrorView
import com.example.smarthome.ui.components.FullScreenLoading
import com.example.smarthome.ui.navigation.Routes
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
    val isLoading by appViewModel.isLoading.collectAsState()
    val error by appViewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    var running by remember { mutableStateOf(setOf<String>()) }
    var builderRoutine by remember { mutableStateOf<RoutineDto?>(null) }
    var showBuilder by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.appBarGradient(),
                title = {
                    Text(
                        stringResource(R.string.nav_routines),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = stringResource(R.string.common_profile), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = gradientTopBarColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { builderRoutine = null; showBuilder = true },
                icon = { Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.onPrimary) },
                text = { Text(stringResource(R.string.routine_new), color = MaterialTheme.colorScheme.onPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        val isExpanded = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
        val twoColumns = isLandscape || isExpanded

        val onExecute: (RoutineDto) -> Unit = { routine ->
            if (routine.id !in running) {
                scope.launch {
                    running = running + routine.id
                    appViewModel.executeRoutine(routine.id)
                    delay(1500)
                    running = running - routine.id
                }
            }
        }
        val onOpen: (RoutineDto) -> Unit = { routine -> builderRoutine = routine; showBuilder = true }

        if (!isLoading && routines.isEmpty() && error != null) {
            ConnectionErrorView(
                message = error!!,
                onRetry = { appViewModel.retryLoad() },
                modifier = Modifier.padding(innerPadding)
            )
        } else if (isLoading && routines.isEmpty()) {
            FullScreenLoading(Modifier.padding(innerPadding))
        } else if (routines.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(top = 48.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(stringResource(R.string.routine_none), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (twoColumns) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    start = 16.dp, end = 16.dp, bottom = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                gridItems(routines, key = { it.id }) { routine ->
                    RoutineCard(routine, routine.id in running, onOpen, onExecute)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    start = 16.dp, end = 16.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(routines, key = { it.id }) { routine ->
                    RoutineCard(routine, routine.id in running, onOpen, onExecute)
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

@Composable
private fun RoutineCard(
    routine: RoutineDto,
    isRunning: Boolean,
    onOpen: (RoutineDto) -> Unit,
    onExecute: (RoutineDto) -> Unit
) {
    val tipo = routine.metadata?.tipoTrigger ?: "manual"
    Card(
        onClick = { onOpen(routine) },
        modifier = Modifier.fillMaxWidth()
    ) {
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
                        "hora" -> stringResource(R.string.routine_scheduled)
                        "evento" -> stringResource(R.string.routine_by_event)
                        else -> stringResource(R.string.routine_manual)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            FilledIconButton(
                onClick = { onExecute(routine) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRunning)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(40.dp)
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        stringResource(R.string.routine_execute),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
