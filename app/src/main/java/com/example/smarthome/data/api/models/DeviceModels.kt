package com.example.smarthome.data.api.models

data class DeviceDto(
    val id: String,
    val name: String,
    val type: DeviceTypeRef,
    val state: Map<String, Any?> = emptyMap(),
    val room: RoomRef? = null
)

data class RoomRef(val id: String)

data class DeviceTypeRef(
    val id: String,
    val name: String? = null
)

data class DeviceTypeDto(
    val id: String,
    val name: String
)

data class DeviceRequest(
    val name: String,
    val type: DeviceTypeRef,
    val state: Map<String, @JvmSuppressWildcards Any?> = emptyMap(),
    val room: RoomRef? = null
)

data class UpdateDeviceRequest(
    val name: String,
    val type: DeviceTypeRef? = null
)

data class LogEntryDto(
    val id: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    @com.google.gson.annotations.SerializedName("actionName") val event: String? = null,
    val timestamp: String? = null,
    val roomName: String? = null,
    val homeName: String? = null
)
