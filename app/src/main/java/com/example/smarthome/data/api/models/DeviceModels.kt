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
    val name: String,
    // Consumo real en watts que reporta la API por cada tipo de dispositivo.
    val powerUsage: Int = 0
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

// La API devuelve el log con el dispositivo anidado, no campos planos:
//   { "device": { "id", "name", "room": { "name", "home": { "name" } } },
//     "action"/"actionName", "timestamp" }
// `device` puede venir como objeto o como id suelto (lo resuelve LogDeviceAdapter).
// Mantenemos además los campos planos por si alguna respuesta los trae así.
// El nombre real se resuelve en la pantalla cruzando deviceId con los dispositivos
// cargados (igual que la web), porque el log no trae un nombre fiable.
data class LogEntryDto(
    val id: String? = null,
    val device: LogDeviceDto? = null,
    @com.google.gson.annotations.SerializedName("deviceId") val deviceIdRaw: String? = null,
    @com.google.gson.annotations.SerializedName("deviceName") val deviceNameRaw: String? = null,
    @com.google.gson.annotations.SerializedName("roomName") val roomNameRaw: String? = null,
    @com.google.gson.annotations.SerializedName("homeName") val homeNameRaw: String? = null,
    @com.google.gson.annotations.SerializedName(value = "action", alternate = ["actionName", "type"])
    val event: String? = null,
    @com.google.gson.annotations.SerializedName(value = "timestamp", alternate = ["createdAt", "date"])
    val timestamp: String? = null
) {
    // Atajos para mantener compatibles las pantallas que ya usaban estos nombres.
    val deviceId: String? get() = device?.id ?: deviceIdRaw
    val deviceName: String? get() = device?.name ?: deviceNameRaw
    val roomName: String? get() = device?.room?.name ?: roomNameRaw
    val homeName: String? get() = device?.room?.home?.name ?: homeNameRaw
}

data class LogDeviceDto(
    val id: String? = null,
    val name: String? = null,
    val room: LogRoomRef? = null
)

data class LogRoomRef(
    val id: String? = null,
    val name: String? = null,
    val home: LogHomeRef? = null
)

data class LogHomeRef(
    val id: String? = null,
    val name: String? = null
)
