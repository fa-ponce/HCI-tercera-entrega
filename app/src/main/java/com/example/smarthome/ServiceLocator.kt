package com.example.smarthome

import android.content.Context
import com.example.smarthome.data.api.ApiClient
import com.example.smarthome.data.datastore.UserPreferences
import com.example.smarthome.data.repository.*

object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val userPreferences: UserPreferences by lazy { UserPreferences(appContext) }

    private val apiClient: ApiClient by lazy { ApiClient(userPreferences) }

    private val api by lazy { apiClient.api }

    val authRepository: AuthRepository by lazy { AuthRepository(api, userPreferences) }
    val homeRepository: HomeRepository by lazy { HomeRepository(api) }
    val deviceRepository: DeviceRepository by lazy { DeviceRepository(api) }
    val routineRepository: RoutineRepository by lazy { RoutineRepository(api) }
    val historyRepository: HistoryRepository by lazy { HistoryRepository(api) }
}
