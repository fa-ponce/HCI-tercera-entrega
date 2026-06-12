package com.example.smarthome.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.datastore.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserPreferencesViewModel(
    application: Application,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    val userName: StateFlow<String?> = userPreferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userEmail: StateFlow<String?> = userPreferences.userEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val costoKwh: StateFlow<Float?> = userPreferences.costoKwh
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val darkMode: StateFlow<Boolean> = userPreferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val appLanguage: StateFlow<String?> = userPreferences.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val shortcuts: StateFlow<List<String>?> = userPreferences.shortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setAppLanguage(language: String?, onSaved: (() -> Unit)? = null) = viewModelScope.launch {
        userPreferences.saveAppLanguage(language)
        onSaved?.invoke()
    }

    fun setShortcuts(tokens: List<String>) = viewModelScope.launch {
        userPreferences.saveShortcuts(tokens)
    }

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        userPreferences.saveDarkMode(enabled)
    }

    fun updateCostoKwh(costo: Float) = viewModelScope.launch {
        userPreferences.saveCostoKwh(costo)
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        onResult: (success: Boolean, error: String?) -> Unit
    ) = viewModelScope.launch {
        val email = userEmail.value
        if (email == null) {
            onResult(false, "No se pudo obtener el email del usuario")
            return@launch
        }
        ServiceLocator.authRepository.changePassword(email, oldPassword, newPassword)
            .onSuccess { onResult(true, null) }
            .onFailure { onResult(false, it.message) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                UserPreferencesViewModel(
                    application = this[APPLICATION_KEY] as Application,
                    userPreferences = ServiceLocator.userPreferences
                )
            }
        }
    }
}
