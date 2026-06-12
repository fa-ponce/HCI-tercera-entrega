# Notificaciones locales — RF20

Canal: `smarthome_main`

## Eventos que disparan notificaciones

### 1. Rutina ejecutada exitosamente
- **Disparado por:** `AppViewModel.executeRoutine(routineId)`
- **Título:** "Rutina ejecutada"
- **Cuerpo:** nombre de la rutina

### 2. Error al ejecutar rutina
- **Disparado por:** `AppViewModel.executeRoutine(routineId)` — rama de error
- **Título:** "Error en rutina"
- **Cuerpo:** "No se pudo ejecutar {nombre}"

### 3. Error al controlar dispositivo
- **Disparado por:** `AppViewModel.toggleDevice(device)` — solo cuando el API falla y se hace rollback
- **Título:** "Error de control"
- **Cuerpo:** "No se pudo controlar {nombre del dispositivo}"
- Toggles exitosos no generan notificación.

### 4. Cambio de estado remoto — alarma o puerta
- **Disparado por:** `AppViewModel.subscribeRealtime()` al recibir un `DeviceEvent` vía socket
- Solo aplica a dispositivos de tipo `ALARMA` o `PUERTA`
- Cubre cambios hechos por otro usuario o por automatización (no por el usuario actual)
- **Título:** "Alarma" o "Puerta"
- **Cuerpo:** "{nombre del dispositivo} — {Activado / Desactivado}"

## Infraestructura

| Componente | Responsabilidad |
|---|---|
| `NotificationHelper.kt` | Crea el canal y dispara notificaciones con `NotificationCompat.Builder` |
| `SmarthomeApp.onCreate()` | Inicializa el canal al arrancar la app |
| `MainActivity` | Solicita permiso `POST_NOTIFICATIONS` en Android 13+ (API 33+) |
| `AndroidManifest.xml` | Declara `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>` |
