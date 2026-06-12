package com.example.smarthome.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smarthome.R
import com.example.smarthome.ServiceLocator
import com.example.smarthome.ui.notifications.NotificationHelper
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
import com.example.smarthome.data.repository.isNetworkError
import retrofit2.HttpException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppViewModel(
    application: Application,
    private val homeRepository: HomeRepository,
    private val deviceRepository: DeviceRepository,
    private val routineRepository: RoutineRepository,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    private val _homes = MutableStateFlow<List<HomeDto>>(emptyList())
    val homes: StateFlow<List<HomeDto>> = _homes.asStateFlow()

    private val _rooms = MutableStateFlow<Map<String, List<RoomDto>>>(emptyMap())
    val rooms: StateFlow<Map<String, List<RoomDto>>> = _rooms.asStateFlow()

    // Habitaciones "sin casa" (standalone): viven en la casa oculta pero con un
    // tipo normal. Se muestran en su propio apartado "Sin casa".
    private val _standaloneRooms = MutableStateFlow<List<RoomDto>>(emptyList())
    val standaloneRooms: StateFlow<List<RoomDto>> = _standaloneRooms.asStateFlow()

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

    private val _errorEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    private val _forceLogout = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val forceLogout: SharedFlow<Unit> = _forceLogout.asSharedFlow()

    private var realtimeJob: Job? = null

    private val appContext: Application get() = getApplication()

    private fun notify(title: String, body: String) {
        NotificationHelper.notify(appContext, title, body)
    }

    fun logout() = viewModelScope.launch {
        realtimeJob?.cancel()
        realtimeJob = null
        SocketManager.disconnect()
        ServiceLocator.authRepository.logout()
            .onFailure { userPreferences.clear() }
        clearLocalState()
    }

    fun clearError() { _error.value = null }

    fun retryLoad() = loadAll()

    // La API no permite listar dispositivos "sueltos": un dispositivo sin
    // habitación queda huérfano y no se puede volver a obtener. Por eso, igual
    // que la web, guardamos los dispositivos "sin casa" en una casa+habitación
    // ocultas marcadas con este tipo especial.
    private val freeType = "__libre__"
    private var freeRoomId: String? = null
    private var freeHomeId: String? = null

    /**
     * Busca (o crea) la casa y habitación ocultas para dispositivos libres y
     * devuelve el id de esa habitación. También cachea el id de la casa oculta.
     */
    private suspend fun ensureFreeRoomId(knownHomes: List<HomeDto>? = null): String? {
        freeRoomId?.let { return it }
        val homes = knownHomes ?: homeRepository.getHomes().getOrNull() ?: return null
        val freeHome = homes.find { it.metadata?.type == freeType }
            ?: homeRepository.createHome(freeType, freeType, "", "").getOrNull()
            ?: return null
        freeHomeId = freeHome.id
        val freeRooms = homeRepository.getHomeRooms(freeHome.id).getOrNull() ?: emptyList()
        val freeRoom = freeRooms.find { it.metadata?.type == freeType }
            ?: homeRepository.createRoom(freeType, freeType, 0, freeHome.id).getOrNull()
            ?: return null
        freeRoomId = freeRoom.id
        return freeRoom.id
    }

    fun loadAll() = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        try {
            val homesJob = async { homeRepository.getHomes().getOrThrow() }
            val routinesJob = async { routineRepository.getRoutines().getOrDefault(emptyList()) }

            val homes = homesJob.await()
            // Aseguramos los contenedores ocultos y excluimos la casa especial
            // de la lista visible de casas.
            val freeRoom = ensureFreeRoomId(homes)
            val realHomes = homes.filter { it.metadata?.type != freeType }
            _homes.value = realHomes
            _routines.value = routinesJob.await()

            val roomsMap = mutableMapOf<String, List<RoomDto>>()
            val devicesMap = mutableMapOf<String, List<DeviceDto>>()

            realHomes.map { home ->
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

            // Dispositivos libres = los de la habitación oculta, mostrados como "sin casa".
            freeRoom?.let { frId ->
                deviceRepository.getRoomDevices(frId).getOrNull()?.let { freeList ->
                    devicesMap["free"] = freeList.map { it.copy(room = null) }
                }
            }

            // Habitaciones "sin casa" = las de la casa oculta, salvo la habitación
            // especial de dispositivos libres. Cargamos también sus dispositivos.
            freeHomeId?.let { fhId ->
                val hiddenRooms = homeRepository.getHomeRooms(fhId).getOrNull() ?: emptyList()
                val standalone = hiddenRooms.filter { it.id != freeRoom && it.metadata?.type != freeType }
                _standaloneRooms.value = standalone
                standalone.map { room ->
                    async {
                        deviceRepository.getRoomDevices(room.id).getOrNull()?.let { deviceList ->
                            devicesMap[room.id] = deviceList.map { it.copy(room = RoomRef(room.id)) }
                        }
                    }
                }.awaitAll()
            }

            _rooms.value = roomsMap
            _devices.value = devicesMap
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 401) {
                realtimeJob?.cancel()
                realtimeJob = null
                SocketManager.disconnect()
                userPreferences.clear()
                clearLocalState()
                _forceLogout.tryEmit(Unit)
                return@launch
            }
            val msg = if (e.isNetworkError())
                "Sin conexión a internet. Verificá tu red."
            else
                e.message ?: "Error al cargar los datos"
            _error.value = msg
            if (_homes.value.isNotEmpty()) _errorEvent.tryEmit(msg)
        } finally {
            _isLoading.value = false
        }
    }

    fun subscribeRealtime() {
        if (realtimeJob?.isActive == true) return
        realtimeJob = viewModelScope.launch {
            val token = userPreferences.getTokenOnce() ?: return@launch
            SocketManager.connect(token)
            SocketManager.deviceEvents.collect { event ->
                val changed = _devices.value.values
                    .flatten()
                    .find { it.id == event.deviceId }

                _devices.update { currentMap ->
                    currentMap.mapValues { (_, deviceList) ->
                        deviceList.map { device ->
                            if (device.id == event.deviceId) device.copy(state = event.newState) else device
                        }
                    }
                }

                if (changed != null) {
                    val titleRes = when (changed.type.id) {
                        DeviceTypes.ALARMA -> R.string.notif_alarm_title
                        DeviceTypes.PUERTA -> R.string.notif_door_title
                        else -> null
                    }
                    if (titleRes != null) {
                        val stateLabel = isDeviceOn(changed.type.id, event.newState)
                            .let { on -> if (on) R.string.notif_state_on else R.string.notif_state_off }
                        notify(
                            appContext.getString(titleRes),
                            "${changed.name}: ${appContext.getString(stateLabel)}"
                        )
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
            notify(
                appContext.getString(R.string.notif_device_error_title),
                appContext.getString(R.string.notif_device_error_body, device.name)
            )
        }
    }

    fun executeRoutine(routineId: String) = viewModelScope.launch {
        val routineName = _routines.value.find { it.id == routineId }?.name ?: ""
        routineRepository.executeRoutine(routineId)
            .onSuccess {
                notify(
                    appContext.getString(R.string.notif_routine_success_title),
                    routineName
                )
            }
            .onFailure {
                notify(
                    appContext.getString(R.string.notif_routine_error_title),
                    appContext.getString(R.string.notif_routine_error_body, routineName)
                )
            }
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
        marca: String,
        onResult: ((success: Boolean) -> Unit)? = null
    ) = viewModelScope.launch {
        val fullName = if (marca.isNotBlank()) "$name - $marca" else name
        // Si no tiene casa, lo creamos en la habitación oculta para que persista.
        val targetRoomId = roomId ?: ensureFreeRoomId()
        if (roomId == null && targetRoomId == null) {
            _errorEvent.tryEmit("No se pudo preparar el espacio para dispositivos sin casa")
            onResult?.invoke(false)
            return@launch
        }
        deviceRepository.createDevice(fullName, typeId, targetRoomId)
            .onSuccess { device ->
                if (roomId == null) {
                    // Dispositivo libre: lo mostramos como "sin casa".
                    addDevice("free", device.copy(room = null))
                } else {
                    addDevice(roomId, device.copy(room = RoomRef(roomId)))
                }
                onResult?.invoke(true)
            }
            .onFailure {
                _errorEvent.tryEmit(it.message ?: "Error al crear el dispositivo")
                onResult?.invoke(false)
            }
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

    fun updateRoom(room: RoomDto) {
        _rooms.update { map ->
            map.mapValues { (_, list) -> list.map { if (it.id == room.id) room else it } }
        }
        _standaloneRooms.update { list -> list.map { if (it.id == room.id) room else it } }
    }

    fun removeRoom(homeId: String, roomId: String) {
        _rooms.update { map -> map + (homeId to (map[homeId].orEmpty().filter { it.id != roomId })) }
        _standaloneRooms.update { list -> list.filter { it.id != roomId } }
        _devices.update { it - roomId }
    }

    /**
     * Guarda nombre/tipo/casa de una habitación. Si homeId es null la habitación
     * queda "sin casa": igual que la web, se mueve a la casa oculta.
     */
    fun saveRoom(
        room: RoomDto,
        name: String,
        type: String,
        homeId: String?,
        onResult: ((success: Boolean, error: String?) -> Unit)? = null
    ) = viewModelScope.launch {
        val targetHomeId = homeId ?: run { ensureFreeRoomId(); freeHomeId }
        if (targetHomeId == null) {
            onResult?.invoke(false, appContext.getString(R.string.sheet_free_room_error))
            return@launch
        }
        homeRepository.updateRoom(room.id, name, type, targetHomeId, room.metadata?.floor)
            .onSuccess { updated ->
                _rooms.update { map -> map.mapValues { (_, list) -> list.filter { it.id != room.id } } }
                _standaloneRooms.update { list -> list.filter { it.id != room.id } }
                if (homeId != null) {
                    _rooms.update { map -> map + (homeId to (map[homeId].orEmpty() + updated)) }
                } else {
                    _standaloneRooms.update { it + updated }
                }
                onResult?.invoke(true, null)
            }
            .onFailure { onResult?.invoke(false, it.message) }
    }

    /**
     * Elimina una habitación moviendo antes sus dispositivos a la habitación
     * oculta para que queden libres y reasignables, como hace la web.
     */
    fun deleteRoomFreeingDevices(
        room: RoomDto,
        onResult: ((success: Boolean, error: String?) -> Unit)? = null
    ) = viewModelScope.launch {
        val roomDevices = _devices.value[room.id].orEmpty()
        if (roomDevices.isNotEmpty()) {
            ensureFreeRoomId()?.let { frId ->
                roomDevices.map { dev ->
                    async { deviceRepository.addDeviceToRoom(frId, dev.id) }
                }.awaitAll()
            }
        }
        homeRepository.deleteRoom(room.id)
            .onSuccess {
                val freed = roomDevices.map { it.copy(room = null) }
                removeRoom(room.home?.id ?: "", room.id)
                if (freed.isNotEmpty()) {
                    _devices.update { map -> map + ("free" to (map["free"].orEmpty() + freed)) }
                }
                onResult?.invoke(true, null)
            }
            .onFailure { onResult?.invoke(false, it.message) }
    }

    /** Crea una habitación "sin casa" (dentro de la casa oculta) y la agrega al apartado. */
    fun createStandaloneRoom(
        name: String,
        type: String,
        floor: Int,
        onResult: ((success: Boolean, error: String?) -> Unit)? = null
    ) = viewModelScope.launch {
        ensureFreeRoomId() // asegura freeHomeId
        val fhId = freeHomeId
        if (fhId == null) {
            onResult?.invoke(false, appContext.getString(R.string.sheet_free_room_error))
            return@launch
        }
        homeRepository.createRoom(name, type, floor, fhId)
            .onSuccess { room ->
                _standaloneRooms.update { it + room }
                onResult?.invoke(true, null)
            }
            .onFailure {
                onResult?.invoke(false, it.message)
            }
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

    private fun clearLocalState() {
        _homes.value = emptyList()
        _rooms.value = emptyMap()
        _standaloneRooms.value = emptyList()
        _devices.value = emptyMap()
        _routines.value = emptyList()
        _deviceTypes.value = emptyList()
        _error.value = null
        freeRoomId = null
        freeHomeId = null
    }

    fun executeDeviceAction(
        deviceId: String,
        action: String,
        params: Map<String, Any> = emptyMap(),
        onResult: ((Result<Map<String, Any?>>) -> Unit)? = null
    ) = viewModelScope.launch {
        val result = deviceRepository.executeAction(deviceId, action, params)
        result.onFailure { _errorEvent.tryEmit(it.message ?: "Error ejecutando acción") }
        onResult?.invoke(result)
    }

    fun renameDevice(
        deviceId: String,
        newName: String,
        onResult: (Result<DeviceDto>) -> Unit
    ) = viewModelScope.launch {
        val result = deviceRepository.updateDevice(deviceId, newName)
        result.onSuccess { updateDevice(it) }
        onResult(result)
    }

    fun deleteDevice(
        deviceId: String,
        onResult: (Result<Unit>) -> Unit
    ) = viewModelScope.launch {
        val result = deviceRepository.deleteDevice(deviceId)
        result.onSuccess { removeDevice(deviceId) }
        onResult(result)
    }

    fun linkDeviceToRoom(
        deviceId: String,
        roomId: String?,
        onResult: (Result<Unit>) -> Unit
    ) = viewModelScope.launch {
        val targetRoomId = roomId ?: ensureFreeRoomId() ?: run {
            onResult(Result.failure(Exception(appContext.getString(R.string.sheet_free_room_error))))
            return@launch
        }
        val result = deviceRepository.addDeviceToRoom(targetRoomId, deviceId)
        result.onSuccess { _ ->
            val current = _devices.value.values.flatten().find { d -> d.id == deviceId }
            if (current != null) relocateDevice(current.copy(room = roomId?.let { RoomRef(it) }))
        }
        onResult(result)
    }

    suspend fun loadDevice(deviceId: String): DeviceDto? =
        deviceRepository.getDevice(deviceId).getOrNull()

    override fun onCleared() {
        super.onCleared()
        SocketManager.disconnect()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AppViewModel(
                    application = this[APPLICATION_KEY] as Application,
                    homeRepository = ServiceLocator.homeRepository,
                    deviceRepository = ServiceLocator.deviceRepository,
                    routineRepository = ServiceLocator.routineRepository,
                    userPreferences = ServiceLocator.userPreferences
                )
            }
        }
    }
}
