package com.example.smarthome.data.repository

import com.example.smarthome.AppStrings
import com.example.smarthome.R
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

internal data class ApiErrorWrapper(val error: ApiErrorDetail?)
internal data class ApiErrorDetail(val code: Int?, val description: String?)

fun Throwable.isNetworkError(): Boolean =
    this is UnknownHostException || this is ConnectException || this is SocketTimeoutException

fun Throwable.apiDescription(): String? {
    if (this !is HttpException) return null
    return try {
        val body = response()?.errorBody()?.string() ?: return null
        Gson().fromJson(body, ApiErrorWrapper::class.java)?.error?.description
    } catch (_: Exception) { null }
}

fun Throwable.toFriendlyMessage(fallback: String = AppStrings.get(R.string.err_unexpected)): String {
    if (isNetworkError()) return AppStrings.get(R.string.err_no_internet)
    if (this is HttpException) {
        val description = apiDescription()
        if (description != null) return mapApiDescription(description)
        return when (code()) {
            401 -> AppStrings.get(R.string.err_session_expired)
            403 -> AppStrings.get(R.string.err_no_permission)
            404 -> AppStrings.get(R.string.err_not_found)
            409 -> AppStrings.get(R.string.err_already_exists)
            in 500..599 -> AppStrings.get(R.string.err_server)
            else -> fallback
        }
    }
    return message?.takeIf { it.isNotBlank() } ?: fallback
}

fun Response<*>.toApiException(fallback: String = AppStrings.get(R.string.err_unexpected)): Exception {
    return try {
        val body = errorBody()?.string()
        val description = body
            ?.let { Gson().fromJson(it, ApiErrorWrapper::class.java)?.error?.description }
        Exception(description?.let(::mapApiDescription) ?: AppStrings.get(R.string.err_code, code()))
    } catch (_: Exception) {
        Exception(if (code() > 0) AppStrings.get(R.string.err_code, code()) else fallback)
    }
}

fun Response<*>.requireSuccessful(fallback: String = AppStrings.get(R.string.err_unexpected)) {
    if (!isSuccessful) throw toApiException(fallback)
}

private fun mapApiDescription(description: String): String {
    val d = description.lowercase()
    return when {
        ("name" in d || "nombre" in d) && ("short" in d || "min" in d || "least" in d) ->
            AppStrings.get(R.string.common_name_min_chars)
        ("name" in d || "nombre" in d) && ("long" in d || "max" in d || "exceed" in d) ->
            AppStrings.get(R.string.common_name_max_chars)
        "already exist" in d || "duplicate" in d ->
            AppStrings.get(R.string.err_already_exists)
        "not found" in d ->
            AppStrings.get(R.string.err_not_found)
        "unauthorized" in d || "invalid token" in d ->
            AppStrings.get(R.string.err_session_expired)
        "permission" in d || "forbidden" in d ->
            AppStrings.get(R.string.err_no_permission)
        else -> description
    }
}

fun <T> Result<T>.mapError(fallback: String = AppStrings.get(R.string.err_unexpected)): Result<T> =
    recoverCatching { throw Exception(it.toFriendlyMessage(fallback)) }
