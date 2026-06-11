package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*
import com.example.smarthome.data.datastore.UserPreferences

class AccountNotVerifiedException : Exception("Cuenta no verificada")

class AuthRepository(
    private val api: SmarthomeApi,
    private val userPreferences: UserPreferences
) {
    suspend fun login(email: String, password: String): Result<UserDto> {
        return try {
            val response = api.login(LoginRequest(email, password))
            userPreferences.saveToken(response.token)
            userPreferences.saveUser(response.user)
            Result.success(response.user)
        } catch (e: Exception) {
            val description = e.apiDescription()
            if (description != null && description.contains("verif", ignoreCase = true)) {
                Result.failure(AccountNotVerifiedException())
            } else {
                Result.failure(Exception(e.toFriendlyMessage("Error al iniciar sesión")))
            }
        }
    }

    suspend fun logout() = runCatching {
        api.logout().requireSuccessful("Error al cerrar sesión")
        userPreferences.clear()
    }

    suspend fun register(name: String, email: String, password: String): Result<UserDto> {
        return try {
            Result.success(api.register(RegisterRequest(name, email, password)))
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage("Error al registrarse")))
        }
    }

    suspend fun sendVerification(email: String): Result<Unit> = runCatching {
        api.sendVerification(EmailRequest(email)).requireSuccessful("Error al enviar el código")
    }

    suspend fun verifyAccount(code: String): Result<Unit> = runCatching {
        api.verifyAccount(VerifyCodeRequest(code)).requireSuccessful("Código inválido")
    }

    suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        api.forgotPassword(EmailRequest(email)).requireSuccessful("Error al enviar el código")
    }

    suspend fun resetPassword(code: String, password: String): Result<Unit> = runCatching {
        api.resetPassword(ResetPasswordRequest(code, password)).requireSuccessful("Error al cambiar la contraseña")
    }

    suspend fun changePassword(email: String, old: String, new: String): Result<Unit> = runCatching {
        api.changePassword(ChangePasswordRequest(email, old, new)).requireSuccessful("Error al cambiar la contraseña")
    }
}
