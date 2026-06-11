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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.smarthome.R
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.components.ConnectionErrorView
import com.example.smarthome.ui.components.FullScreenLoading
import com.example.smarthome.ui.navigation.Routes
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
        )
    }

    if (showRoomDialog) {
        NewRoomDialog(
            homes = homes,
            onDismiss = { showRoomDialog = false },
            onCreate = { name, type, floor, homeId, onResult ->
                if (homeId == null) {
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
                message = error!!,
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
                start = 16.dp, end = 16.dp, bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = if (isExpanded) Modifier.weight(0.35f).fillMaxHeight() else Modifier.fillMaxSize()
        ) {
            item {
                Column {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text(stringResource(R.string.homes_search_placeholder)) },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.MeetingRoom, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                stringResource(R.string.homes_no_home),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    if (filteredStandalone.size != 1) stringResource(R.string.common_rooms_count_many, filteredStandalone.size)
                                    else stringResource(R.string.common_rooms_count_one, filteredStandalone.size),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
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

/** Una casa: cabecera (nombre azul + badge + divisor) y sus habitaciones debajo. */
@Composable
private fun HomeSection(
    homeName: String,
    subtitle: String,
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
                Icon(Icons.Rounded.Apartment, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    homeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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

    Card(
        onClick = onClick,
        modifier = Modifier.size(160.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.MeetingRoom, null,
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
                    DotBadge("$on", OnGreen)
                    DotBadge("$off", OffRed)
                    DotBadge("$total", MaterialTheme.colorScheme.primary)
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
private fun DotBadge(text: String, accent: Color) {
    Surface(shape = RoundedCornerShape(20.dp), color = accent.copy(alpha = 0.14f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
            Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = accent)
        }
    }
}

/** Badge tipo pill simple (texto sobre fondo primario suave). */
@Composable
private fun CountBadge(text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

private val ROOM_TYPES = listOf(
    "Living", "Dormitorio", "Cocina", "Baño", "Garaje", "Estudio", "Comedor", "Lavadero"
)

/** Diálogo para crear una habitación, eligiendo casa o "Sin casa". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewRoomDialog(
    homes: List<HomeDto>,
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, floor: Int, homeId: String?, onResult: (Boolean, String?) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf(ROOM_TYPES[0]) }
    var typeExpanded by remember { mutableStateOf(false) }
    var floor by remember { mutableStateOf(1) }
    var selectedHome by remember { mutableStateOf<HomeDto?>(null) }
    var homeExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.homes_new_room), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text(stringResource(R.string.common_name_required)) },
                    placeholder = { Text(stringResource(R.string.homes_room_name_placeholder)) },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { msg -> { Text(msg) } },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Tipo
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.common_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        ROOM_TYPES.forEach { type ->
                            DropdownMenuItem(text = { Text(type) }, onClick = { selectedType = type; typeExpanded = false })
                        }
                    }
                }

                // Casa (o Sin casa)
                ExposedDropdownMenuBox(expanded = homeExpanded, onExpandedChange = { homeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedHome?.name ?: stringResource(R.string.homes_no_home),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.common_home)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(homeExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = homeExpanded, onDismissRequest = { homeExpanded = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.homes_no_home)) }, onClick = { selectedHome = null; homeExpanded = false })
                        homes.forEach { home ->
                            DropdownMenuItem(text = { Text(home.name) }, onClick = { selectedHome = home; homeExpanded = false })
                        }
                    }
                }

                // Piso
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.common_floor), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (floor > 1) floor-- }, modifier = Modifier.size(36.dp)) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text("$floor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { floor++ }, modifier = Modifier.size(36.dp)) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }

                if (saveError != null) {
                    Text(saveError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            val errMin = stringResource(R.string.common_name_min_chars)
            val errMax = stringResource(R.string.common_name_max_chars)
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.length < 3 -> nameError = errMin
                        trimmed.length > 100 -> nameError = errMax
                        !isSaving -> {
                            isSaving = true
                            saveError = null
                            onCreate(trimmed, selectedType, floor, selectedHome?.id) { ok, err ->
                                isSaving = false
                                if (!ok) saveError = err
                            }
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(stringResource(R.string.common_create), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

private val PROPERTY_TYPES = listOf("Casa", "Departamento", "Oficina", "Local Comercial")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewHomeDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, address: String, city: String, onResult: (Boolean, String?) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PROPERTY_TYPES[0]) }
    var typeExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.homes_new_home), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text(stringResource(R.string.homes_home_name_label)) },
                    placeholder = { Text(stringResource(R.string.homes_home_name_placeholder)) },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { msg -> { Text(msg) } },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.homes_property_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        PROPERTY_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(stringResource(R.string.homes_address)) },
                    placeholder = { Text(stringResource(R.string.homes_address_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(stringResource(R.string.homes_city)) },
                    placeholder = { Text(stringResource(R.string.homes_city_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (saveError != null) {
                    Text(saveError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            val errMin = stringResource(R.string.common_name_min_chars)
            val errMax = stringResource(R.string.common_name_max_chars)
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.length < 3 -> nameError = errMin
                        trimmed.length > 100 -> nameError = errMax
                        !isSaving -> {
                            isSaving = true
                            saveError = null
                            onCreate(trimmed, selectedType, address.trim(), city.trim()) { ok, err ->
                                isSaving = false
                                if (!ok) saveError = err
                            }
                        }
                    }
                },
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(stringResource(R.string.common_create), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
