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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.res.stringResource
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
import com.example.smarthome.R
import com.example.smarthome.ui.components.appBarBrush

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

    // Validación local de campos obligatorios: se activa al intentar enviar.
    var showErrors by remember { mutableStateOf(false) }
    val requiredMsg = stringResource(R.string.common_required_field)
    val invalidEmailMsg = stringResource(R.string.login_email_invalid)
    val requiredError: (String) -> (@Composable () -> Unit)? = { value ->
        if (showErrors && value.isBlank()) { { Text(requiredMsg) } } else null
    }
    // Error del campo email en el registro: vacío o con formato inválido.
    val emailFormatInvalid = state.email.isNotBlank() && !isValidEmail(state.email)
    val registerEmailError: (@Composable () -> Unit)? = when {
        !showErrors -> null
        state.email.isBlank() -> { { Text(requiredMsg) } }
        emailFormatInvalid -> { { Text(invalidEmailMsg) } }
        else -> null
    }

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
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(32.dp))

                Text(
                    text = when (state.mode) {
                        LoginMode.Login -> stringResource(R.string.login_title_login)
                        LoginMode.Register -> stringResource(R.string.login_title_register)
                        LoginMode.Verify -> stringResource(R.string.login_title_verify)
                        LoginMode.ForgotPassword -> stringResource(R.string.login_title_forgot)
                        LoginMode.ResetPassword -> stringResource(R.string.login_title_reset)
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
                            label = { Text(stringResource(R.string.common_name) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = showErrors && state.name.isBlank(),
                            supportingText = requiredError(state.name)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::setEmail,
                            label = { Text(stringResource(R.string.login_email) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = showErrors && (state.email.isBlank() || emailFormatInvalid),
                            supportingText = registerEmailError
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::setPassword,
                            label = { Text(stringResource(R.string.login_password) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = showErrors && state.password.isBlank(),
                            supportingText = requiredError(state.password),
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
                            label = { Text(stringResource(R.string.login_email) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = showErrors && state.email.isBlank(),
                            supportingText = requiredError(state.email)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = viewModel::setPassword,
                            label = { Text(stringResource(R.string.login_password) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = showErrors && state.password.isBlank(),
                            supportingText = requiredError(state.password),
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
                            text = stringResource(R.string.login_verify_hint, state.email),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.code,
                            onValueChange = viewModel::setCode,
                            label = { Text(stringResource(R.string.login_code) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showErrors && state.code.isBlank(),
                            supportingText = requiredError(state.code)
                        )
                    }

                    LoginMode.ForgotPassword -> {
                        Text(
                            text = stringResource(R.string.login_forgot_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::setEmail,
                            label = { Text(stringResource(R.string.login_email) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = showErrors && state.email.isBlank(),
                            supportingText = requiredError(state.email)
                        )
                    }

                    LoginMode.ResetPassword -> {
                        Text(
                            text = stringResource(R.string.login_reset_hint, state.email),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.code,
                            onValueChange = viewModel::setCode,
                            label = { Text(stringResource(R.string.login_code) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showErrors && state.code.isBlank(),
                            supportingText = requiredError(state.code)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = state.newPassword,
                            onValueChange = viewModel::setNewPassword,
                            label = { Text(stringResource(R.string.login_new_password) + " *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = showErrors && state.newPassword.isBlank(),
                            supportingText = requiredError(state.newPassword),
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
                        text = state.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Botón principal ───────────────────────────────────────────
                Button(
                    onClick = {
                        val invalid = when (state.mode) {
                            LoginMode.Login -> state.email.isBlank() || state.password.isBlank()
                            LoginMode.Register -> state.name.isBlank() || state.email.isBlank() ||
                                state.password.isBlank() || !isValidEmail(state.email)
                            LoginMode.Verify -> state.code.isBlank()
                            LoginMode.ForgotPassword -> state.email.isBlank()
                            LoginMode.ResetPassword -> state.code.isBlank() || state.newPassword.isBlank()
                        }
                        showErrors = invalid
                        if (!invalid) {
                            when (state.mode) {
                                LoginMode.Login -> viewModel.login()
                                LoginMode.Register -> viewModel.register()
                                LoginMode.Verify -> viewModel.verifyAccount()
                                LoginMode.ForgotPassword -> viewModel.forgotPassword()
                                LoginMode.ResetPassword -> viewModel.resetPassword()
                            }
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
                                LoginMode.Login -> stringResource(R.string.login_title_login)
                                LoginMode.Register -> stringResource(R.string.login_button_register)
                                LoginMode.Verify -> stringResource(R.string.login_button_verify)
                                LoginMode.ForgotPassword -> stringResource(R.string.login_button_send_code)
                                LoginMode.ResetPassword -> stringResource(R.string.login_button_change_password)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // ── Acción secundaria ─────────────────────────────────────────
                when (state.mode) {
                    LoginMode.Login -> {
                        TextButton(onClick = { showErrors = false; viewModel.setMode(LoginMode.Register) }) {
                            Text(stringResource(R.string.login_no_account))
                        }
                        TextButton(onClick = { showErrors = false; viewModel.setMode(LoginMode.ForgotPassword) }) {
                            Text(stringResource(R.string.login_forgot_password))
                        }
                    }
                    LoginMode.Register -> TextButton(onClick = { showErrors = false; viewModel.setMode(LoginMode.Login) }) {
                        Text(stringResource(R.string.login_have_account))
                    }
                    LoginMode.Verify -> TextButton(onClick = { viewModel.sendVerification() }) {
                        Text(stringResource(R.string.login_resend_code))
                    }
                    LoginMode.ForgotPassword, LoginMode.ResetPassword -> {}
                }
            }

            // ── Flecha de volver ──────────────────────────────────────────────
            if (showBackArrow) {
                IconButton(
                    onClick = { showErrors = false; viewModel.setMode(backTarget) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            }
        }
    }
}

/** Valida que el texto tenga formato de email (nombre@dominio.tld). */
private fun isValidEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
