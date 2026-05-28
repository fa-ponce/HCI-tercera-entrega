package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class RoutineRepository(private val api: SmarthomeApi) {

    suspend fun getRoutines(): Result<List<RoutineDto>> = runCatching { api.getRoutines() }

    suspend fun createRoutine(request: RoutineRequest): Result<RoutineDto> = runCatching {
        api.createRoutine(request)
    }

    suspend fun updateRoutine(id: String, request: RoutineRequest): Result<RoutineDto> = runCatching {
        api.updateRoutine(id, request)
    }

    suspend fun deleteRoutine(id: String): Result<Unit> = runCatching {
        api.deleteRoutine(id)
        Unit
    }

    suspend fun executeRoutine(id: String): Result<Unit> = runCatching {
        api.executeRoutine(id, emptyMap())
        Unit
    }
}
