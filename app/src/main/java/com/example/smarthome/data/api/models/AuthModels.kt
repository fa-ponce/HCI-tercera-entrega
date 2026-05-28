package com.example.smarthome.data.api.models

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String, val user: UserDto)

data class RegisterRequest(val name: String, val email: String, val password: String)

data class VerifyCodeRequest(val code: String)

data class EmailRequest(val email: String)

data class ChangePasswordRequest(
    val email: String,
    val oldPassword: String,
    val newPassword: String
)

data class ResetPasswordRequest(val code: String, val password: String)

data class UserDto(val id: String, val name: String, val email: String)
