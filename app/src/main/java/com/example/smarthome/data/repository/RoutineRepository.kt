package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class RoutineRepository(private val api: SmarthomeApi) {

    suspend fun getRoutines(): Result<List<RoutineDto>> = runCatching {
        api.getRoutines()
    }.mapError("Error al cargar las rutinas")

    suspend fun createRoutine(request: RoutineRequest): Result<RoutineDto> = runCatching {
        api.createRoutine(request)
    }.mapError("Error al crear la rutina")

    suspend fun updateRoutine(id: String, request: RoutineRequest): Result<RoutineDto> = runCatching {
        api.updateRoutine(id, request)
    }.mapError("Error al guardar la rutina")

    suspend fun deleteRoutine(id: String): Result<Unit> = runCatching {
        api.deleteRoutine(id)
        Unit
    }.mapError("Error al eliminar la rutina")

    suspend fun executeRoutine(id: String): Result<Unit> = runCatching {
        api.executeRoutine(id, emptyMap())
        Unit
    }.mapError("Error al ejecutar la rutina")
}
