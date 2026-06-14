package com.example.smarthome.data.repository

import com.example.smarthome.AppStrings
import com.example.smarthome.R
import com.example.smarthome.data.api.SmarthomeApi
import com.example.smarthome.data.api.models.*
import com.example.smarthome.data.datastore.UserPreferences
import retrofit2.HttpException

// Marcador interno: nunca se muestra al usuario (LoginViewModel lo intercepta por tipo).
class AccountNotVerifiedException : Exception("Account not verified")

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
            when {
                description != null && description.contains("verif", ignoreCase = true) ->
                    Result.failure(AccountNotVerifiedException())
                e is HttpException && e.code() == 401 ->
                    Result.failure(Exception(AppStrings.get(R.string.err_invalid_credentials)))
                else ->
                    Result.failure(Exception(e.toFriendlyMessage(AppStrings.get(R.string.err_login))))
            }
        }
    }

    suspend fun logout() = runCatching {
        api.logout().requireSuccessful(AppStrings.get(R.string.err_logout))
        userPreferences.clear()
    }

    suspend fun register(name: String, email: String, password: String): Result<UserDto> {
        return try {
            Result.success(api.register(RegisterRequest(name, email, password)))
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage(AppStrings.get(R.string.err_register))))
        }
    }

    suspend fun sendVerification(email: String): Result<Unit> = runCatching {
        api.sendVerification(EmailRequest(email)).requireSuccessful(AppStrings.get(R.string.err_send_code))
    }

    suspend fun verifyAccount(code: String): Result<Unit> = runCatching {
        api.verifyAccount(VerifyCodeRequest(code)).requireSuccessful(AppStrings.get(R.string.err_code_invalid))
    }

    suspend fun forgotPassword(email: String): Result<Unit> = runCatching {
        api.forgotPassword(EmailRequest(email)).requireSuccessful(AppStrings.get(R.string.err_send_code))
    }

    suspend fun resetPassword(code: String, password: String): Result<Unit> = runCatching {
        api.resetPassword(ResetPasswordRequest(code, password)).requireSuccessful(AppStrings.get(R.string.err_change_password))
    }

    suspend fun changePassword(email: String, old: String, new: String): Result<Unit> = runCatching {
        api.changePassword(ChangePasswordRequest(email, old, new)).requireSuccessful(AppStrings.get(R.string.err_change_password))
    }
}
