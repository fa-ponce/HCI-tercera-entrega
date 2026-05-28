package com.example.smarthome.data.api

import com.example.smarthome.data.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface SmarthomeApi {

    // Auth
    @POST("users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("users/logout")
    suspend fun logout(): Response<Unit>

    @POST("users/register")
    suspend fun register(@Body body: RegisterRequest): UserDto

    @POST("users/verify-account")
    suspend fun verifyAccount(@Body body: VerifyCodeRequest): Response<Unit>

    @POST("users/send-verification")
    suspend fun sendVerification(@Body body: EmailRequest): Response<Unit>

    @POST("users/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<Unit>

    @POST("users/forgot-password")
    suspend fun forgotPassword(@Body body: EmailRequest): Response<Unit>

    @POST("users/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<Unit>

    // Homes
    @GET("homes")
    suspend fun getHomes(): List<HomeDto>

    @POST("homes")
    suspend fun createHome(@Body body: HomeRequest): HomeDto

    @PUT("homes/{id}")
    suspend fun updateHome(@Path("id") id: String, @Body body: HomeRequest): HomeDto

    @DELETE("homes/{id}")
    suspend fun deleteHome(@Path("id") id: String): Response<Unit>

    @GET("homes/{homeId}/rooms")
    suspend fun getHomeRooms(@Path("homeId") homeId: String): List<RoomDto>

    // Rooms
    @POST("rooms")
    suspend fun createRoom(@Body body: RoomRequest): RoomDto

    @PUT("rooms/{id}")
    suspend fun updateRoom(@Path("id") id: String, @Body body: RoomRequest): RoomDto

    @DELETE("rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: String): Response<Unit>

    @GET("rooms/{roomId}/devices")
    suspend fun getRoomDevices(@Path("roomId") roomId: String): List<DeviceDto>

    @POST("rooms/{roomId}/devices/{deviceId}")
    suspend fun addDeviceToRoom(
        @Path("roomId") roomId: String,
        @Path("deviceId") deviceId: String
    ): Response<Unit>

    @DELETE("rooms/devices/{deviceId}")
    suspend fun removeDeviceFromRoom(@Path("deviceId") deviceId: String): Response<Unit>

    // Devices
    @POST("devices")
    suspend fun createDevice(@Body body: DeviceRequest): DeviceDto

    @PUT("devices/{id}")
    suspend fun updateDevice(@Path("id") id: String, @Body body: UpdateDeviceRequest): DeviceDto

    @DELETE("devices/{id}")
    suspend fun deleteDevice(@Path("id") id: String): Response<Unit>

    @GET("devices/{id}")
    suspend fun getDevice(@Path("id") id: String): DeviceDto

    @GET("devicetypes")
    suspend fun getDeviceTypes(): List<DeviceTypeDto>

    @PATCH("devices/{deviceId}/{action}")
    suspend fun executeAction(
        @Path("deviceId") deviceId: String,
        @Path("action") action: String,
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Map<String, Any?>

    @GET("devices/logs/limit/{limit}/offset/{offset}")
    suspend fun getDeviceLogs(
        @Path("limit") limit: Int,
        @Path("offset") offset: Int
    ): List<LogEntryDto>

    @GET("devices/{deviceId}/logs/limit/{limit}/offset/{offset}")
    suspend fun getDeviceLogsById(
        @Path("deviceId") deviceId: String,
        @Path("limit") limit: Int,
        @Path("offset") offset: Int
    ): List<LogEntryDto>

    // Routines
    @GET("routines")
    suspend fun getRoutines(): List<RoutineDto>

    @POST("routines")
    suspend fun createRoutine(@Body body: RoutineRequest): RoutineDto

    @PUT("routines/{id}")
    suspend fun updateRoutine(@Path("id") id: String, @Body body: RoutineRequest): RoutineDto

    @DELETE("routines/{id}")
    suspend fun deleteRoutine(@Path("id") id: String): Response<Unit>

    @PATCH("routines/{id}/execute")
    suspend fun executeRoutine(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>
}
