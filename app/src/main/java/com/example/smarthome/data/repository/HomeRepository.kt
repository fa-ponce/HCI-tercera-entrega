package com.example.smarthome.data.repository

import com.example.smarthome.AppStrings
import com.example.smarthome.R
import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class HomeRepository(private val api: SmarthomeApi) {

    suspend fun getHomes(): Result<List<HomeDto>> = runCatching {
        api.getHomes()
    }.mapError(AppStrings.get(R.string.err_homes_load))

    suspend fun createHome(name: String, type: String, address: String, city: String): Result<HomeDto> = runCatching {
        api.createHome(HomeRequest(name, HomeMetadata(type, address, city)))
    }.mapError(AppStrings.get(R.string.err_home_create))

    suspend fun updateHome(id: String, name: String, type: String, address: String, city: String): Result<HomeDto> = runCatching {
        api.updateHome(id, HomeRequest(name, HomeMetadata(type, address, city)))
    }.mapError(AppStrings.get(R.string.err_home_rename))

    suspend fun deleteHome(id: String): Result<Unit> = runCatching {
        api.deleteHome(id).requireSuccessful(AppStrings.get(R.string.err_home_delete))
    }.mapError(AppStrings.get(R.string.err_home_delete))

    suspend fun getHomeRooms(homeId: String): Result<List<RoomDto>> = runCatching {
        api.getHomeRooms(homeId)
    }.mapError(AppStrings.get(R.string.err_rooms_load))

    suspend fun createRoom(name: String, type: String, floor: Int, homeId: String?): Result<RoomDto> = runCatching {
        api.createRoom(RoomRequest(name, homeId?.let { HomeRef(it) }, RoomMetadata(type, floor)))
    }.mapError(AppStrings.get(R.string.err_room_create))

    suspend fun updateRoom(
        id: String,
        name: String,
        type: String,
        homeId: String?,
        floor: Int? = null
    ): Result<RoomDto> = runCatching {
        api.updateRoom(id, RoomRequest(name, homeId?.let { HomeRef(it) }, RoomMetadata(type, floor)))
    }.mapError(AppStrings.get(R.string.err_room_rename))

    suspend fun deleteRoom(id: String): Result<Unit> = runCatching {
        api.deleteRoom(id).requireSuccessful(AppStrings.get(R.string.err_room_delete))
    }.mapError(AppStrings.get(R.string.err_room_delete))
}
