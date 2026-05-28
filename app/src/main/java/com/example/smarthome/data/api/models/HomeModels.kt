package com.example.smarthome.data.api.models

data class HomeDto(
    val id: String,
    val name: String,
    val metadata: HomeMetadata? = null
)

data class HomeMetadata(
    val type: String? = null,
    val address: String? = null,
    val city: String? = null
)

data class HomeRequest(
    val name: String,
    val metadata: HomeMetadata
)
