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
        private val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
    }

    val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }
    val userName: Flow<String?> = dataStore.data.map { it[USER_NAME_KEY] }
    val userEmail: Flow<String?> = dataStore.data.map { it[USER_EMAIL_KEY] }
    val costoKwh: Flow<Float?> = dataStore.data.map { it[COSTO_KWH_KEY] }
    val darkMode: Flow<Boolean> = dataStore.data.map { it[DARK_MODE_KEY] ?: false }

    // Idioma de la app: "es" / "en", o null = idioma del teléfono (RNF1).
    val appLanguage: Flow<String?> = dataStore.data.map { it[APP_LANGUAGE_KEY] }

    // Accesos directos del inicio: lista ordenada de tokens ("r:<id>" rutina,
    // "d:<id>" dispositivo). null = el usuario nunca personalizó (se muestran
    // sugerencias por defecto); lista vacía = personalizó y dejó sin accesos.
    val shortcuts: Flow<List<String>?> = dataStore.data.map { prefs ->
        prefs[SHORTCUTS_KEY]?.let { raw ->
            if (raw.isEmpty()) emptyList() else raw.split("|")
        }
    }

    suspend fun getTokenOnce(): String? = dataStore.data.first()[TOKEN_KEY]

    suspend fun getAppLanguageOnce(): String? = dataStore.data.first()[APP_LANGUAGE_KEY]

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

    suspend fun saveAppLanguage(language: String?) {
        dataStore.edit { prefs ->
            if (language == null) prefs.remove(APP_LANGUAGE_KEY)
            else prefs[APP_LANGUAGE_KEY] = language
        }
    }

    suspend fun getVacuumPrefs(deviceId: String): Pair<String?, String?> {
        val prefs = dataStore.data.first()
        val loc = prefs[stringPreferencesKey("vacuum_loc_$deviceId")]
        val dock = prefs[stringPreferencesKey("vacuum_dock_$deviceId")]
        return Pair(loc, dock)
    }

    suspend fun saveVacuumPrefs(deviceId: String, locationId: String?, dockingId: String?) {
        dataStore.edit { prefs ->
            val locKey = stringPreferencesKey("vacuum_loc_$deviceId")
            val dockKey = stringPreferencesKey("vacuum_dock_$deviceId")
            if (locationId != null) prefs[locKey] = locationId else prefs.remove(locKey)
            if (dockingId != null) prefs[dockKey] = dockingId else prefs.remove(dockKey)
        }
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            // El idioma elegido manualmente sobrevive al cierre de sesión.
            val language = prefs[APP_LANGUAGE_KEY]
            prefs.clear()
            language?.let { prefs[APP_LANGUAGE_KEY] = it }
        }
    }
}
