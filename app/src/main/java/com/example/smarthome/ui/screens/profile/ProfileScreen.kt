package com.example.smarthome.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.navigation.Routes
import androidx.compose.foundation.layout.WindowInsets

private val AppBlue = Color(0xFF3A5A90)

private data class TutorialStep(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val tutorialSteps = listOf(
    TutorialStep(
        icon = Icons.Rounded.Home,
        title = "Inicio",
        description = "El panel principal de tu hogar. Muestra un resumen de tus casas, dispositivos activos, consumo actual y actividad reciente."
    ),
    TutorialStep(
        icon = Icons.Rounded.Apartment,
        title = "Casas",
        description = "Administrá todas tus casas y sus habitaciones. Podés agregar nuevas casas, ver los cuartos de cada una y navegar a sus dispositivos."
    ),
    TutorialStep(
        icon = Icons.Rounded.Devices,
        title = "Dispositivos",
        description = "Controlá todos tus dispositivos desde un solo lugar. Podés encender, apagar y configurar cada dispositivo, además de filtrarlos por tipo."
    ),
    TutorialStep(
        icon = Icons.Rounded.Schedule,
        title = "Rutinas",
        description = "Automatizá acciones con rutinas. Podés crearlas de forma manual, programarlas por horario o disparadas por eventos del hogar."
    ),
    TutorialStep(
        icon = Icons.Rounded.BarChart,
        title = "Consumo",
        description = "Monitoreá el consumo energético en tiempo real. Ves el total de watts activos, el costo estimado por hora y el desglose por casa."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val userName by appViewModel.userName.collectAsState()
    val userEmail by appViewModel.userEmail.collectAsState()
    val costoKwh by appViewModel.costoKwh.collectAsState()
    val darkMode by appViewModel.darkMode.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    var tutorialStep by remember { mutableStateOf(0) }

    var costoInput by remember(costoKwh) {
        mutableStateOf(costoKwh?.let { "%.2f".format(it) } ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Mi Perfil",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBlue)
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar + info
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(88.dp),
                tint = AppBlue
            )
            Spacer(Modifier.height(12.dp))
            Text(
                userName ?: "Usuario",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (!userEmail.isNullOrEmpty()) {
                Text(
                    userEmail!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(28.dp))

            // Cambiar contraseña
            ProfileCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Lock, null, tint = AppBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Contraseña", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Cambiá tu contraseña actual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(
                        onClick = { showPasswordDialog = true },
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppBlue)
                    ) {
                        Text("Cambiar", color = AppBlue, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Costo de energía
            ProfileCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.BarChart, null, tint = AppBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Costo de energía", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = costoInput,
                            onValueChange = { costoInput = it },
                            label = { Text("$/kWh") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                TextButton(onClick = {
                                    costoInput.toFloatOrNull()?.let { appViewModel.updateCostoKwh(it) }
                                }) {
                                    Text("Guardar", color = AppBlue, style = MaterialTheme.typography.labelMedium)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Modo oscuro
            ProfileCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.DarkMode, null, tint = AppBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Modo oscuro", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(if (darkMode) "Activado" else "Desactivado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { appViewModel.setDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppBlue)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Ayuda
            ProfileCard(onClick = { tutorialStep = 0; showTutorial = true }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Help, null, tint = AppBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Ayuda", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("Recorrido por las funciones de la app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            // Cerrar sesión
            Button(
                onClick = {
                    appViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Modal cambiar contraseña
    if (showPasswordDialog) {
        ChangePasswordDialog(
            appViewModel = appViewModel,
            onDismiss = { showPasswordDialog = false }
        )
    }

    // Tutorial overlay
    AnimatedVisibility(visible = showTutorial, enter = fadeIn(), exit = fadeOut()) {
        TutorialOverlay(
            step = tutorialStep,
            onNext = {
                if (tutorialStep < tutorialSteps.lastIndex) tutorialStep++
                else showTutorial = false
            },
            onBack = { if (tutorialStep > 0) tutorialStep-- },
            onClose = { showTutorial = false }
        )
    }
}

@Composable
private fun ProfileCard(onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(Modifier.padding(16.dp)) { content() }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun ChangePasswordDialog(appViewModel: AppViewModel, onDismiss: () -> Unit) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("Cambiar contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (success) {
                    Text("Contraseña actualizada correctamente.", color = Color(0xFF2E7D32))
                } else {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it; errorMsg = null },
                        label = { Text("Contraseña actual") },
                        singleLine = true,
                        visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { oldVisible = !oldVisible }) {
                                Icon(if (oldVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; errorMsg = null },
                        label = { Text("Nueva contraseña") },
                        singleLine = true,
                        visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newVisible = !newVisible }) {
                                Icon(if (newVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it; errorMsg = null },
                        label = { Text("Confirmar nueva contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            if (success) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                ) { Text("Cerrar", color = Color.White) }
            } else {
                Button(
                    onClick = {
                        when {
                            oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank() ->
                                errorMsg = "Completá todos los campos."
                            newPass != confirmPass ->
                                errorMsg = "Las contraseñas nuevas no coinciden."
                            newPass.length < 6 ->
                                errorMsg = "La nueva contraseña debe tener al menos 6 caracteres."
                            else -> {
                                loading = true
                                appViewModel.changePassword(oldPass, newPass) { ok, err ->
                                    loading = false
                                    if (ok) success = true
                                    else errorMsg = err ?: "Error al cambiar la contraseña."
                                }
                            }
                        }
                    },
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                ) { Text(if (loading) "Guardando…" else "Guardar", color = Color.White) }
            }
        },
        dismissButton = {
            if (!success) {
                TextButton(onClick = { if (!loading) onDismiss() }) { Text("Cancelar") }
            }
        }
    )
}

@Composable
private fun TutorialOverlay(
    step: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val current = tutorialSteps[step]
    val isLast = step == tutorialSteps.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Indicador de paso
                Text(
                    "${step + 1} / ${tutorialSteps.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                // Ícono + título
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = current.icon,
                        contentDescription = null,
                        tint = AppBlue,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        current.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(current.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(20.dp))

                // Botones de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClose) { Text("Omitir") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (step > 0) {
                            OutlinedButton(onClick = onBack) { Text("Anterior") }
                        }
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(containerColor = AppBlue)
                        ) {
                            Text(if (isLast) "Finalizar" else "Siguiente", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
