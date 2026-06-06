package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class HomeRepository(private val api: SmarthomeApi) {

    suspend fun getHomes(): Result<List<HomeDto>> = runCatching { api.getHomes() }

    suspend fun createHome(name: String, type: String, address: String, city: String): Result<HomeDto> = runCatching {
        api.createHome(HomeRequest(name, HomeMetadata(type, address, city)))
    }

    suspend fun updateHome(id: String, name: String, type: String, address: String, city: String): Result<HomeDto> = runCatching {
        api.updateHome(id, HomeRequest(name, HomeMetadata(type, address, city)))
    }

    suspend fun deleteHome(id: String): Result<Unit> = runCatching {
        api.deleteHome(id)
        Unit
    }

    suspend fun getHomeRooms(homeId: String): Result<List<RoomDto>> = runCatching {
        api.getHomeRooms(homeId)
    }

    suspend fun createRoom(name: String, type: String, floor: Int, homeId: String?): Result<RoomDto> = runCatching {
        api.createRoom(RoomRequest(name, homeId?.let { HomeRef(it) }, RoomMetadata(type, floor)))
    }

    suspend fun updateRoom(id: String, name: String, type: String, homeId: String?): Result<RoomDto> = runCatching {
        api.updateRoom(id, RoomRequest(name, homeId?.let { HomeRef(it) }, RoomMetadata(type)))
    }

    suspend fun deleteRoom(id: String): Result<Unit> = runCatching {
        api.deleteRoom(id)
        Unit
    }
}
