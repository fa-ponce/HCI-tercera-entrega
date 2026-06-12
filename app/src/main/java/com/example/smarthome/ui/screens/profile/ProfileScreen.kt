package com.example.smarthome.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Language
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.setValue
import androidx.annotation.StringRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.ui.AppViewModel
import com.example.smarthome.ui.UserPreferencesViewModel
import com.example.smarthome.ui.navigation.Routes
import androidx.compose.foundation.layout.WindowInsets


private data class TutorialStep(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

private val tutorialSteps = listOf(
    TutorialStep(
        icon = Icons.Rounded.Home,
        titleRes = R.string.nav_home,
        descriptionRes = R.string.profile_tut_home_desc
    ),
    TutorialStep(
        icon = Icons.Rounded.Apartment,
        titleRes = R.string.nav_homes,
        descriptionRes = R.string.profile_tut_homes_desc
    ),
    TutorialStep(
        icon = Icons.Rounded.Devices,
        titleRes = R.string.nav_devices,
        descriptionRes = R.string.profile_tut_devices_desc
    ),
    TutorialStep(
        icon = Icons.Rounded.Schedule,
        titleRes = R.string.nav_routines,
        descriptionRes = R.string.profile_tut_routines_desc
    ),
    TutorialStep(
        icon = Icons.Rounded.BarChart,
        titleRes = R.string.nav_consumption,
        descriptionRes = R.string.profile_tut_consumption_desc
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appViewModel: AppViewModel,
    prefsViewModel: UserPreferencesViewModel,
    navController: NavHostController
) {
    val userName by prefsViewModel.userName.collectAsState()
    val userEmail by prefsViewModel.userEmail.collectAsState()
    val costoKwh by prefsViewModel.costoKwh.collectAsState()
    val darkMode by prefsViewModel.darkMode.collectAsState()
    val appLanguage by prefsViewModel.appLanguage.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }
    var tutorialStep by remember { mutableStateOf(0) }

    var costoInput by remember(costoKwh) {
        mutableStateOf(costoKwh?.let { "%.2f".format(it) } ?: "")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.appBarGradient(),
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = gradientTopBarColors()
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
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                userName ?: stringResource(R.string.common_user),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (!userEmail.isNullOrEmpty()) {
                Text(
                    userEmail.orEmpty(),
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
                    Icon(Icons.Rounded.Lock, contentDescription = stringResource(R.string.login_password), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.login_password), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.profile_password_sub), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(
                        onClick = { showPasswordDialog = true },
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.profile_change), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
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
                    Icon(Icons.Rounded.BarChart, contentDescription = stringResource(R.string.profile_energy_cost), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.profile_energy_cost), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = costoInput,
                            onValueChange = { costoInput = it },
                            label = { Text(stringResource(R.string.profile_kwh_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            trailingIcon = {
                                TextButton(onClick = {
                                    costoInput.toFloatOrNull()?.let { prefsViewModel.updateCostoKwh(it) }
                                }) {
                                    Text(stringResource(R.string.common_save), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
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
                    Icon(Icons.Rounded.DarkMode, contentDescription = stringResource(R.string.profile_dark_mode), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.profile_dark_mode), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(if (darkMode) stringResource(R.string.profile_enabled) else stringResource(R.string.profile_disabled), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { prefsViewModel.setDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Idioma
            val currentLanguageLabel = when (appLanguage) {
                "es" -> stringResource(R.string.language_es)
                "en" -> stringResource(R.string.language_en)
                else -> stringResource(R.string.profile_language_system)
            }
            ProfileCard(onClick = { showLanguageDialog = true }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Language, contentDescription = stringResource(R.string.profile_language), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.profile_language), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(currentLanguageLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Ayuda
            ProfileCard(onClick = { tutorialStep = 0; showTutorial = true }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Help, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.profile_help), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(stringResource(R.string.profile_help_sub), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(32.dp))

            // Cerrar sesión
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.profile_logout), color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Modal cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.profile_logout), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.profile_logout_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        appViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.profile_logout), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // Modal cambiar contraseña
    if (showPasswordDialog) {
        ChangePasswordDialog(
            prefsViewModel = prefsViewModel,
            onDismiss = { showPasswordDialog = false }
        )
    }

    // Modal elegir idioma
    if (showLanguageDialog) {
        val activity = LocalContext.current as? Activity
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.profile_language_choose), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        null to stringResource(R.string.profile_language_system),
                        "es" to stringResource(R.string.language_es),
                        "en" to stringResource(R.string.language_en)
                    ).forEach { (code, label) ->
                        val select = {
                            showLanguageDialog = false
                            // El idioma se aplica en attachBaseContext, así que
                            // recreamos la Activity una vez guardada la elección.
                            prefsViewModel.setAppLanguage(code) { activity?.recreate() }
                            Unit
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { select() }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(selected = appLanguage == code, onClick = { select() })
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
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
private fun ChangePasswordDialog(prefsViewModel: UserPreferencesViewModel, onDismiss: () -> Unit) {
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
        title = { Text(stringResource(R.string.profile_change_password), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (success) {
                    Text(stringResource(R.string.profile_password_updated), color = Color(0xFF2E7D32))
                } else {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it; errorMsg = null },
                        label = { Text(stringResource(R.string.profile_current_password)) },
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
                        label = { Text(stringResource(R.string.profile_new_password_label)) },
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
                        label = { Text(stringResource(R.string.profile_confirm_password)) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text(stringResource(R.string.common_close), color = MaterialTheme.colorScheme.onPrimary) }
            } else {
                val errFill = stringResource(R.string.profile_fill_fields)
                val errMismatch = stringResource(R.string.profile_passwords_mismatch)
                val errMin = stringResource(R.string.profile_password_min)
                val errGeneric = stringResource(R.string.profile_password_error)
                Button(
                    onClick = {
                        when {
                            oldPass.isBlank() || newPass.isBlank() || confirmPass.isBlank() ->
                                errorMsg = errFill
                            newPass != confirmPass ->
                                errorMsg = errMismatch
                            newPass.length < 6 ->
                                errorMsg = errMin
                            else -> {
                                loading = true
                                prefsViewModel.changePassword(oldPass, newPass) { ok, err ->
                                    loading = false
                                    if (ok) success = true
                                    else errorMsg = err ?: errGeneric
                                }
                            }
                        }
                    },
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text(if (loading) stringResource(R.string.profile_saving) else stringResource(R.string.common_save), color = MaterialTheme.colorScheme.onPrimary) }
            }
        },
        dismissButton = {
            if (!success) {
                TextButton(onClick = { if (!loading) onDismiss() }) { Text(stringResource(R.string.common_cancel)) }
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(current.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(stringResource(current.descriptionRes), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(20.dp))

                // Botones de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClose) { Text(stringResource(R.string.profile_skip)) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (step > 0) {
                            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.common_previous)) }
                        }
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(if (isLast) stringResource(R.string.profile_finish) else stringResource(R.string.common_next), color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    }
}
