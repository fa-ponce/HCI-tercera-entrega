package com.example.smarthome.data.api.models

data class RoomDto(
    val id: String,
    val name: String,
    val home: HomeRef? = null,
    val metadata: RoomMetadata? = null
)

data class HomeRef(val id: String)

data class RoomMetadata(val type: String? = null)

data class RoomRequest(
    val name: String,
    val home: HomeRef? = null,
    val metadata: RoomMetadata? = null
)
