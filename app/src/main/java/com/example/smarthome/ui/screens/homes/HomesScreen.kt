package com.example.smarthome.ui.screens.homes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.AppStrings
import com.example.smarthome.R
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.domain.roomTypeIcon
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.components.AppClickablePanel
import com.example.smarthome.ui.components.AppFabListBottomPadding
import com.example.smarthome.ui.components.AppIconShape
import com.example.smarthome.ui.components.AppPillShape
import com.example.smarthome.ui.components.AppScreenHorizontalPadding
import com.example.smarthome.ui.components.AppSearchField
import com.example.smarthome.ui.components.AppSectionHeader
import com.example.smarthome.ui.components.ConnectionErrorView
import com.example.smarthome.ui.components.FullScreenLoading
import com.example.smarthome.ui.navigation.Routes
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Button
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomesScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val homes by appViewModel.homes.collectAsState()
    val rooms by appViewModel.rooms.collectAsState()
    val devices by appViewModel.devices.collectAsState()
    val standaloneRooms by appViewModel.standaloneRooms.collectAsState()
    val isLoading by appViewModel.isLoading.collectAsState()
    val error by appViewModel.error.collectAsState()
    val scope = rememberCoroutineScope()

    var search by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showRoomDialog by remember { mutableStateOf(false) }

    // En tablets (ancho EXPANDED) mostramos lista + detalle en dos paneles.
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    var selectedHomeId by remember { mutableStateOf<String?>(null) }
    // Si la casa seleccionada deja de existir (p. ej. tras borrarla), limpiar.
    if (selectedHomeId != null && homes.none { it.id == selectedHomeId }) {
        selectedHomeId = null
    }

    val filtered = remember(homes, search) {
        if (search.isBlank()) homes
        else homes.filter { home ->
            home.name.contains(search, ignoreCase = true) ||
            (home.metadata?.city ?: "").contains(search, ignoreCase = true) ||
            (home.metadata?.address ?: "").contains(search, ignoreCase = true)
        }
    }

    val filteredStandalone = remember(standaloneRooms, search) {
        if (search.isBlank()) standaloneRooms
        else standaloneRooms.filter { it.name.contains(search, ignoreCase = true) }
    }

    if (showDialog) {
        NewHomeDialog(
            onDismiss = { showDialog = false },
            onCreate = { name, type, address, city, onResult ->
                // La API permite casas con nombre repetido; lo bloqueamos del lado de la app.
                if (homes.any { it.name.equals(name, ignoreCase = true) }) {
                    onResult(false, AppStrings.get(R.string.err_already_exists))
                } else {
                    scope.launch {
                        ServiceLocator.homeRepository.createHome(name, type, address, city)
                            .onSuccess {
                                appViewModel.addHome(it)
                                showDialog = false
                                onResult(true, null)
                            }
                            .onFailure { onResult(false, it.message) }
                    }
                }
            }
        )
    }

    if (showRoomDialog) {
        NewRoomDialog(
            homes = homes,
            onDismiss = { showRoomDialog = false },
            onCreate = { name, type, floor, homeId, onResult ->
                // No permitimos habitaciones con nombre repetido dentro de la misma casa
                // (ni entre las "sin casa"). La API sí las permitiría.
                val existing = if (homeId == null) standaloneRooms else (rooms[homeId] ?: emptyList())
                if (existing.any { it.name.equals(name, ignoreCase = true) }) {
                    onResult(false, AppStrings.get(R.string.err_already_exists))
                } else if (homeId == null) {
                    appViewModel.createStandaloneRoom(name, type, floor) { ok, err ->
                        if (ok) showRoomDialog = false
                        onResult(ok, err)
                    }
                } else {
                    scope.launch {
                        ServiceLocator.homeRepository.createRoom(name, type, floor, homeId)
                            .onSuccess {
                                appViewModel.addRoom(homeId, it)
                                showRoomDialog = false
                                onResult(true, null)
                            }
                            .onFailure { onResult(false, it.message) }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.appBarGradient(),
                title = {
                    Text(
                        stringResource(R.string.nav_homes),
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
                onClick = { showDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary) },
                text = { Text(stringResource(R.string.homes_new_home), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        if (!isLoading && homes.isEmpty() && error != null) {
            ConnectionErrorView(
                message = error.orEmpty(),
                onRetry = { appViewModel.retryLoad() },
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        if (isLoading && homes.isEmpty()) {
            FullScreenLoading(Modifier.padding(innerPadding))
            return@Scaffold
        }

        Row(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                start = AppScreenHorizontalPadding,
                end = AppScreenHorizontalPadding,
                bottom = AppFabListBottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = if (isExpanded) Modifier.weight(0.35f).fillMaxHeight() else Modifier.fillMaxSize()
        ) {
            item {
                Column {
                    AppSearchField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = stringResource(R.string.homes_search_placeholder),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { showRoomDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.homes_add_room), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Cada casa con sus habitaciones dentro
            items(filtered) { home ->
                HomeSection(
                    homeName = home.name,
                    subtitle = listOfNotNull(home.metadata?.city, home.metadata?.address).joinToString(" · "),
                    homeType = home.metadata?.type,
                    homeRooms = rooms[home.id] ?: emptyList(),
                    devices = devices,
                    selected = isExpanded && selectedHomeId == home.id,
                    onHomeClick = {
                        if (isExpanded) selectedHomeId = home.id
                        else navController.navigate(Routes.homeDetail(home.id))
                    },
                    onRoomClick = { roomId -> navController.navigate(Routes.room(roomId)) }
                )
            }

            // Apartado "Sin casa" con las habitaciones standalone
            if (filteredStandalone.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppSectionHeader(
                            title = stringResource(R.string.homes_no_home),
                            icon = Icons.Rounded.MeetingRoom,
                            count = if (filteredStandalone.size != 1) {
                                stringResource(R.string.common_rooms_count_many, filteredStandalone.size)
                            } else {
                                stringResource(R.string.common_rooms_count_one, filteredStandalone.size)
                            }
                        )
                        RoomsGrid(
                            roomList = filteredStandalone,
                            devices = devices,
                            onRoomClick = { roomId -> navController.navigate(Routes.room(roomId)) }
                        )
                    }
                }
            }

            if (filtered.isEmpty() && filteredStandalone.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.common_no_results), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

            // Panel de detalle (solo en tablets)
            if (isExpanded) {
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Box(Modifier.weight(0.65f).fillMaxHeight()) {
                    val selId = selectedHomeId
                    if (selId != null) {
                        HomeDetailContent(
                            homeId = selId,
                            appViewModel = appViewModel,
                            navController = navController,
                            showBackButton = false,
                            onHomeDeleted = { selectedHomeId = null }
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.Apartment, null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.homes_select_home),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

// Colores que matchean la web (verde encendido / rojo apagado).
private val OnGreen = Color(0xFF16A34A)
private val OffRed = Color(0xFFC0392B)

private fun homeTypeIcon(type: String?): ImageVector = when (type) {
    "Casa"            -> Icons.Rounded.Home
    "Departamento"    -> Icons.Rounded.Apartment
    "Oficina"         -> Icons.Rounded.BusinessCenter
    "Local Comercial" -> Icons.Rounded.Storefront
    else              -> Icons.Rounded.Home
}

/** Una casa: cabecera (nombre azul + badge + divisor) y sus habitaciones debajo. */
@Composable
private fun HomeSection(
    homeName: String,
    subtitle: String,
    homeType: String?,
    homeRooms: List<RoomDto>,
    devices: Map<String, List<DeviceDto>>,
    selected: Boolean = false,
    onHomeClick: () -> Unit,
    onRoomClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Cabecera estilo web
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (selected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        else Modifier
                    )
                    .clickable(onClick = onHomeClick)
                    .padding(vertical = 4.dp)
            ) {
                Icon(homeTypeIcon(homeType), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    homeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                CountBadge(
                    if (homeRooms.size != 1) stringResource(R.string.common_rooms_count_many, homeRooms.size)
                    else stringResource(R.string.common_rooms_count_one, homeRooms.size)
                )
                Icon(Icons.Rounded.ChevronRight, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            }
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        if (homeRooms.isNotEmpty()) {
            RoomsGrid(roomList = homeRooms, devices = devices, onRoomClick = onRoomClick)
        } else {
            Text(
                stringResource(R.string.homes_no_rooms),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/** Fila horizontal scrolleable de tarjetas cuadradas de habitación. */
@Composable
private fun RoomsGrid(
    roomList: List<RoomDto>,
    devices: Map<String, List<DeviceDto>>,
    onRoomClick: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(roomList, key = { it.id }) { room ->
            WebRoomCard(
                room = room,
                roomDevices = devices[room.id] ?: emptyList(),
                onClick = { onRoomClick(room.id) }
            )
        }
    }
}

/** Tarjeta cuadrada de habitación: ícono, nombre y badges de estado (encendidos / apagados / total). */
@Composable
private fun WebRoomCard(
    room: RoomDto,
    roomDevices: List<DeviceDto>,
    onClick: () -> Unit
) {
    val total = roomDevices.size
    val on = roomDevices.count { isDeviceOn(it.type.id, it.state) }
    val off = total - on

    AppClickablePanel(
        onClick = onClick,
        modifier = Modifier.size(160.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = AppIconShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        roomTypeIcon(room.metadata?.type), null,
                        Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Text(
                room.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            if (total > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    DotBadge("$on", OnGreen, Icons.Rounded.PowerSettingsNew)
                    DotBadge("$off", OffRed, Icons.Rounded.PowerSettingsNew, slashed = true)
                    DotBadge("$total", MaterialTheme.colorScheme.primary, Icons.Rounded.Devices)
                }
            } else {
                Text(
                    stringResource(R.string.homes_no_devices),
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/** Badge tipo pill con un punto de color (verde/rojo/azul) + número. */
@Composable
private fun DotBadge(
    text: String,
    accent: Color,
    icon: ImageVector,
    slashed: Boolean = false
) {
    Surface(shape = AppPillShape, color = accent.copy(alpha = 0.14f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(14.dp)) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(13.dp))
                if (slashed) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(15.dp)) {
                        drawLine(
                            color = accent,
                            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.82f),
                            end = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.18f),
                            strokeWidth = 1.7.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = accent)
        }
    }
}

/** Badge tipo pill simple (texto sobre fondo primario suave). */
@Composable
private fun CountBadge(text: String) {
    Surface(shape = AppPillShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
