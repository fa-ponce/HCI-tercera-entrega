package com.example.smarthome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smarthome.ServiceLocator
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.DeviceTypeDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.data.api.models.RoomRef
import com.example.smarthome.data.api.models.RoutineDto
import com.example.smarthome.data.datastore.UserPreferences
import com.example.smarthome.data.repository.DeviceRepository
import com.example.smarthome.data.repository.HomeRepository
import com.example.smarthome.data.repository.RoutineRepository
import com.example.smarthome.domain.DeviceTypes
import com.example.smarthome.domain.isDeviceOn
import com.example.smarthome.domain.toggleAction
import com.example.smarthome.socket.SocketManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    private val homeRepository: HomeRepository,
    private val deviceRepository: DeviceRepository,
    private val routineRepository: RoutineRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _homes = MutableStateFlow<List<HomeDto>>(emptyList())
    val homes: StateFlow<List<HomeDto>> = _homes.asStateFlow()

    private val _rooms = MutableStateFlow<Map<String, List<RoomDto>>>(emptyMap())
    val rooms: StateFlow<Map<String, List<RoomDto>>> = _rooms.asStateFlow()

    private val _devices = MutableStateFlow<Map<String, List<DeviceDto>>>(emptyMap())
    val devices: StateFlow<Map<String, List<DeviceDto>>> = _devices.asStateFlow()

    private val _routines = MutableStateFlow<List<RoutineDto>>(emptyList())
    val routines: StateFlow<List<RoutineDto>> = _routines.asStateFlow()

    private val _deviceTypes = MutableStateFlow<List<DeviceTypeDto>>(emptyList())
    val deviceTypes: StateFlow<List<DeviceTypeDto>> = _deviceTypes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val userName: StateFlow<String?> = userPreferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userEmail: StateFlow<String?> = userPreferences.userEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val costoKwh: StateFlow<Float?> = userPreferences.costoKwh
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val darkMode: StateFlow<Boolean> = userPreferences.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
        val email = userEmail.value ?: return@launch
        ServiceLocator.authRepository.changePassword(email, oldPassword, newPassword)
            .onSuccess { onResult(true, null) }
            .onFailure { onResult(false, it.message) }
    }

    fun logout() = viewModelScope.launch {
        userPreferences.clear()
    }

    fun loadAll() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val homesJob = async { homeRepository.getHomes().getOrThrow() }
            val routinesJob = async { routineRepository.getRoutines().getOrDefault(emptyList()) }

            val homes = homesJob.await()
            _homes.value = homes
            _routines.value = routinesJob.await()

            val roomsMap = mutableMapOf<String, List<RoomDto>>()
            val devicesMap = mutableMapOf<String, List<DeviceDto>>()

            homes.map { home ->
                async {
                    homeRepository.getHomeRooms(home.id).getOrNull()?.let { roomList ->
                        roomsMap[home.id] = roomList
                        roomList.map { room ->
                            async {
                                deviceRepository.getRoomDevices(room.id).getOrNull()?.let { deviceList ->
                                    devicesMap[room.id] = deviceList.map { it.copy(room = RoomRef(room.id)) }
                                }
                            }
                        }.awaitAll()
                    }
                }
            }.awaitAll()

            _rooms.value = roomsMap
            _devices.value = devicesMap
        } catch (e: Exception) {
            _error.value = e.message ?: "Error al cargar datos"
        } finally {
            _isLoading.value = false
        }
    }

    fun subscribeRealtime() = viewModelScope.launch {
        val token = userPreferences.getTokenOnce() ?: return@launch
        SocketManager.connect(token)
        SocketManager.deviceEvents.collect { event ->
            _devices.update { currentMap ->
                currentMap.mapValues { (_, deviceList) ->
                    deviceList.map { device ->
                        if (device.id == event.deviceId) device.copy(state = event.newState) else device
                    }
                }
            }
        }
    }

    fun toggleDevice(device: DeviceDto) = viewModelScope.launch {
        val currentlyOn = isDeviceOn(device.type.id, device.state)
        val action = toggleAction(device.type.id, currentlyOn)
        val optimisticState = buildOptimisticState(device.type.id, !currentlyOn, device.state)
        updateDevice(device.copy(state = optimisticState))
        deviceRepository.executeAction(device.id, action).onFailure {
            updateDevice(device)
        }
    }

    fun executeRoutine(routineId: String) = viewModelScope.launch {
        routineRepository.executeRoutine(routineId)
    }

    fun loadDeviceTypes() = viewModelScope.launch {
        if (_deviceTypes.value.isEmpty()) {
            deviceRepository.getDeviceTypes().onSuccess { _deviceTypes.value = it }
        }
    }

    fun createDevice(
        name: String,
        typeId: String,
        roomId: String?,
        marca: String
    ) = viewModelScope.launch {
        val fullName = if (marca.isNotBlank()) "$name - $marca" else name
        deviceRepository.createDevice(fullName, typeId, roomId)
            .onSuccess { device ->
                if (roomId != null) addDevice(roomId, device.copy(room = RoomRef(roomId)))
            }
            .onFailure { _error.value = it.message }
    }

    private fun buildOptimisticState(
        typeId: String,
        isOn: Boolean,
        current: Map<String, Any?>
    ): Map<String, Any?> = current.toMutableMap().apply {
        when (typeId) {
            DeviceTypes.PERSIANA   -> put("level", if (isOn) 100.0 else 0.0)
            DeviceTypes.ALARMA     -> put("status", if (isOn) "armed_away" else "disarmed")
            DeviceTypes.PUERTA,
            DeviceTypes.GRIFO      -> put("status", if (isOn) "opened" else "closed")
            DeviceTypes.PARLANTE   -> put("status", if (isOn) "playing" else "stopped")
            DeviceTypes.ASPIRADORA -> put("status", if (isOn) "active" else "docked")
            else                   -> put("status", if (isOn) "on" else "off")
        }
    }

    // --- Mutaciones locales ---

    fun addHome(home: HomeDto) = _homes.update { it + home }

    fun updateHome(home: HomeDto) = _homes.update { list -> list.map { if (it.id == home.id) home else it } }

    fun removeHome(homeId: String) {
        _homes.update { it.filter { h -> h.id != homeId } }
        _rooms.update { it - homeId }
    }

    fun addRoom(homeId: String, room: RoomDto) = _rooms.update { map ->
        map + (homeId to (map[homeId].orEmpty() + room))
    }

    fun updateRoom(room: RoomDto) = _rooms.update { map ->
        map.mapValues { (_, list) -> list.map { if (it.id == room.id) room else it } }
    }

    fun removeRoom(homeId: String, roomId: String) {
        _rooms.update { map -> map + (homeId to (map[homeId].orEmpty().filter { it.id != roomId })) }
        _devices.update { it - roomId }
    }

    fun addDevice(roomId: String, device: DeviceDto) = _devices.update { map ->
        map + (roomId to (map[roomId].orEmpty() + device))
    }

    fun updateDevice(device: DeviceDto) = _devices.update { map ->
        map.mapValues { (_, list) -> list.map { if (it.id == device.id) device else it } }
    }

    fun relocateDevice(device: DeviceDto) = _devices.update { map ->
        val cleaned = map.mapValues { (_, list) -> list.filter { it.id != device.id } }
        val targetKey = device.room?.id ?: "free"
        cleaned + (targetKey to (cleaned[targetKey].orEmpty() + device))
    }

    fun removeDevice(deviceId: String) = _devices.update { map ->
        map.mapValues { (_, list) -> list.filter { it.id != deviceId } }
    }

    fun addRoutine(routine: RoutineDto) = _routines.update { it + routine }

    fun updateRoutine(routine: RoutineDto) = _routines.update { list ->
        list.map { if (it.id == routine.id) routine else it }
    }

    fun removeRoutine(routineId: String) = _routines.update { it.filter { r -> r.id != routineId } }

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppViewModel(
                    homeRepository = ServiceLocator.homeRepository,
                    deviceRepository = ServiceLocator.deviceRepository,
                    routineRepository = ServiceLocator.routineRepository,
                    userPreferences = ServiceLocator.userPreferences
                )
            }
        }
    }
}
