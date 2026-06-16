# Smarthome — App móvil (HCI · Tercera entrega)

App Android nativa para controlar un hogar inteligente (casas, habitaciones, dispositivos, rutinas y consumo). Es la versión móvil de la app web de la materia y consume el mismo backend de HCI (ITBA).

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM
- **Red:** Retrofit + OkHttp + Socket.IO (tiempo real)
- **Persistencia local:** DataStore
- **Paquete:** `com.example.smarthome`

---

## Entorno de pruebas

| Parámetro | Valor |
|---|---|
| Tipo de dispositivo | Emulador (dispositivo virtual) |
| Modelo emulado | Google Pixel 4a |
| API Level utilizado | 37.0 (Android 16 Developer Preview) |
| Arquitectura | x86_64 |
| API Level mínimo soportado | **29 (Android 10)** |
| API Level objetivo | 36 (Android 16) |

> La app funciona en cualquier dispositivo físico o emulador con **Android 10 (API 29) o superior**.

---

## Instalación desde APK (dispositivo físico)

### Ubicación del APK

El archivo APK se encuentra dentro del ZIP entregado en la siguiente ruta:

```
HCI-tercera-entrega/app/build/outputs/apk/debug/app-debug.apk
```

### Paso 1 — Transferir el APK al dispositivo

**Opción A — Cable USB**
1. Conectá el teléfono a la computadora con el cable USB.
2. Deslizá la barra de notificaciones y tocá la notificación de USB.
3. Seleccioná **"Transferir archivos"** (MTP).
4. Copiá `app-debug.apk` a cualquier carpeta del teléfono (por ejemplo, `Descargas`).

**Opción B — Google Drive / descarga directa**
1. Subí el APK a Google Drive u otro servicio de nube.
2. Desde el teléfono, abrí el enlace y descargalo.

### Paso 2 — Habilitar instalación desde fuentes desconocidas

1. Abrí **Ajustes → Aplicaciones → Instalar apps desconocidas** (o **Permisos especiales de apps**).
2. Seleccioná la app desde la que vas a instalar el APK (explorador de archivos o navegador).
3. Activá **"Permitir desde esta fuente"**.

### Paso 3 — Instalar el APK

1. Abrí el explorador de archivos del teléfono.
2. Navegá hasta donde guardaste `app-debug.apk` y tocalo.
3. Confirmá la instalación → **"Instalar"**.
4. Tocá **"Abrir"** para lanzar la app o **"Listo"** para abrirla después desde el cajón de apps.

### Primer uso

Al abrir la app por primera vez, Android puede pedir permiso para enviar notificaciones. Aceptarlo es opcional pero recomendado: la app lo usa para alertas de rutinas y eventos de alarma/puerta.

La app se conecta a `https://hci.it.itba.edu.ar` — **requiere internet** para funcionar.

---

## Desarrollo: requisitos

- Android Studio (o JDK + Android SDK por línea de comandos)
- Un emulador o dispositivo Android con **Android 10 (API 29) o superior**

---

## Configuración: archivo `.env` (API key)

La API key **no está en el repositorio** (el `.gitignore` ignora `*.env`). Antes de compilar, creá un archivo `.env` en la raíz del proyecto:

```env
API_KEY=tu_api_key_aca
```

Gradle lee ese `.env`, lo inyecta como `BuildConfig.API_KEY` y la app lo manda en el header `X-API-KEY` de cada request. La URL del backend ya está fija en el código (`https://hci.it.itba.edu.ar/api/`).

---

## Compilar y correr

Desde la carpeta del proyecto:

```bash
# Compilar + instalar en el emulador/dispositivo conectado
./gradlew installDebug

# Abrir la app
adb shell monkey -p com.example.smarthome -c android.intent.category.LAUNCHER 1
```

| Acción | Comando |
|---|---|
| Solo compilar | `./gradlew assembleDebug` |
| Compilar + instalar | `./gradlew installDebug` |
| Recompilar desde cero | `./gradlew clean installDebug` |
| Ver logs en vivo | `adb logcat \| grep -i smarthome` |

> O más simple: abrí la carpeta en **Android Studio**, elegí el emulador y tocá **▶ Run**.

---

## Estructura del proyecto

```
Final/
├─ .env                     # API key (NO se sube a git)
├─ app/build.gradle.kts     # dependencias + inyecta API_KEY a BuildConfig
└─ app/src/main/java/com/example/smarthome/
   ├─ SmarthomeApp.kt        # Application; inicializa el ServiceLocator
   ├─ MainActivity.kt        # Entrada; abre Login o Home según haya token guardado
   ├─ ServiceLocator.kt      # Inyección de dependencias manual (singletons)
   │
   ├─ data/                  # CAPA DE DATOS
   │  ├─ api/
   │  │  ├─ ApiClient.kt     # Retrofit + OkHttp + interceptores (X-API-KEY y Bearer token)
   │  │  ├─ SmarthomeApi.kt  # Endpoints (auth, homes, rooms, devices, routines)
   │  │  └─ models/          # DTOs y requests/responses
   │  ├─ repository/         # Lógica por dominio (Auth, Home, Device, Routine, History)
   │  │  └─ ApiError.kt      # Traduce errores del backend a mensajes amigables
   │  └─ datastore/
   │     └─ UserPreferences.kt  # Guarda token, usuario, costo kWh y modo oscuro
   │
   ├─ domain/
   │  └─ DeviceUtils.kt      # Tipos de dispositivo, estado on/off, acción de toggle
   │
   ├─ socket/
   │  └─ SocketManager.kt    # Socket.IO: cambios de estado en tiempo real
   │
   └─ ui/                    # CAPA DE PRESENTACIÓN
      ├─ AppViewModel.kt     # ViewModel central: homes/rooms/devices/routines + acciones
      ├─ navigation/NavGraph.kt  # Rutas + barra inferior
      ├─ theme/              # Colores, tipografía, tema claro/oscuro
      ├─ components/         # DeviceCard + sheets de control por tipo de dispositivo
      └─ screens/            # Pantallas: login, home, homes, room, devices,
                             #            routines, consumption, history, profile
```

### Qué hace cada parte (en corto)

- **Arranque:** `MainActivity` mira si hay token guardado. Si hay → carga todo y va a **Home**; si no → va a **Login**.
- **Capa de datos:** Retrofit habla con la API. Dos interceptores agregan automáticamente la `X-API-KEY` (app) y el `Bearer token` (usuario logueado). Los repositorios envuelven las llamadas en `Result` y traducen errores.
- **AppViewModel:** mantiene en memoria casas, habitaciones, dispositivos y rutinas; hace cargas en paralelo, toggles optimistas (cambia la UI al instante y revierte si falla) y recibe updates en vivo por Socket.IO.
- **Pantallas:** Inicio (resumen), Casas/Habitaciones, Dispositivos (con sheets de control por tipo: AC, horno, lámpara, persiana, etc.), Rutinas, Consumo, Historial y Perfil.
- **Persistencia local:** `UserPreferences` (DataStore) guarda token, datos del usuario, costo del kWh y preferencia de modo oscuro.

---

## Flujo de autenticación

- **Registro:** `/register` ya envía el correo con el código de verificación, así que la app pasa directo a la pantalla de **Verificar** (no reenvía el código).
- **Login con cuenta no verificada:** la app encamina al usuario a la pantalla de **Verificar**, donde puede ingresar el código o tocar **"Reenviar código"**.

## Correo de prueba (verificación de cuenta)

El registro requiere verificación por email. Para ver los códigos de confirmación, ingresar al siguiente buzón de prueba:

- **URL:** https://ethereal.email/messages
- **Nombre:** Rhoda Bahringer
- **Usuario:** rhoda14@ethereal.email
- **Contraseña:** F1rPSZEJKyAcY61v9W

> Esta cuenta es solo de salida (no recibe emails externos). Los mensajes enviados por la app aparecen en el link de arriba.

---
