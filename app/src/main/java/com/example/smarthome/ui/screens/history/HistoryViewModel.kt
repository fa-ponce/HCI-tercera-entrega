package com.example.smarthome.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.LogEntryDto
import com.example.smarthome.data.repository.HistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val historyRepository: HistoryRepository) : ViewModel() {

    private val _logs = MutableStateFlow<List<LogEntryDto>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val search = MutableStateFlow("")
    val page = MutableStateFlow(0)

    val filteredLogs: StateFlow<List<LogEntryDto>> = combine(_logs, search) { logs, q ->
        if (q.isBlank()) logs
        else {
            val sq = q.lowercase()
            logs.filter { log ->
                (log.deviceName ?: "").lowercase().contains(sq) ||
                (log.event ?: "").lowercase().contains(sq) ||
                (log.roomName ?: "").lowercase().contains(sq) ||
                (log.homeName ?: "").lowercase().contains(sq)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
