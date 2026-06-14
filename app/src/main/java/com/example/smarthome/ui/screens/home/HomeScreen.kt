package com.example.smarthome.ui.screens.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.lazy.LazyListScope
import android.content.res.Configuration
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.LogEntryDto
import com.example.smarthome.data.api.models.RoutineDto
import com.example.smarthome.domain.canToggle
import com.example.smarthome.domain.deviceConsumptionW
import com.example.smarthome.domain.deviceIcon
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.domain.logActionLabelRes
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.UserPreferencesViewModel
import com.example.smarthome.ui.components.ConnectionErrorView
import com.example.smarthome.ui.components.sheets.DeviceSheetActions
import com.example.smarthome.ui.components.sheets.DeviceSheetRouter
import com.example.smarthome.ui.components.truncateName
import com.example.smarthome.ui.navigation.Routes
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Un acceso directo del inicio ya resuelto a su entidad real. */
private sealed interface Shortcut {
    data class DeviceShortcut(val device: DeviceDto) : Shortcut
    data class RoutineShortcut(val routine: RoutineDto) : Shortcut
}

// Acentos de los íconos, iguales a los de la web (InicioView / RutinasView).
private val StatCyan = Color(0xFF1FB6C1)   // habitaciones
private val StatGreen = Color(0xFF16A34A)  // dispositivos
private val StatWarm = Color(0xFFC9A227)   // consumo
private val HoraOrange = Color(0xFFE07A3C) // rutinas por hora
private val EventoPink = Color(0xFFC0568A) // rutinas por evento

private fun deviceToken(id: String) = "d:$id"
private fun routineToken(id: String) = "r:$id"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    prefsViewModel: UserPreferencesViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()
    val routines by appViewModel.routines.collectAsState()
    val isLoading by appViewModel.isLoading.collectAsState()
    val loadError by appViewModel.error.collectAsState()
    val userName by prefsViewModel.userName.collectAsState()
    val savedShortcuts by prefsViewModel.shortcuts.collectAsState()
    var selectedDevice by remember { mutableStateOf<DeviceDto?>(null) }

    val allDevices = remember(devices) { devices.values.flatten() }
    val onDevices = remember(allDevices) { allDevices.filter { isDeviceOn(it.type.id, it.state) } }
    val offDevices = remember(allDevices) { allDevices.filter { !isDeviceOn(it.type.id, it.state) } }
    val totalW = remember(onDevices) { onDevices.sumOf { deviceConsumptionW(it.type.id) } }
    val deviceById = remember(allDevices) { allDevices.associateBy { it.id } }
    val routineById = remember(routines) { routines.associateBy { it.id } }

    var recentLogs by remember { mutableStateOf<List<LogEntryDto>>(emptyList()) }
    LaunchedEffect(Unit) {
        ServiceLocator.historyRepository.getLogs(10, 0).onSuccess { recentLogs = it }
    }

    // Tokens efectivos: lo guardado, o una sugerencia por defecto si nunca se personalizó.
    val effectiveTokens = remember(savedShortcuts, routines, onDevices, offDevices) {
        savedShortcuts ?: defaultShortcutTokens(routines, onDevices + offDevices)
    }
    val resolvedShortcuts = remember(effectiveTokens, routineById, deviceById) {
        effectiveTokens.mapNotNull { token ->
            when {
                token.startsWith("r:") -> routineById[token.removePrefix("r:")]
                    ?.let { Shortcut.RoutineShortcut(it) }
                token.startsWith("d:") -> deviceById[token.removePrefix("d:")]
                    ?.let { Shortcut.DeviceShortcut(it) }
                else -> null
            }
        }
    }
    val toggleShortcut: (String) -> Unit = { token ->
        val base = effectiveTokens.toMutableList()
        if (token in base) base.remove(token) else base.add(token)
        prefsViewModel.setShortcuts(base)
    }

    var showCustomize by remember { mutableStateOf(false) }

    val saludoRes = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 6..11 -> R.string.home_greeting_morning
            in 12..19 -> R.string.home_greeting_afternoon
            else -> R.string.home_greeting_evening
        }
    }
    val greeting = stringResource(saludoRes)
    val firstName = userName?.split(" ")?.firstOrNull() ?: stringResource(R.string.common_user)
    val fecha = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
            .format(Date()).replaceFirstChar { it.uppercase() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isLoading && homes.isEmpty() && loadError != null) {
            ConnectionErrorView(
                message = loadError.orEmpty(),
                onRetry = { appViewModel.retryLoad() }
            )
            return@Box
        }

        if (isLoading && homes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Box
        }

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        // Encabezado con gradiente
        val headerItem: LazyListScope.() -> Unit = {
            item {
                HomeHeader(
                    saludo = greeting,
                    firstName = firstName,
                    fecha = fecha,
                    onProfile = { navController.navigate(Routes.PROFILE) }
                )
            }
        }

        // Tarjeta de estado (anillo animado + encendidos/apagados)
        val statusItem: LazyListScope.() -> Unit = {
            item {
                DeviceStatusCard(
                    onCount = onDevices.size,
                    offCount = offDevices.size,
                    homeCount = homes.size,
                    roomCount = rooms.values.sumOf { it.size },
                    totalW = totalW,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Accesos directos personalizables
        val shortcutsItem: LazyListScope.() -> Unit = {
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.home_shortcuts),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { showCustomize = true }) {
                            Icon(
                                Icons.Rounded.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.home_customize))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (resolvedShortcuts.isEmpty()) {
                        EmptyShortcuts(onAdd = { showCustomize = true })
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            resolvedShortcuts.chunked(2).forEach { rowItems ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    rowItems.forEach { shortcut ->
                                        ShortcutTile(
                                            shortcut = shortcut,
                                            modifier = Modifier.weight(1f),
                                            onDevice = {
                                                if (canToggle(it.type.id)) appViewModel.toggleDevice(it)
                                                else selectedDevice = it
                                            },
                                            onRoutine = { appViewModel.executeRoutine(it.id) }
                                        )
                                    }
                                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Actividad reciente
        val recentItem: LazyListScope.() -> Unit = {
            if (recentLogs.isNotEmpty()) {
                item {
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.home_recent_activity),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { navController.navigate(Routes.HISTORY) }) {
                                Text(stringResource(R.string.home_view_history))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(Modifier.padding(vertical = 4.dp)) {
                                recentLogs.take(5).forEach { log ->
                                    val (_, hora) = formatTimestamp(log.timestamp)
                                    val device = deviceById[log.deviceId]
                                    val deviceName = truncateName(device?.name ?: stringResource(R.string.device_type_generic))
                                    val typeId = device?.type?.id ?: ""
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = deviceIcon(typeId),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                deviceName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                translateAction(log.event),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            hora,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLandscape) {
            // Dos columnas: izquierda con cabecera/estado/stats, derecha con accesos + actividad.
            Row(Modifier.fillMaxSize()) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    headerItem()
                    statusItem()
                }
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    shortcutsItem()
                    recentItem()
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                headerItem()
                statusItem()
                shortcutsItem()
                recentItem()
            }
        }
    }

    if (showCustomize) {
        CustomizeShortcutsSheet(
            routines = routines,
            devices = allDevices,
            selected = effectiveTokens,
            onToggle = toggleShortcut,
            onDismiss = { showCustomize = false }
        )
    }

    selectedDevice?.let { device ->
        DeviceSheetRouter(
            device = device,
            onDismiss = { selectedDevice = null },
            actions = DeviceSheetActions(
                onExecuteAction = { action, params, cb -> appViewModel.executeDeviceAction(device.id, action, params, cb) },
                onRename = { newName, cb -> appViewModel.renameDevice(device.id, newName, cb) },
                onDelete = { cb -> appViewModel.deleteDevice(device.id, cb) },
                onLink = { roomId, cb -> appViewModel.linkDeviceToRoom(device.id, roomId, cb) },
                onUnlink = { cb -> appViewModel.linkDeviceToRoom(device.id, null, cb) },
                onLoad = { id -> appViewModel.loadDevice(id) }
            ),
            homes = homes,
            rooms = rooms
        )
    }
}

/* ----------------------------- Header ----------------------------- */

@Composable
private fun HomeHeader(
    saludo: String,
    firstName: String,
    fecha: String,
    onProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        Column(
            Modifier
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "$saludo,",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Text(
                        firstName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = onProfile)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.AccountCircle,
                            contentDescription = stringResource(R.string.common_profile),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    fecha,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/* ----------------------- Tarjeta de estado ----------------------- */

@Composable
private fun DeviceStatusCard(
    onCount: Int,
    offCount: Int,
    homeCount: Int,
    roomCount: Int,
    totalW: Int,
    modifier: Modifier = Modifier
) {
    val total = onCount + offCount
    var play by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { play = true }

    val targetFraction = if (total > 0) onCount.toFloat() / total else 0f
    val fraction by animateFloatAsState(
        targetValue = if (play) targetFraction else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "ringFraction"
    )
    val animOn by animateIntAsState(
        targetValue = if (play) onCount else 0,
        animationSpec = tween(durationMillis = 900),
        label = "onCount"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.home_status_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(14.dp))
                    StatusLegend(
                        icon = Icons.Rounded.Home,
                        label = stringResource(R.string.nav_homes),
                        value = "$homeCount",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    StatusLegend(
                        icon = Icons.Rounded.SpaceDashboard,
                        label = stringResource(R.string.common_rooms),
                        value = "$roomCount",
                        tint = StatCyan
                    )
                    Spacer(Modifier.height(10.dp))
                    StatusLegend(
                        icon = Icons.Rounded.Bolt,
                        label = stringResource(R.string.consumption_current),
                        value = formatPower(totalW),
                        tint = StatWarm
                    )
                }

                Spacer(Modifier.width(20.dp))

                val ringColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier.size(116.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 14.dp.toPx()
                        val inset = stroke / 2f
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        if (fraction > 0f) {
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = 360f * fraction,
                                useCenter = false,
                                topLeft = Offset(inset, inset),
                                size = arcSize,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$animOn/$total",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (total == 1) stringResource(R.string.home_device_singular) else stringResource(R.string.home_device_plural),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusLegend(
    icon: ImageVector,
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ------------------------ Shortcut tiles ------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShortcutTile(
    shortcut: Shortcut,
    modifier: Modifier = Modifier,
    onDevice: (DeviceDto) -> Unit,
    onRoutine: (RoutineDto) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "tileScale"
    )

    // Tarjetas con ícono lleno sobre acento, leve gradiente cuando están
    // activas y una píldora de estado, al estilo de las acciones rápidas.
    when (shortcut) {
        is Shortcut.DeviceShortcut -> {
            val device = shortcut.device
            val toggleable = canToggle(device.type.id)
            val on = isDeviceOn(device.type.id, device.state)
            val active = if (toggleable) on else true
            val accent = if (active) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline
            ShortcutTileScaffold(
                modifier = modifier.scale(scale),
                interaction = interaction,
                icon = deviceIcon(device.type.id),
                accent = accent,
                active = active,
                badge = when {
                    !toggleable -> stringResource(R.string.common_active)
                    on -> stringResource(R.string.common_on)
                    else -> stringResource(R.string.common_off)
                },
                title = device.name,
                showStatusDot = toggleable,
                onClick = { onDevice(device) }
            )
        }
        is Shortcut.RoutineShortcut -> {
            val routine = shortcut.routine
            val triggerType = routine.metadata?.tipoTrigger ?: "manual"
            val accent = when (triggerType) {
                "hora" -> HoraOrange
                "evento" -> EventoPink
                else -> MaterialTheme.colorScheme.primary
            }
            ShortcutTileScaffold(
                modifier = modifier.scale(scale),
                interaction = interaction,
                icon = if (triggerType == "hora") Icons.Rounded.Schedule else Icons.Rounded.PlayArrow,
                accent = accent,
                active = true,
                badge = stringResource(R.string.home_routine_badge),
                title = routine.name,
                showStatusDot = false,
                onClick = { onRoutine(routine) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShortcutTileScaffold(
    modifier: Modifier,
    interaction: MutableInteractionSource,
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    active: Boolean,
    badge: String,
    title: String,
    showStatusDot: Boolean,
    onClick: () -> Unit
) {
    val f by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "tileActive"
    )
    val neutralChip = MaterialTheme.colorScheme.surfaceVariant
    // Chip del ícono: tintado en reposo, lleno (acento sólido) cuando está activo.
    val iconBg = lerp(neutralChip, accent, f)
    val iconTint = lerp(MaterialTheme.colorScheme.onSurfaceVariant, Color.White, f)

    Card(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier.height(124.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = (1 + 3 * f).dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.16f * f),
                            accent.copy(alpha = 0.04f * f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = iconBg,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    if (showStatusDot) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) accent
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (active) accent.copy(alpha = 0.16f) else neutralChip
                    ) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (active) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyShortcuts(onAdd: () -> Unit) {
    Card(
        onClick = onAdd,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_add_shortcuts_title),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.home_add_shortcuts_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* --------------------- Hoja de personalización -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomizeShortcutsSheet(
    routines: List<RoutineDto>,
    devices: List<DeviceDto>,
    selected: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val q = query.trim()

    val filteredRoutines = remember(routines, q) {
        if (q.isBlank()) routines
        else routines.filter { it.name.contains(q, ignoreCase = true) }
    }
    val filteredDevices = remember(devices, q) {
        if (q.isBlank()) devices
        else devices.filter { it.name.contains(q, ignoreCase = true) }
    }
    val selectedCount = remember(selected) { selected.size }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // --- Cabecera fija con título y contador de seleccionados ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.home_customize_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.home_customize_sub),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (selectedCount > 0) {
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            stringResource(R.string.home_customize_selected_count, selectedCount),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Buscador ---
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.home_customize_search)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.common_clear)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // --- Lista desplazable de resultados ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (routines.isEmpty() && devices.isEmpty()) {
                    Text(
                        stringResource(R.string.home_no_items),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else if (filteredRoutines.isEmpty() && filteredDevices.isEmpty()) {
                    NoResults(query = q)
                } else {
                    if (filteredRoutines.isNotEmpty()) {
                        SheetSectionTitle(stringResource(R.string.nav_routines))
                        filteredRoutines.forEach { routine ->
                            val token = routineToken(routine.id)
                            val triggerType = routine.metadata?.tipoTrigger ?: "manual"
                            PickerRow(
                                icon = if (triggerType == "hora") Icons.Rounded.Schedule else Icons.Rounded.PlayArrow,
                                accent = when (triggerType) {
                                    "hora" -> HoraOrange
                                    "evento" -> EventoPink
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                title = routine.name,
                                checked = token in selected,
                                onToggle = { onToggle(token) }
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    if (filteredDevices.isNotEmpty()) {
                        SheetSectionTitle(stringResource(R.string.nav_devices))
                        filteredDevices.forEach { device ->
                            val token = deviceToken(device.id)
                            PickerRow(
                                icon = deviceIcon(device.type.id),
                                accent = MaterialTheme.colorScheme.primary,
                                title = device.name,
                                checked = token in selected,
                                onToggle = { onToggle(token) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoResults(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.home_customize_no_results, query),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SheetSectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

@Composable
private fun PickerRow(
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    title: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (checked) accent.copy(alpha = 0.10f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.14f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = accent
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
        }
    }
}

/* ---------------------------- Helpers ---------------------------- */

private fun defaultShortcutTokens(
    routines: List<RoutineDto>,
    devices: List<DeviceDto>
): List<String> {
    val r = routines.take(2).map { routineToken(it.id) }
    val d = devices.take(2).map { deviceToken(it.id) }
    return r + d
}

private fun formatPower(w: Int): String =
    if (w >= 1000) "${"%.1f".format(w / 1000f)} kW" else "$w W"

private fun formatTimestamp(ts: String?): Pair<String, String> {
    if (ts == null) return Pair("—", "—")
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(ts) ?: return Pair(ts.take(10), "")
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        Pair(SimpleDateFormat("dd/MM", Locale.getDefault()).format(date), timeFmt.format(date))
    } catch (e: Exception) {
        Pair(ts.take(10), "")
    }
}

@Composable
private fun translateAction(event: String?): String {
    val res = logActionLabelRes(event)
    return if (res != null) stringResource(res) else event ?: "—"
}
