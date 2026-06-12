package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.LogEntryDto

private const val DEFAULT_PAGE_SIZE = 20

class HistoryRepository(private val api: SmarthomeApi) {

    suspend fun getLogs(limit: Int = DEFAULT_PAGE_SIZE, offset: Int = 0): Result<List<LogEntryDto>> = runCatching {
        api.getDeviceLogs(limit, offset)
    }

    suspend fun getDeviceLogs(deviceId: String, limit: Int = DEFAULT_PAGE_SIZE, offset: Int = 0): Result<List<LogEntryDto>> = runCatching {
        api.getDeviceLogsById(deviceId, limit, offset)
    }
}
