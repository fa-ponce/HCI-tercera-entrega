package com.example.smarthome.data.repository

import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*
import com.example.smarthome.data.datastore.UserPreferences
import com.google.gson.Gson
import retrofit2.HttpException

class AccountNotVerifiedException : Exception("Cuenta no verificada")

private data class ApiErrorWrapper(val error: ApiErrorDetail?)
private data class ApiErrorDetail(val code: Int?, val description: String?)

private fun retrofit2.Response<*>.parseError(): Exception {
    return try {
        val body = errorBody()?.string() ?: return Exception("Error ${code()}")
        Gson().fromJson(body, ApiErrorWrapper::class.java)?.error?.description
            ?.let { Exception(it) } ?: Exception("Error ${code()}")
    } catch (_: Exception) {
        Exception("Error ${code()}")
    }
}

private fun Throwable.apiDescription(): String? {
    if (this !is HttpException) return null
    return try {
        val body = response()?.errorBody()?.string() ?: return null
        Gson().fromJson(body, ApiErrorWrapper::class.java)?.error?.description
    } catch (_: Exception) { null }
}

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
                Result.failure(Exception(description ?: e.message ?: "Error al iniciar sesión"))
            }
        }
    }

    suspend fun logout() = runCatching {
        api.logout()
        userPreferences.clear()
    }

    suspend fun register(name: String, email: String, password: String): Result<UserDto> {
        return try {
            Result.success(api.register(RegisterRequest(name, email, password)))
        } catch (e: Exception) {
            val description = e.apiDescription()
            Result.failure(Exception(description ?: e.message ?: "Error al registrarse"))
        }
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
        val r = api.forgotPassword(EmailRequest(email))
        if (!r.isSuccessful) throw r.parseError()
    }

    suspend fun resetPassword(code: String, password: String): Result<Unit> = runCatching {
        val r = api.resetPassword(ResetPasswordRequest(code, password))
        if (!r.isSuccessful) throw r.parseError()
    }

    suspend fun changePassword(email: String, old: String, new: String): Result<Unit> = runCatching {
        api.changePassword(ChangePasswordRequest(email, old, new))
        Unit
    }
}
