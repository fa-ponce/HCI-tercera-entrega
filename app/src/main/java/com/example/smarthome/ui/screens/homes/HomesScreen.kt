package com.example.smarthome.ui.screens.homes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.domain.deviceIcon
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.components.truncateName
import com.example.smarthome.ui.navigation.Routes
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
            onCreate = { name, type, address, city ->
                showDialog = false
                scope.launch {
                    ServiceLocator.homeRepository.createHome(name, type, address, city)
                        .onSuccess { appViewModel.addHome(it) }
                }
            }
        )
    }

    if (showRoomDialog) {
        NewRoomDialog(
            homes = homes,
            onDismiss = { showRoomDialog = false },
            onCreate = { name, type, floor, homeId ->
                if (homeId == null) {
                    appViewModel.createStandaloneRoom(name, type, floor) { ok ->
                        if (ok) showRoomDialog = false
                    }
                } else {
                    scope.launch {
                        ServiceLocator.homeRepository.createRoom(name, type, floor, homeId)
                            .onSuccess { appViewModel.addRoom(homeId, it); showRoomDialog = false }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Casas",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.PROFILE) }) {
                        Icon(Icons.Rounded.AccountCircle, contentDescription = "Perfil", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary) },
                text = { Text("Nueva casa", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        if (!isLoading && homes.isEmpty() && error != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Rounded.WifiOff, null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(onClick = { appViewModel.retryLoad() }) {
                        Text("Reintentar")
                    }
                }
            }
            return@Scaffold
        }

        if (isLoading && homes.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                start = 16.dp, end = 16.dp, bottom = 88.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Buscar casa o habitación…") },
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
                        Text("Agregar habitación", fontWeight = FontWeight.SemiBold)
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
                    onHomeClick = { navController.navigate(Routes.homeDetail(home.id)) },
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
                                "Sin casa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "${filteredStandalone.size} habitaci${if (filteredStandalone.size != 1) "ones" else "ón"}",
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
                        Text("Sin resultados", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onHomeClick: () -> Unit,
    onRoomClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Cabecera estilo web
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable(onClick = onHomeClick)
            ) {
                Icon(Icons.Rounded.Apartment, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    homeName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                CountBadge("${homeRooms.size} habitaci${if (homeRooms.size != 1) "ones" else "ón"}")
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
                "Esta casa no tiene habitaciones.",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/** Lista vertical de tarjetas de habitación estilo web (una por fila). */
@Composable
private fun RoomsGrid(
    roomList: List<RoomDto>,
    devices: Map<String, List<DeviceDto>>,
    onRoomClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        roomList.forEach { room ->
            WebRoomCard(
                room = room,
                roomDevices = devices[room.id] ?: emptyList(),
                onClick = { onRoomClick(room.id) }
            )
        }
    }
}

/** Tarjeta de habitación al estilo de la web: header con badges + burbujas de dispositivos. */
@OptIn(ExperimentalLayoutApi::class)
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header: nombre + badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.MeetingRoom, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Text(
                        room.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DotBadge("$on", OnGreen)
                    DotBadge("$off", OffRed)
                    DotBadge("$total", MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            // Burbujas de dispositivos
            if (total > 0) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roomDevices.sortedBy { it.name.length }.forEach { dev ->
                        DeviceBubble(dev)
                    }
                }
            } else {
                Text(
                    "Sin dispositivos",
                    style = MaterialTheme.typography.bodySmall,
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

/** "Burbuja" de un dispositivo: icono + nombre, verde si está encendido, gris si no. */
@Composable
private fun DeviceBubble(device: DeviceDto) {
    val on = isDeviceOn(device.type.id, device.state)
    val color = if (on) OnGreen else MaterialTheme.colorScheme.outline
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (on) OnGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(deviceIcon(device.type.id), null, Modifier.size(13.dp), tint = color)
            Text(
                truncateName(device.name, 16),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
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
    onCreate: (name: String, type: String, floor: Int, homeId: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf(ROOM_TYPES[0]) }
    var typeExpanded by remember { mutableStateOf(false) }
    var floor by remember { mutableStateOf(1) }
    var selectedHome by remember { mutableStateOf<HomeDto?>(null) }
    var homeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva habitación", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Nombre *") },
                    placeholder = { Text("Ej: Dormitorio Principal") },
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
                        label = { Text("Tipo") },
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
                        value = selectedHome?.name ?: "Sin casa",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Casa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(homeExpanded) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = homeExpanded, onDismissRequest = { homeExpanded = false }) {
                        DropdownMenuItem(text = { Text("Sin casa") }, onClick = { selectedHome = null; homeExpanded = false })
                        homes.forEach { home ->
                            DropdownMenuItem(text = { Text(home.name) }, onClick = { selectedHome = home; homeExpanded = false })
                        }
                    }
                }

                // Piso
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Piso", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { if (floor > 1) floor-- }, modifier = Modifier.size(36.dp)) {
                        Text("−", style = MaterialTheme.typography.titleLarge)
                    }
                    Text("$floor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { floor++ }, modifier = Modifier.size(36.dp)) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.length < 3 -> nameError = "El nombre debe tener al menos 3 caracteres"
                        trimmed.length > 100 -> nameError = "El nombre no puede superar 100 caracteres"
                        else -> onCreate(trimmed, selectedType, floor, selectedHome?.id)
                    }
                }
            ) {
                Text("Crear", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private val PROPERTY_TYPES = listOf("Casa", "Departamento", "Oficina", "Local Comercial")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewHomeDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, type: String, address: String, city: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PROPERTY_TYPES[0]) }
    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva casa", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Nombre de la casa *") },
                    placeholder = { Text("Ej: Casa de playa") },
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
                        label = { Text("Tipo de propiedad") },
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
                    label = { Text("Dirección") },
                    placeholder = { Text("Ej: Av. Libertador 1234") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Ciudad") },
                    placeholder = { Text("Ej: Buenos Aires") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    when {
                        trimmed.length < 3 -> nameError = "El nombre debe tener al menos 3 caracteres"
                        trimmed.length > 100 -> nameError = "El nombre no puede superar 100 caracteres"
                        else -> onCreate(trimmed, selectedType, address.trim(), city.trim())
                    }
                }
            ) {
                Text("Crear", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
