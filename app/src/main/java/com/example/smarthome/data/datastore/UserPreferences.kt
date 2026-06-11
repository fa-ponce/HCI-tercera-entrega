package com.example.smarthome.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.smarthome.data.api.models.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smarthome_prefs")

class UserPreferences(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val COSTO_KWH_KEY = floatPreferencesKey("costo_kwh")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val SHORTCUTS_KEY = stringPreferencesKey("home_shortcuts")
    }

    val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }
    val userName: Flow<String?> = dataStore.data.map { it[USER_NAME_KEY] }
    val userEmail: Flow<String?> = dataStore.data.map { it[USER_EMAIL_KEY] }
    val costoKwh: Flow<Float?> = dataStore.data.map { it[COSTO_KWH_KEY] }
    val darkMode: Flow<Boolean> = dataStore.data.map { it[DARK_MODE_KEY] ?: false }

    // Accesos directos del inicio: lista ordenada de tokens ("r:<id>" rutina,
    // "d:<id>" dispositivo). null = el usuario nunca personalizó (se muestran
    // sugerencias por defecto); lista vacía = personalizó y dejó sin accesos.
    val shortcuts: Flow<List<String>?> = dataStore.data.map { prefs ->
        prefs[SHORTCUTS_KEY]?.let { raw ->
            if (raw.isEmpty()) emptyList() else raw.split("|")
        }
    }

    suspend fun getTokenOnce(): String? = dataStore.data.first()[TOKEN_KEY]

    suspend fun saveToken(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun saveUser(user: UserDto) {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = user.id
            prefs[USER_NAME_KEY] = user.name
            prefs[USER_EMAIL_KEY] = user.email
        }
    }

    suspend fun saveCostoKwh(costo: Float) {
        dataStore.edit { it[COSTO_KWH_KEY] = costo }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        dataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    suspend fun saveShortcuts(tokens: List<String>) {
        dataStore.edit { it[SHORTCUTS_KEY] = tokens.joinToString("|") }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
