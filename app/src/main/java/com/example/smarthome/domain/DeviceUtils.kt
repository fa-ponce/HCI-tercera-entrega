package com.example.smarthome.domain

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.smarthome.R

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

/** Recurso de string del nombre amigable del tipo de dispositivo, para subtítulos y etiquetas. */
@StringRes
fun deviceTypeName(typeId: String): Int = when (typeId) {
    DeviceTypes.LAMPARA    -> R.string.device_type_lamp
    DeviceTypes.AC         -> R.string.device_type_ac
    DeviceTypes.ALARMA     -> R.string.device_type_alarm
    DeviceTypes.PERSIANA   -> R.string.device_type_blinds
    DeviceTypes.PUERTA     -> R.string.device_type_door
    DeviceTypes.GRIFO      -> R.string.device_type_faucet
    DeviceTypes.HORNO      -> R.string.device_type_oven
    DeviceTypes.PARLANTE   -> R.string.device_type_speaker
    DeviceTypes.ASPIRADORA -> R.string.device_type_vacuum
    DeviceTypes.HELADERA   -> R.string.device_type_fridge
    else                   -> R.string.device_type_generic
}

/** Recurso de string del label (en pasado) de un evento del historial, o null si no se conoce. */
@StringRes
fun logActionLabelRes(event: String?): Int? = when (event) {
    "turnOn"        -> R.string.action_turned_on
    "turnOff"       -> R.string.action_turned_off
    "open"          -> R.string.action_opened
    "close"         -> R.string.action_closed
    "armAway", "arm" -> R.string.action_armed
    "disarm"        -> R.string.action_disarmed
    "trigger"       -> R.string.action_triggered
    "play"          -> R.string.action_playing
    "pause"         -> R.string.action_paused
    "stop"          -> R.string.action_stopped
    "startCleaning" -> R.string.action_cleaning
    "dock"          -> R.string.action_docked
    "up"            -> R.string.action_raised
    "down"          -> R.string.action_lowered
    "lock"          -> R.string.action_locked
    "unlock"        -> R.string.action_unlocked
    "start"         -> R.string.action_started
    "setBrightness" -> R.string.action_brightness_set
    "setTemperature" -> R.string.action_temperature_set
    "setColor"      -> R.string.action_color_set
    "setVolume"     -> R.string.action_volume_set
    else            -> null
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
