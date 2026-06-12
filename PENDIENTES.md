# Pendientes Android — Basado en devolución Web (Segunda Entrega)

## Estado por punto de la devolución

---

### 5.1 Requerimientos Funcionales

| Requisito | Estado | Detalle |
|---|---|---|
| Registrar cuenta | ✅ CUMPLE | `register()` no llama a `/send-verification` de más |
| Verificar cuenta | ❌ NO CUMPLE | Ver bug #1 |
| Recuperar contraseña | ❌ NO CUMPLE | Ver bug #2 |
| Cambiar contraseña | ✅ CUMPLE | `ChangePasswordDialog` en `ProfileScreen` |
| Iniciar sesión | ✅ CUMPLE | |
| Cerrar sesión | ⚠️ PARCIAL | Falta confirmación — ver usabilidad #1 |
| Gestionar dispositivos | ❌ NO CUMPLE | Ver bug #3 |
| Consultar dispositivos | ✅ CUMPLE | |
| Controlar dispositivos | ❌ NO CUMPLE | Ver bug #4 (alarma sin código) |
| Gestionar rutinas | ❌ NO CUMPLE | Ver bug #5 |
| Consultar rutinas | ✅ CUMPLE | |
| Ejecutar rutinas | ✅ CUMPLE | |
| Consultar acciones realizadas | ✅ CUMPLE | |
| Gestionar habitaciones | ✅ CUMPLE | |
| Consultar habitaciones | ✅ CUMPLE | |
| Vincular dispositivos a habitaciones | ✅ CUMPLE | |
| Gestionar hogares | ✅ CUMPLE | |
| Consultar hogares | ✅ CUMPLE | |
| Vincular habitaciones a hogares | ✅ CUMPLE | |
| Consultar consumo eléctrico | ✅ CUMPLE | `ConsumptionScreen` |
| Planificar ejecución de rutinas | ✅ CUMPLE | Trigger por hora en `RoutineBuilderSheet` |

---

## Bugs a corregir

### Bug #1 — Verificar cuenta fuera del flujo de login
**Archivo:** `ui/screens/login/LoginViewModel.kt:50-56`

Al llamar a `login()`, si el API devuelve un error de "cuenta no verificada", el ViewModel solo muestra el mensaje de error. No redirige al usuario a `LoginMode.Verify` ni ofrece la opción de verificar su cuenta.

**Corrección:** En `login().onFailure`, detectar si el mensaje contiene "not verified" (o el código que devuelva la API) y automáticamente hacer `setMode(LoginMode.Verify)`.

---

### Bug #2 — Recuperar contraseña no tiene UI
**Archivos:** `ui/screens/login/LoginScreen.kt`, `ui/screens/login/LoginViewModel.kt`

`AuthRepository` ya tiene implementados `forgotPassword(email)` y `resetPassword(code, password)`, pero **no hay UI que los invoque**. No existe botón de "¿Olvidaste tu contraseña?" en `LoginScreen`.

**Corrección:**
1. Agregar `LoginMode.ForgotPassword` y `LoginMode.ResetPassword` al sealed class.
2. Agregar funciones `forgotPassword()` y `resetPassword()` en `LoginViewModel`.
3. En `LoginScreen`, en el modo `Login`, agregar `TextButton("¿Olvidaste tu contraseña?")` que cambie al modo `ForgotPassword`.
4. Mostrar formulario de código + nueva contraseña en modo `ResetPassword`.

---

### Bug #3 — Gestionar dispositivos sin validación de longitud
**Archivo:** `ui/screens/devices/AddDeviceDialog.kt:216`

El `TextButton` "Crear" solo verifica `name.isNotBlank()` y `selectedType != null`. Si el nombre tiene 1-2 caracteres o más de 100, se llama al API, este devuelve un error, pero el usuario no recibe un mensaje claro antes del intento.

**Corrección:** En `AddDeviceDialog`, antes de llamar a `onCreate`, validar:
- `name.trim().length < 3` → mostrar error "El nombre debe tener al menos 3 caracteres"
- `name.trim().length > 100` → mostrar error "El nombre no puede superar 100 caracteres"

También aplicar la misma validación en `NewHomeDialog` (`HomesScreen`) y `AddRoomDialog` (`HomeDetailScreen`).

---

### Bug #4 — Toggle de alarma no pide código de seguridad
**Archivos:** `domain/DeviceUtils.kt:37`, `ui/components/DeviceCard.kt:95-97`, `ui/AppViewModel.kt:148-156`

`canToggle(ALARMA)` retorna `true`, por lo que el `DeviceCard` muestra un `Switch` para la alarma. Al togglarlo, se llama `appViewModel.toggleDevice()` directamente con `armAway` o `disarm` sin solicitar el código de seguridad. Esto es un bypass de seguridad.

**Corrección:** Cambiar `canToggle` para que la alarma devuelva `false`, o en `DevicesScreen` al recibir el `onToggle` de un dispositivo de tipo `ALARMA`, abrir el `AlarmaSheet` en vez de llamar a `toggleDevice()`.

---

### Bug #5 — Rutinas no incluyen dispositivos libres ni en habitaciones sin casa
**Archivo:** `ui/screens/routines/RoutineBuilderSheet.kt:102-109`

`allDevices` en `RoutineBuilderSheet` solo itera sobre `homes → rooms[home.id] → devices[room.id]`. Los dispositivos en `devices["free"]` (sin habitación) y los dispositivos en habitaciones no asignadas a una casa quedan **completamente excluidos** de la lista de dispositivos disponibles para rutinas.

**Corrección:** Extender la construcción de `allDevices` para incluir:
1. Dispositivos en `devices["free"]` con `homeName = ""` y `roomName = "Sin habitación"`.
2. Opcionalmente, cargar en `AppViewModel.loadAll()` los dispositivos libres (sin habitación) consultando la API de dispositivos generales.

---

## Mejoras de usabilidad

### Usabilidad #1 — Confirmación al cerrar sesión
**Archivo:** `ui/screens/profile/ProfileScreen.kt:276-288`

El botón "Cerrar sesión" ejecuta logout y navega inmediatamente sin pedir confirmación. Agregar un `AlertDialog` de confirmación antes de llamar a `appViewModel.logout()`.

---

### Usabilidad #2 — Eliminar adjetivos posesivos
**Archivos:** `ui/screens/homes/HomesScreen.kt:105`, `ui/screens/profile/ProfileScreen.kt:133`

- "Mis Casas" → "Casas"
- "Mi Perfil" → "Perfil"

---

### Usabilidad #3 — Eliminar abreviaturas
**Archivos:** `HomesScreen.kt:213`, `HomeDetailScreen.kt`, varios

- `"hab."` → `"habitaciones"` o `"hab"` al menos sin punto
- `"disp."` → `"dispositivos"`
- `"enc."` → `"encendidos"`

---

### Usabilidad #4 — Indicar campos requeridos en formularios
**Archivos:** `AddDeviceDialog.kt`, `HomesScreen.kt` (NewHomeDialog), `HomeDetailScreen.kt` (AddRoomDialog), `RoutineBuilderSheet.kt`

Ningún formulario indica visualmente qué campos son obligatorios. Agregar asterisco (*) en el `label` de los campos requeridos, por ejemplo `label = { Text("Nombre *") }`.

---

### Usabilidad #5 — Consistencia en capitalización de leyendas
Revisar todas las pantallas y estandarizar: solo la primera letra de la primera palabra en mayúscula (sentence case) para labels, títulos de secciones y placeholders. Ejemplos detectados:
- `HomeScreen.kt`: "Estado de dispositivos" ✅ vs "Rutinas rápidas" ✅ — consistentes entre sí, revisar el resto.
- `DevicesScreen.kt`: "Nuevo Dispositivo" → "Nuevo dispositivo"
- `HomesScreen.kt`: "Nueva Casa" → "Nueva casa"
- `HomeDetailScreen.kt`: "Nueva habitación" ✅

---

### Usabilidad #6 — Manejo de falta de conectividad con el API
**Archivo:** `ui/AppViewModel.kt:96-132`

`loadAll()` captura excepciones genéricas y pone `_error.value = e.message`. No hay detección de si el error es de red (sin conexión) versus error de servidor. En Android se puede usar `ConnectivityManager` o simplemente detectar `java.net.UnknownHostException` / `java.net.ConnectException` para mostrar un mensaje diferenciado como "Sin conexión a internet".

**Corrección mínima:** En el `catch`, distinguir entre `IOException`/`ConnectException` y otros errores para mostrar "Sin conexión. Verificá tu red." en lugar del error técnico del API.

---

### Usabilidad #7 — Validación temprana en formularios (sin llamar al API)
Aplicar validaciones client-side antes de llamar al API en todos los formularios:

| Formulario | Campo | Validación faltante |
|---|---|---|
| NewHomeDialog | Nombre | Mínimo 3 caracteres |
| AddDeviceDialog | Nombre | Mínimo 3, máximo 100 caracteres |
| AddRoomDialog | Nombre | Mínimo 3 caracteres |
| RoutineBuilderSheet | Nombre | Ya valida (ok) |

---

### Usabilidad #8 — Feedback de progreso en operaciones
**Archivos:** `HomeDetailScreen.kt` (renombrar/eliminar casa), `RoomScreen.kt`, otros

Varias operaciones (renombrar casa, crear habitación, vincular dispositivo) no muestran indicador de carga mientras esperan la respuesta del API. El botón confirmar debería desactivarse y mostrar un `CircularProgressIndicator` durante la operación.

---

### Usabilidad #9 — Mensajes de error informativos
**Archivos:** `AppViewModel.kt:179`, varios

Muchos errores muestran `it.message` que puede ser un mensaje técnico del API en inglés o un JSON. Mapear al menos los errores más comunes a mensajes en español:
- Error de nombre demasiado corto → "El nombre debe tener al menos 3 caracteres"
- Error de nombre demasiado largo → "El nombre no puede superar 100 caracteres"
- Error de autenticación → "Email o contraseña incorrectos"

---

## No funcionales

### No funcional #1 — Colores hardcodeados
**Archivos:** `ProfileScreen.kt:73`, `DevicesScreen.kt`, `HomesScreen.kt`, `HomeDetailScreen.kt`

`Color(0xFF3A5A90)` está definido localmente en múltiples archivos en lugar de usar el color del tema (`MaterialTheme.colorScheme.primary`). Centralizar en el `Theme.kt` o usar `MaterialTheme.colorScheme.primary` directamente.

---

## Resumen de prioridades

| Prioridad | Tarea |
|---|---|
| 🔴 Alta | Bug #4 — Toggle alarma sin código de seguridad |
| 🔴 Alta | Bug #2 — Recuperar contraseña sin UI |
| 🔴 Alta | Bug #1 — Verificar cuenta al intentar logueo fallido |
| 🔴 Alta | Bug #5 — Rutinas sin dispositivos libres |
| 🟡 Media | Bug #3 — Validación de longitud de nombres |
| 🟡 Media | Usabilidad #1 — Confirmación al cerrar sesión |
| 🟡 Media | Usabilidad #4 — Indicar campos requeridos |
| 🟡 Media | Usabilidad #6 — Manejo de conectividad |
| 🟡 Media | Usabilidad #7 — Validación temprana en formularios |
| 🟢 Baja | Usabilidad #2 — Eliminar "Mis/Mi" |
| 🟢 Baja | Usabilidad #3 — Eliminar abreviaturas |
| 🟢 Baja | Usabilidad #5 — Consistencia capitalización |
| 🟢 Baja | Usabilidad #8 — Feedback de progreso |
| 🟢 Baja | Usabilidad #9 — Mensajes de error informativos |
| 🟢 Baja | No funcional #1 — Colores hardcodeados |
