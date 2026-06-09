package com.example.smarthome.data.repository

import com.google.gson.Gson
import retrofit2.HttpException
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

fun Throwable.toFriendlyMessage(fallback: String = "Ocurrió un error inesperado"): String {
    if (isNetworkError()) return "Sin conexión a internet. Verificá tu red."
    if (this is HttpException) {
        val description = apiDescription()
        if (description != null) return mapApiDescription(description)
        return when (code()) {
            401 -> "Sesión expirada. Ingresá de nuevo."
            403 -> "No tenés permiso para realizar esta acción."
            404 -> "El elemento no fue encontrado."
            409 -> "Ya existe un elemento con ese nombre."
            in 500..599 -> "Error del servidor. Intentá más tarde."
            else -> fallback
        }
    }
    return message?.takeIf { it.isNotBlank() } ?: fallback
}

private fun mapApiDescription(description: String): String {
    val d = description.lowercase()
    return when {
        ("name" in d || "nombre" in d) && ("short" in d || "min" in d || "least" in d) ->
            "El nombre debe tener al menos 3 caracteres."
        ("name" in d || "nombre" in d) && ("long" in d || "max" in d || "exceed" in d) ->
            "El nombre no puede superar los 100 caracteres."
        "already exist" in d || "duplicate" in d ->
            "Ya existe un elemento con ese nombre."
        "not found" in d ->
            "El elemento no fue encontrado."
        "unauthorized" in d || "invalid token" in d ->
            "Sesión expirada. Ingresá de nuevo."
        "permission" in d || "forbidden" in d ->
            "No tenés permiso para realizar esta acción."
        else -> description
    }
}

fun <T> Result<T>.mapError(fallback: String = "Ocurrió un error inesperado"): Result<T> =
    recoverCatching { throw Exception(it.toFriendlyMessage(fallback)) }
