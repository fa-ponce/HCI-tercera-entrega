package com.example.smarthome.data.repository

import com.example.smarthome.AppStrings
import com.example.smarthome.R
import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class RoutineRepository(private val api: SmarthomeApi) {

    suspend fun getRoutines(): Result<List<RoutineDto>> = runCatching {
        api.getRoutines()
    }.mapError(AppStrings.get(R.string.err_routines_load))

    suspend fun createRoutine(request: RoutineRequest): Result<RoutineDto> = runCatching {
        api.createRoutine(request)
    }.mapError(AppStrings.get(R.string.err_routine_create))

    suspend fun updateRoutine(id: String, request: RoutineRequest): Result<RoutineDto> = runCatching {
        api.updateRoutine(id, request)
    }.mapError(AppStrings.get(R.string.err_routine_save))

    suspend fun deleteRoutine(id: String): Result<Unit> = runCatching {
        api.deleteRoutine(id).requireSuccessful(AppStrings.get(R.string.err_routine_delete))
    }.mapError(AppStrings.get(R.string.err_routine_delete))

    suspend fun executeRoutine(id: String): Result<Unit> = runCatching {
        api.executeRoutine(id, emptyMap()).requireSuccessful(AppStrings.get(R.string.err_routine_execute))
    }.mapError(AppStrings.get(R.string.err_routine_execute))
}
