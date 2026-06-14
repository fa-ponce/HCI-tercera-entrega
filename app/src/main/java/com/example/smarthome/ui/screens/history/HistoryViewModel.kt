package com.example.smarthome.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.LogEntryDto
import com.example.smarthome.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    // Logs crudos de la API. El nombre/habitación/casa del dispositivo NO viene
    // (fiable) en el log: se resuelve en la pantalla cruzando el deviceId contra
    // los dispositivos cargados, igual que hace la web.
    private val _logs = MutableStateFlow<List<LogEntryDto>>(emptyList())
    val logs: StateFlow<List<LogEntryDto>> = _logs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val search = MutableStateFlow("")
    val page = MutableStateFlow(0)

    init {
        fetchLogs()
    }

    fun fetchLogs() = viewModelScope.launch {
        _isLoading.value = true
        historyRepository.getLogs(200, 0)
            .onSuccess { _logs.value = it }
        _isLoading.value = false
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { HistoryViewModel(ServiceLocator.historyRepository) }
        }
    }
}
