package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*
import com.example.smarthome.data.datastore.UserPreferences

class AuthRepository(
    private val api: SmarthomeApi,
    private val userPreferences: UserPreferences
) {
    suspend fun login(email: String, password: String): Result<UserDto> = runCatching {
        val response = api.login(LoginRequest(email, password))
        userPreferences.saveToken(response.token)
        userPreferences.saveUser(response.user)
        response.user
    }

    suspend fun logout() = runCatching {
        api.logout()
        userPreferences.clear()
    }

    suspend fun register(name: String, email: String, password: String): Result<UserDto> = runCatching {
        api.register(RegisterRequest(name, email, password))
    }

    suspend fun sendVerification(email: String): Result<Unit> = runCatching {
        api.sendVerification(EmailRequest(email))
        Unit
    }

    suspend fun verifyAccount(code: String): Result<Unit> = runCatching {
        api.verifyAccount(VerifyCodeRequest(code))
        Unit
    }

    suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        api.forgotPassword(EmailRequest(email))
        Unit
    }

    suspend fun resetPassword(code: String, password: String): Result<Unit> = runCatching {
        api.resetPassword(ResetPasswordRequest(code, password))
        Unit
    }

    suspend fun changePassword(email: String, old: String, new: String): Result<Unit> = runCatching {
        api.changePassword(ChangePasswordRequest(email, old, new))
        Unit
    }
}
