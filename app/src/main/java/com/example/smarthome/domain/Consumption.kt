package com.example.smarthome.domain

import com.example.smarthome.data.api.models.DeviceDto

/**
 * Consumo eléctrico calculado para un conjunto de dispositivos.
 *
 * [watts] es la potencia instantánea (solo cuentan los dispositivos encendidos),
 * [kwhPerHour] el consumo por hora, y [costPerHour]/[costPerDay] el costo estimado
 * según la tarifa. Los costos son null cuando el usuario todavía no cargó el costo
 * del kWh en su perfil.
 */
data class Consumption(
    val watts: Int,
    val kwhPerHour: Float,
    val costPerHour: Float?,
    val costPerDay: Float?
)

/**
 * Calcula el consumo de [devices] a partir de la potencia por tipo de dispositivo
 * ([powerByType], en watts) y la tarifa [costoKwh] (costo del kWh, o null si no
 * está cargada). Un dispositivo apagado consume 0 W.
 *
 * Es la única fuente de verdad de la fórmula de energía: antes se repetía igual en
 * RoomScreen y en ConsumptionScreen, lo que abría la puerta a que se desincronizaran.
 */
fun calculateConsumption(
    devices: List<DeviceDto>,
    powerByType: Map<String, Int>,
    costoKwh: Float?
): Consumption {
    val watts = devices
        .filter { isDeviceOn(it.type.id, it.state) }
        .sumOf { powerByType[it.type.id] ?: 0 }
    val kwhPerHour = watts / 1000f
    val costPerHour = costoKwh?.let { kwhPerHour * it }
    return Consumption(
        watts = watts,
        kwhPerHour = kwhPerHour,
        costPerHour = costPerHour,
        costPerDay = costPerHour?.let { it * 24f }
    )
}
