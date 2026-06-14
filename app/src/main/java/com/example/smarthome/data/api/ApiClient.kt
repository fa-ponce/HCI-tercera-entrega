package com.example.smarthome.data.api

import com.example.smarthome.BuildConfig
import com.example.smarthome.data.api.models.LogDeviceDto
import com.example.smarthome.data.api.models.LogRoomRef
import com.example.smarthome.data.datastore.UserPreferences
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://hci.it.itba.edu.ar/api/"
private const val NETWORK_TIMEOUT_SECONDS = 15L

private val API_KEY get() = BuildConfig.API_KEY

class ApiClient(private val userPreferences: UserPreferences) {

    private val apiKeyInterceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .addHeader("X-API-KEY", API_KEY)
                .addHeader("Content-Type", "application/json")
                .build()
        )
    }

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { userPreferences.getTokenOnce() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
        redactHeader("Authorization")
        redactHeader("X-API-KEY")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(apiKeyInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    // En los logs, `device` puede llegar como objeto anidado o como id suelto.
    // Este adapter acepta ambas formas (como hace extractDeviceId en la web) para
    // que el deviceId siempre quede disponible y no rompa el parseo.
    private val gson = GsonBuilder()
        .registerTypeAdapter(
            LogDeviceDto::class.java,
            JsonDeserializer<LogDeviceDto> { json, _, context ->
                when {
                    json.isJsonObject -> {
                        val o = json.asJsonObject
                        LogDeviceDto(
                            id = o.get("id")?.takeUnless { it.isJsonNull }?.asString,
                            name = o.get("name")?.takeUnless { it.isJsonNull }?.asString,
                            room = o.get("room")?.takeIf { it.isJsonObject }
                                ?.let { context.deserialize<LogRoomRef>(it, LogRoomRef::class.java) }
                        )
                    }
                    json.isJsonPrimitive -> LogDeviceDto(id = json.asString)
                    else -> LogDeviceDto()
                }
            }
        )
        .create()

    val api: SmarthomeApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(SmarthomeApi::class.java)
}
