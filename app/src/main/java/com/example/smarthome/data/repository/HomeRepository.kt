package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*

class HomeRepository(private val api: SmarthomeApi) {

    suspend fun getHomes(): Result<List<HomeDto>> = runCatching {
        api.getHomes()
    }.mapError("Error al cargar las casas")

    suspend fun createHome(name: String, type: String, address: String, city: String): Result<HomeDto> = runCatching {
        api.createHome(HomeRequest(name, HomeMetadata(type, address, city)))
    }.mapError("Error al crear la casa")

    suspend fun updateHome(id: String, name: String, type: String, address: String, city: String): Result<HomeDto> = runCatching {
        api.updateHome(id, HomeRequest(name, HomeMetadata(type, address, city)))
    }.mapError("Error al renombrar la casa")

    suspend fun deleteHome(id: String): Result<Unit> = runCatching {
        api.deleteHome(id).requireSuccessful("Error al eliminar la casa")
    }.mapError("Error al eliminar la casa")

    suspend fun getHomeRooms(homeId: String): Result<List<RoomDto>> = runCatching {
        api.getHomeRooms(homeId)
    }.mapError("Error al cargar las habitaciones")

    suspend fun createRoom(name: String, type: String, floor: Int, homeId: String?): Result<RoomDto> = runCatching {
        api.createRoom(RoomRequest(name, homeId?.let { HomeRef(it) }, RoomMetadata(type, floor)))
    }.mapError("Error al crear la habitación")

    suspend fun updateRoom(
        id: String,
        name: String,
        type: String,
        homeId: String?,
        floor: Int? = null
    ): Result<RoomDto> = runCatching {
        api.updateRoom(id, RoomRequest(name, homeId?.let { HomeRef(it) }, RoomMetadata(type, floor)))
    }.mapError("Error al renombrar la habitación")

    suspend fun deleteRoom(id: String): Result<Unit> = runCatching {
        api.deleteRoom(id).requireSuccessful("Error al eliminar la habitación")
    }.mapError("Error al eliminar la habitación")
}
