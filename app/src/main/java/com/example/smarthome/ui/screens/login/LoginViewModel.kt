package com.example.smarthome.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LoginMode {
    object Login : LoginMode()
    object Register : LoginMode()
    object Verify : LoginMode()
}

data class LoginUiState(
    val mode: LoginMode = LoginMode.Login,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigateToHome = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToHome: SharedFlow<Unit> = _navigateToHome.asSharedFlow()

    fun setMode(mode: LoginMode) = _uiState.update { it.copy(mode = mode, error = null) }
    fun setName(v: String) = _uiState.update { it.copy(name = v) }
    fun setEmail(v: String) = _uiState.update { it.copy(email = v) }
    fun setPassword(v: String) = _uiState.update { it.copy(password = v) }
    fun setCode(v: String) = _uiState.update { it.copy(code = v) }

    fun login() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val s = uiState.value
        authRepository.login(s.email, s.password)
            .onSuccess { _navigateToHome.emit(Unit) }
            .onFailure { _uiState.update { st -> st.copy(error = it.message ?: "Error al iniciar sesión") } }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun register() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val s = uiState.value
        authRepository.register(s.name, s.email, s.password)
            .onSuccess { setMode(LoginMode.Verify) }
            .onFailure { _uiState.update { st -> st.copy(error = it.message ?: "Error al registrarse") } }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun verifyAccount() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        authRepository.verifyAccount(uiState.value.code)
            .onSuccess { setMode(LoginMode.Login) }
            .onFailure { _uiState.update { st -> st.copy(error = it.message ?: "Código inválido") } }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun sendVerification() = viewModelScope.launch {
        authRepository.sendVerification(uiState.value.email)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(ServiceLocator.authRepository) }
        }
    }
}
