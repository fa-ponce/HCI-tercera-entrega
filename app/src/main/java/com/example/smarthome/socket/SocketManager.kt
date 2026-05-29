package com.example.smarthome.socket

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject

private const val SOCKET_URL = "https://hci.it.itba.edu.ar"

data class DeviceEvent(val deviceId: String, val newState: Map<String, Any?>)

object SocketManager {

    private val _deviceEvents = MutableSharedFlow<DeviceEvent>(extraBufferCapacity = 32)
    val deviceEvents: SharedFlow<DeviceEvent> = _deviceEvents.asSharedFlow()

    private var socket: Socket? = null

    fun connect(token: String) {
        if (socket?.connected() == true) return
        val opts = IO.Options().apply {
            extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
        }
        socket = IO.socket(SOCKET_URL, opts).apply {
            on("device") { args ->
                val json = args.firstOrNull() as? JSONObject ?: return@on
                val deviceId = json.optString("id").takeIf { it.isNotEmpty() } ?: return@on
                val stateJson = json.optJSONObject("state") ?: return@on
                val state = buildMap<String, Any?> {
                    stateJson.keys().forEach { key -> put(key, stateJson.get(key)) }
                }
                _deviceEvents.tryEmit(DeviceEvent(deviceId, state))
            }
            connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off("device")
        socket = null
    }
}
