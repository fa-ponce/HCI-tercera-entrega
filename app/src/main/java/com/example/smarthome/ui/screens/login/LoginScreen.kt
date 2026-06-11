package com.example.smarthome.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.example.smarthome.ui.components.appBarBrush

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collect { onNavigateToHome() }
    }

    val showBackArrow = state.mode in listOf(
        LoginMode.Verify, LoginMode.ForgotPassword, LoginMode.ResetPassword
    )
    val backTarget = when (state.mode) {
        LoginMode.ResetPassword -> LoginMode.ForgotPassword
        else -> LoginMode.Login
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(appBarBrush()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Smarthome",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = when (state.mode) {
                        LoginMode.Login -> "Iniciar sesión"
                        LoginMode.Register -> "Crear cuenta"
                        LoginMode.Verify -> "Verificar cuenta"
                        LoginMode.ForgotPassword -> "Recuperar contraseña"
                        LoginMode.ResetPassword -> "Nueva contraseña"
                    },
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(24.dp))

                // ── Campos según el modo ──────────────────────────────────────
                when (state.mode) {
                    LoginMode.Register -> {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = viewModel::setName,
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::setEmail,
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::setPassword,
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }

                    LoginMode.Login -> {
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::setEmail,
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::setPassword,
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }

                    LoginMode.Verify -> {
                        Text(
                            text = "Ingresá el código que enviamos a ${state.email}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.code,
                            onValueChange = viewModel::setCode,
                            label = { Text("Código") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    LoginMode.ForgotPassword -> {
                        Text(
                            text = "Ingresá tu email y te enviaremos un código para restablecer tu contraseña.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::setEmail,
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }

                    LoginMode.ResetPassword -> {
                        Text(
                            text = "Ingresá el código que enviamos a ${state.email} y tu nueva contraseña.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.code,
                            onValueChange = viewModel::setCode,
                            label = { Text("Código") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.newPassword,
                            onValueChange = viewModel::setNewPassword,
                            label = { Text("Nueva contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                }

                // ── Error ─────────────────────────────────────────────────────
                if (state.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Botón principal ───────────────────────────────────────────
                Button(
                    onClick = {
                        when (state.mode) {
                            LoginMode.Login -> viewModel.login()
                            LoginMode.Register -> viewModel.register()
                            LoginMode.Verify -> viewModel.verifyAccount()
                            LoginMode.ForgotPassword -> viewModel.forgotPassword()
                            LoginMode.ResetPassword -> viewModel.resetPassword()
                        }
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = when (state.mode) {
                                LoginMode.Login -> "Iniciar sesión"
                                LoginMode.Register -> "Registrarse"
                                LoginMode.Verify -> "Verificar"
                                LoginMode.ForgotPassword -> "Enviar código"
                                LoginMode.ResetPassword -> "Cambiar contraseña"
                            }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Acción secundaria ─────────────────────────────────────────
                when (state.mode) {
                    LoginMode.Login -> {
                        TextButton(onClick = { viewModel.setMode(LoginMode.Register) }) {
                            Text("No tenés cuenta? Registrarte")
                        }
                        TextButton(onClick = { viewModel.setMode(LoginMode.ForgotPassword) }) {
                            Text("¿Olvidaste tu contraseña?")
                        }
                    }
                    LoginMode.Register -> TextButton(onClick = { viewModel.setMode(LoginMode.Login) }) {
                        Text("Ya tenés cuenta? Iniciar sesión")
                    }
                    LoginMode.Verify -> TextButton(onClick = { viewModel.sendVerification() }) {
                        Text("Reenviar código")
                    }
                    LoginMode.ForgotPassword, LoginMode.ResetPassword -> {}
                }
            }

            // ── Flecha de volver ──────────────────────────────────────────────
            if (showBackArrow) {
                IconButton(
                    onClick = { viewModel.setMode(backTarget) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Volver")
                }
            }
        }
    }
}
