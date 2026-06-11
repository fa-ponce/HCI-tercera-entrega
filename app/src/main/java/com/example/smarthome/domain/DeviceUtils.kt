package com.example.smarthome.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object DeviceTypes {
    const val LAMPARA     = "eu0v2xgprrhhg41g"
    const val AC          = "go46xmbqeomjrsjr"
    const val ALARMA      = "im77xxyulpegfmv8"
    const val PERSIANA    = "mxztsyjzsrq7iaqc"
    const val PUERTA      = "c89qmhhzm3bcpoie"
    const val GRIFO       = "dbrlsh0juhf3dhf0"
    const val HORNO       = "lsf78ly0eqrjbz91"
    const val PARLANTE    = "fud5vmuy0fkh6zt9"
    const val ASPIRADORA  = "ofglvd9gqx8yfl3l"
    const val HELADERA    = "rnizejqr2di0okho"
}

private data class ToggleConfig(val on: String, val off: String, val isOn: (Map<String, Any?>) -> Boolean)

private val TYPE_TOGGLE = mapOf(
    DeviceTypes.LAMPARA    to ToggleConfig("turnOn",  "turnOff", { it["status"] == "on" }),
    DeviceTypes.AC         to ToggleConfig("turnOn",  "turnOff", { it["status"] == "on" }),
    DeviceTypes.ALARMA     to ToggleConfig("armAway", "disarm",  { it["status"] != "disarmed" }),
    DeviceTypes.PERSIANA   to ToggleConfig("up",      "down",    { (it["level"] as? Double ?: 0.0) > 0.0 }),
    DeviceTypes.PUERTA     to ToggleConfig("open",    "close",   { it["status"] == "opened" }),
    DeviceTypes.GRIFO      to ToggleConfig("open",    "close",   { it["status"] == "opened" }),
    DeviceTypes.HORNO      to ToggleConfig("turnOn",  "turnOff", { it["status"] == "on" }),
    DeviceTypes.PARLANTE   to ToggleConfig("play",    "stop",    { it["status"] == "playing" }),
    DeviceTypes.ASPIRADORA to ToggleConfig("start",   "dock",    { it["status"] != "docked" }),
    DeviceTypes.HELADERA   to ToggleConfig("",        "",        { true }),
)

private val NO_TOGGLE = setOf(DeviceTypes.HELADERA, DeviceTypes.ALARMA)

fun canToggle(typeId: String): Boolean = typeId !in NO_TOGGLE

fun isDeviceOn(typeId: String, state: Map<String, Any?>): Boolean {
    val config = TYPE_TOGGLE[typeId]
    return config?.isOn?.invoke(state) ?: (state["status"] == "on")
}

fun toggleAction(typeId: String, isCurrentlyOn: Boolean): String {
    val config = TYPE_TOGGLE[typeId]
    return if (isCurrentlyOn) config?.off ?: "turnOff" else config?.on ?: "turnOn"
}

fun deviceIcon(typeId: String): ImageVector = when (typeId) {
    DeviceTypes.LAMPARA    -> Icons.Rounded.LightMode
    DeviceTypes.AC         -> Icons.Rounded.AcUnit
    DeviceTypes.ALARMA     -> Icons.Rounded.Security
    DeviceTypes.PERSIANA   -> Icons.Rounded.Blinds
    DeviceTypes.PUERTA     -> Icons.Rounded.MeetingRoom
    DeviceTypes.GRIFO      -> Icons.Rounded.WaterDrop
    DeviceTypes.HORNO      -> Icons.Rounded.Microwave
    DeviceTypes.PARLANTE   -> Icons.Rounded.Speaker
    DeviceTypes.ASPIRADORA -> Icons.Rounded.CleaningServices
    DeviceTypes.HELADERA   -> Icons.Rounded.Kitchen
    else                   -> Icons.Rounded.DevicesOther
}

/** Nombre amigable en español del tipo de dispositivo, para subtítulos y etiquetas. */
fun deviceTypeName(typeId: String): String = when (typeId) {
    DeviceTypes.LAMPARA    -> "Lámpara"
    DeviceTypes.AC         -> "Aire acondicionado"
    DeviceTypes.ALARMA     -> "Alarma"
    DeviceTypes.PERSIANA   -> "Persiana"
    DeviceTypes.PUERTA     -> "Puerta"
    DeviceTypes.GRIFO      -> "Grifo"
    DeviceTypes.HORNO      -> "Horno"
    DeviceTypes.PARLANTE   -> "Parlante"
    DeviceTypes.ASPIRADORA -> "Aspiradora"
    DeviceTypes.HELADERA   -> "Heladera"
    else                   -> "Dispositivo"
}

fun deviceConsumptionW(typeId: String): Int = when (typeId) {
    DeviceTypes.LAMPARA    -> 10
    DeviceTypes.AC         -> 1500
    DeviceTypes.ALARMA     -> 5
    DeviceTypes.PERSIANA   -> 50
    DeviceTypes.PUERTA     -> 20
    DeviceTypes.GRIFO      -> 10
    DeviceTypes.HORNO      -> 2000
    DeviceTypes.PARLANTE   -> 30
    DeviceTypes.ASPIRADORA -> 1200
    DeviceTypes.HELADERA   -> 150
    else                   -> 0
}
