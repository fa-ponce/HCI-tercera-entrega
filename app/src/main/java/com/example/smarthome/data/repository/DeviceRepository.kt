package com.example.smarthome.data.repository

import com.example.smarthome.AppStrings
import com.example.smarthome.R
import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class DeviceRepository(private val api: SmarthomeApi) {

    suspend fun getRoomDevices(roomId: String): Result<List<DeviceDto>> = runCatching {
        api.getRoomDevices(roomId)
    }.mapError(AppStrings.get(R.string.err_devices_load))

    suspend fun getDevice(id: String): Result<DeviceDto> = runCatching {
        api.getDevice(id)
    }.mapError(AppStrings.get(R.string.err_device_get))

    suspend fun getDeviceTypes(): Result<List<DeviceTypeDto>> = runCatching {
        api.getDeviceTypes()
    }.mapError(AppStrings.get(R.string.err_device_types_load))

    suspend fun createDevice(name: String, typeId: String, roomId: String?): Result<DeviceDto> = runCatching {
        api.createDevice(DeviceRequest(name, DeviceTypeRef(typeId), room = roomId?.let { RoomRef(it) }))
    }.mapError(AppStrings.get(R.string.err_device_create))

    suspend fun updateDevice(id: String, name: String): Result<DeviceDto> = runCatching {
        api.updateDevice(id, UpdateDeviceRequest(name))
    }.mapError(AppStrings.get(R.string.err_device_rename))

    suspend fun deleteDevice(id: String): Result<Unit> = runCatching {
        api.deleteDevice(id).requireSuccessful(AppStrings.get(R.string.err_device_delete))
    }.mapError(AppStrings.get(R.string.err_device_delete))

    suspend fun addDeviceToRoom(roomId: String, deviceId: String): Result<Unit> = runCatching {
        api.addDeviceToRoom(roomId, deviceId).requireSuccessful(AppStrings.get(R.string.err_device_link))
    }.mapError(AppStrings.get(R.string.err_device_link))

    suspend fun removeDeviceFromRoom(deviceId: String): Result<Unit> = runCatching {
        api.removeDeviceFromRoom(deviceId).requireSuccessful(AppStrings.get(R.string.err_device_unlink))
    }.mapError(AppStrings.get(R.string.err_device_unlink))

    suspend fun executeAction(
        deviceId: String,
        action: String,
        params: Map<String, Any> = emptyMap()
    ): Result<Map<String, Any?>> = runCatching {
        val raw = api.executeAction(deviceId, action, params)
        if (raw.containsKey("result")) mapOf("result" to raw["result"]) else raw
    }.mapError(AppStrings.get(R.string.err_action_execute))

    suspend fun getDeviceLogs(limit: Int, offset: Int): Result<List<LogEntryDto>> = runCatching {
        api.getDeviceLogs(limit, offset)
    }.mapError(AppStrings.get(R.string.err_history_load))

    suspend fun getDeviceLogsById(deviceId: String, limit: Int, offset: Int): Result<List<LogEntryDto>> = runCatching {
        api.getDeviceLogsById(deviceId, limit, offset)
    }.mapError(AppStrings.get(R.string.err_history_load))
}
