package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class DeviceRepository(private val api: SmarthomeApi) {

    suspend fun getRoomDevices(roomId: String): Result<List<DeviceDto>> = runCatching {
        api.getRoomDevices(roomId)
    }

    suspend fun getDevice(id: String): Result<DeviceDto> = runCatching {
        api.getDevice(id)
    }

    suspend fun getDeviceTypes(): Result<List<DeviceTypeDto>> = runCatching {
        api.getDeviceTypes()
    }

    suspend fun createDevice(name: String, typeId: String, roomId: String?): Result<DeviceDto> = runCatching {
        api.createDevice(DeviceRequest(name, DeviceTypeRef(typeId), room = roomId?.let { RoomRef(it) }))
    }

    suspend fun updateDevice(id: String, name: String): Result<DeviceDto> = runCatching {
        api.updateDevice(id, UpdateDeviceRequest(name))
    }

    suspend fun deleteDevice(id: String): Result<Unit> = runCatching {
        api.deleteDevice(id)
        Unit
    }

    suspend fun addDeviceToRoom(roomId: String, deviceId: String): Result<Unit> = runCatching {
        api.addDeviceToRoom(roomId, deviceId)
        Unit
    }

    suspend fun removeDeviceFromRoom(deviceId: String): Result<Unit> = runCatching {
        api.removeDeviceFromRoom(deviceId)
        Unit
    }

    suspend fun executeAction(
        deviceId: String,
        action: String,
        params: Map<String, Any> = emptyMap()
    ): Result<Map<String, Any?>> = runCatching {
        val raw = api.executeAction(deviceId, action, params)
        // Si la respuesta tiene { "result": ... } lo desenvuelve, si no devuelve el mapa completo
        if (raw.containsKey("result")) mapOf("result" to raw["result"]) else raw
    }

    suspend fun getDeviceLogs(limit: Int, offset: Int): Result<List<LogEntryDto>> = runCatching {
        api.getDeviceLogs(limit, offset)
    }

    suspend fun getDeviceLogsById(deviceId: String, limit: Int, offset: Int): Result<List<LogEntryDto>> = runCatching {
        api.getDeviceLogsById(deviceId, limit, offset)
    }
}
