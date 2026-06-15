package com.example.smarthome.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta tomada 1:1 de los tokens CSS de la web (HCI-segunda-entrega/src/style.css),
// para que la app móvil se vea exactamente igual que la web en ambos temas.

// ── Tema claro ──
val LightBg          = Color(0xFFF7F7F5) // --bg
val LightSurface     = Color(0xFFFDFDFB) // --surface (tarjetas, modales, navbar)
val LightSurface2    = Color(0xFFF4F4F1) // --surface-2
val LightSurface3    = Color(0xFFEEEEEA) // --surface-3
val LightBorder      = Color(0xFFC8C8C2) // --border
val LightBorderLight = Color(0xFFD8D8D3) // --border-light
val LightPrimary     = Color(0xFF3A5A90) // --primary
val LightPrimaryDk   = Color(0xFF2F4A7A) // --primary-dk
val LightPrimary2    = Color(0xFF4A6EA8) // --primary-2
val LightPrimaryBg   = Color(0xFFEEF2F9) // --primary-bg
val LightPrimaryBg2  = Color(0xFFEAEFF7) // --primary-bg2
val LightText        = Color(0xFF2A2A2A) // --text
val LightText3       = Color(0xFF5E5E5E) // --text-3
val LightDangerBg    = Color(0xFFFAEEEC) // --danger-bg
val LightSuccessBg   = Color(0xFFECF6EF) // --success-bg

// ── Tema oscuro ──
val DarkBg          = Color(0xFF0F0F0F) // --bg
val DarkSurface     = Color(0xFF1A1A1A) // --surface
val DarkSurface2    = Color(0xFF222222) // --surface-2
val DarkSurface3    = Color(0xFF2A2A2A) // --surface-3
val DarkBorder      = Color(0xFF333333) // --border
val DarkBorderLight = Color(0xFF2E2E2E) // --border-light
val DarkPrimary     = Color(0xFFF59E0B) // --primary (ámbar)
val DarkPrimary2    = Color(0xFFFBBF24) // --primary-2
val DarkPrimaryBg   = Color(0xFF1C1800) // --primary-bg
val DarkPrimaryBg2  = Color(0xFF221E00) // --primary-bg2
val DarkText        = Color(0xFFF0F0F0) // --text
val DarkText3       = Color(0xFFA8A8A8) // --text-3
val DarkDangerBg    = Color(0xFF2A1010) // --danger-bg
val DarkSuccessBg   = Color(0xFF0F2A1A) // --success-bg

// ── Compartidos (idénticos en ambos temas de la web) ──
val SuccessGreen = Color(0xFF16A34A) // verde "encendido" / badges
val DangerRed    = Color(0xFFC0392B) // rojo "apagado" / acciones peligrosas

// Acentos de estadísticas / categorías (iguales a los íconos de la web).
val StatCyan   = Color(0xFF1FB6C1) // habitaciones
val StatWarm   = Color(0xFFC9A227) // consumo / energía
val HoraOrange = Color(0xFFE07A3C) // rutinas por hora
val EventoPink = Color(0xFFC0568A) // rutinas por evento
