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
    private var currentToken: String? = null

    fun connect(token: String) {
        if (socket?.connected() == true && currentToken == token) return
        disconnect()
        currentToken = token
        val opts = IO.Options().apply {
            extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
        }
        socket = IO.socket(SOCKET_URL, opts).apply {
            on("device") { args ->
                val json = args.firstOrNull() as? JSONObject ?: return@on
                val deviceId = json.optString("id").takeIf { it.isNotEmpty() } ?: return@on
                val stateJson = json.optJSONObject("state") ?: return@on
                val state = buildMap<String, Any?> {
                    // org.json devuelve Integer/Long para los enteros, pero el resto de
                    // la app (estado parseado del REST vía Gson) trata los números como
                    // Double. Normalizamos a Double para que un mismo campo (p. ej.
                    // "level" de una persiana) tenga el mismo tipo venga del socket o del
                    // REST; si no, un cast `as? Double` fallaría y el estado se leería mal.
                    stateJson.keys().forEach { key ->
                        val value = stateJson.get(key)
                        put(key, if (value is Number) value.toDouble() else value)
                    }
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
        currentToken = null
    }
}
