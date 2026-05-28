package com.example.smarthome.data.api.models

data class RoutineDto(
    val id: String,
    val name: String,
    val actions: List<RoutineActionDto> = emptyList(),
    val metadata: RoutineMetaDto? = null
)

data class RoutineActionDto(
    val device: DeviceRef,
    val actionName: String,
    val params: List<@JvmSuppressWildcards Any?> = emptyList(),
    val duration: Int? = null
)

data class DeviceRef(val id: String)

data class RoutineMetaDto(
    val descripcion: String? = null,
    val trigger: String? = null,
    val tipoTrigger: String? = null,
    val enabled: Boolean? = null
)

data class RoutineRequest(
    val name: String,
    val actions: List<RoutineActionDto>,
    val metadata: RoutineMetaDto? = null
)
