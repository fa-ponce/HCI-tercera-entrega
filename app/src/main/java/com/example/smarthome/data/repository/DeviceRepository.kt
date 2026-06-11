package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class DeviceRepository(private val api: SmarthomeApi) {

    suspend fun getRoomDevices(roomId: String): Result<List<DeviceDto>> = runCatching {
        api.getRoomDevices(roomId)
    }.mapError("Error al cargar los dispositivos")

    suspend fun getDevice(id: String): Result<DeviceDto> = runCatching {
        api.getDevice(id)
    }.mapError("Error al obtener el dispositivo")

    suspend fun getDeviceTypes(): Result<List<DeviceTypeDto>> = runCatching {
        api.getDeviceTypes()
    }.mapError("Error al cargar los tipos de dispositivo")

    suspend fun createDevice(name: String, typeId: String, roomId: String?): Result<DeviceDto> = runCatching {
        api.createDevice(DeviceRequest(name, DeviceTypeRef(typeId), room = roomId?.let { RoomRef(it) }))
    }.mapError("Error al crear el dispositivo")

    suspend fun updateDevice(id: String, name: String): Result<DeviceDto> = runCatching {
        api.updateDevice(id, UpdateDeviceRequest(name))
    }.mapError("Error al renombrar el dispositivo")

    suspend fun deleteDevice(id: String): Result<Unit> = runCatching {
        api.deleteDevice(id).requireSuccessful("Error al eliminar el dispositivo")
    }.mapError("Error al eliminar el dispositivo")

    suspend fun addDeviceToRoom(roomId: String, deviceId: String): Result<Unit> = runCatching {
        api.addDeviceToRoom(roomId, deviceId).requireSuccessful("Error al vincular el dispositivo")
    }.mapError("Error al vincular el dispositivo")

    suspend fun removeDeviceFromRoom(deviceId: String): Result<Unit> = runCatching {
        api.removeDeviceFromRoom(deviceId).requireSuccessful("Error al desvincular el dispositivo")
    }.mapError("Error al desvincular el dispositivo")

    suspend fun executeAction(
        deviceId: String,
        action: String,
        params: Map<String, Any> = emptyMap()
    ): Result<Map<String, Any?>> = runCatching {
        val raw = api.executeAction(deviceId, action, params)
        if (raw.containsKey("result")) mapOf("result" to raw["result"]) else raw
    }.mapError("Error al ejecutar la acción")

    suspend fun getDeviceLogs(limit: Int, offset: Int): Result<List<LogEntryDto>> = runCatching {
        api.getDeviceLogs(limit, offset)
    }.mapError("Error al cargar el historial")

    suspend fun getDeviceLogsById(deviceId: String, limit: Int, offset: Int): Result<List<LogEntryDto>> = runCatching {
        api.getDeviceLogsById(deviceId, limit, offset)
    }.mapError("Error al cargar el historial")
}
