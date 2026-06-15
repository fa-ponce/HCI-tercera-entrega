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
    DeviceTypes.LAMPARA    -> Icons.Rounded.Lightbulb
    DeviceTypes.AC         -> Icons.Rounded.Hvac
    DeviceTypes.ALARMA     -> Icons.Rounded.Notifications
    DeviceTypes.PERSIANA   -> Icons.Rounded.Blinds
    DeviceTypes.PUERTA     -> Icons.Rounded.MeetingRoom
    DeviceTypes.GRIFO      -> Icons.Rounded.WaterDrop
    DeviceTypes.HORNO      -> Icons.Rounded.Microwave
    DeviceTypes.PARLANTE   -> Icons.Rounded.Speaker
    DeviceTypes.ASPIRADORA -> Icons.Rounded.Radar
    DeviceTypes.HELADERA   -> Icons.Rounded.Kitchen
    else                   -> Icons.Rounded.DevicesOther
}

fun roomTypeIcon(type: String?): ImageVector = when (type) {
    "Living"    -> Icons.Rounded.Tv
    "Dormitorio"-> Icons.Rounded.Hotel
    "Cocina"    -> Icons.Rounded.Kitchen
    "Baño"      -> Icons.Rounded.Bathtub
    "Garaje"    -> Icons.Rounded.DirectionsCar
    "Estudio"   -> Icons.Rounded.Work
    "Comedor"   -> Icons.Rounded.TableRestaurant
    "Lavadero"  -> Icons.Rounded.LocalLaundryService
    else        -> Icons.Rounded.MeetingRoom
}

/**
 * Etiqueta localizada para un tipo de habitación. El valor guardado en la API
 * sigue siendo el español canónico ("Living", "Dormitorio", ...); esto solo
 * traduce lo que se muestra. Devuelve null si el tipo no es uno conocido (se
 * muestra el valor crudo como fallback).
 */
@StringRes
fun roomTypeLabelRes(type: String?): Int? = when (type) {
    "Living"     -> R.string.room_type_living
    "Dormitorio" -> R.string.room_type_bedroom
    "Cocina"     -> R.string.room_type_kitchen
    "Baño"       -> R.string.room_type_bathroom
    "Garaje"     -> R.string.room_type_garage
    "Estudio"    -> R.string.room_type_study
    "Comedor"    -> R.string.room_type_dining
    "Lavadero"   -> R.string.room_type_laundry
    else         -> null
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
    "setMode"       -> R.string.action_mode_set
    "setVerticalSwing"   -> R.string.action_vertical_swing_set
    "setHorizontalSwing" -> R.string.action_horizontal_swing_set
    "setFanSpeed"   -> R.string.action_fan_speed_set
    "changeSecurityCode" -> R.string.action_security_code_changed
    "armStay"       -> R.string.action_armed_stay
    "setLevel"      -> R.string.action_level_set
    "dispense"      -> R.string.action_dispensed
    "setHeat"       -> R.string.action_heat_set
    "setGrill"      -> R.string.action_grill_set
    "setConvection" -> R.string.action_convection_set
    "setFreezerTemperature" -> R.string.action_freezer_temp_set
    "resume"        -> R.string.action_resumed
    "nextSong"      -> R.string.action_next_song
    "previousSong"  -> R.string.action_previous_song
    "setGenre"      -> R.string.action_genre_set
    "getPlaylist"   -> R.string.action_playlist_viewed
    "setLocation"   -> R.string.action_location_set
    else            -> null
}

// El consumo (watts) por tipo de dispositivo ya NO está hardcodeado: se toma del
// campo `powerUsage` que devuelve la API (ver AppViewModel.powerByType).
