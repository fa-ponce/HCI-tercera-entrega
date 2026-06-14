package com.example.smarthome

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import java.util.Locale

/**
 * Permite resolver string resources (R.string.*) desde capas que no tienen un
 * Context propio, como los repositorios o el manejo de errores de la API.
 *
 * Usa el locale efectivo actual (Locale.getDefault(), que MainActivity mantiene
 * sincronizado con el idioma del teléfono o con el elegido a mano en Perfil),
 * de modo que los mensajes de error salen en el mismo idioma que el resto de la
 * app: inglés si la app está en inglés, español si está en español.
 */
object AppStrings {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(@StringRes resId: Int): String = localizedContext().getString(resId)

    fun get(@StringRes resId: Int, vararg formatArgs: Any): String =
        localizedContext().getString(resId, *formatArgs)

    private fun localizedContext(): Context {
        val config = Configuration(appContext.resources.configuration).apply {
            setLocale(Locale.getDefault())
        }
        return appContext.createConfigurationContext(config)
    }
}
