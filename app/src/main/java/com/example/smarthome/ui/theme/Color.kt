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
val LightBorder2     = Color(0xFFCDCDC7) // --border-2
val LightBorderLight = Color(0xFFD8D8D3) // --border-light
val LightPrimary     = Color(0xFF3A5A90) // --primary
val LightPrimaryDk   = Color(0xFF2F4A7A) // --primary-dk
val LightPrimary2    = Color(0xFF4A6EA8) // --primary-2
val LightPrimaryBg   = Color(0xFFEEF2F9) // --primary-bg
val LightPrimaryBg2  = Color(0xFFEAEFF7) // --primary-bg2
val LightPrimaryBd   = Color(0xFFC8D2E4) // --primary-bd
val LightText        = Color(0xFF2A2A2A) // --text
val LightText2       = Color(0xFF424242) // --text-2
val LightText3       = Color(0xFF5E5E5E) // --text-3
val LightText4       = Color(0xFF707070) // --text-4
val LightMuted       = Color(0xFFA8A8A4) // --muted
val LightMuted2      = Color(0xFFB8B8B4) // --muted-2
val LightMuted3      = Color(0xFFC8C8C4) // --muted-3
val LightDangerBg    = Color(0xFFFAEEEC) // --danger-bg
val LightSuccessBg   = Color(0xFFECF6EF) // --success-bg
val LightWarningBg   = Color(0xFFFBF3E3) // --warning-bg
val LightInfoBg      = Color(0xFFF3F6FB) // --info-bg

// ── Tema oscuro ──
val DarkBg          = Color(0xFF0F0F0F) // --bg
val DarkSurface     = Color(0xFF1A1A1A) // --surface
val DarkSurface2    = Color(0xFF222222) // --surface-2
val DarkSurface3    = Color(0xFF2A2A2A) // --surface-3
val DarkBorder      = Color(0xFF333333) // --border
val DarkBorder2     = Color(0xFF3A3A3A) // --border-2
val DarkBorderLight = Color(0xFF2E2E2E) // --border-light
val DarkPrimary     = Color(0xFFF59E0B) // --primary (ámbar)
val DarkPrimaryDk   = Color(0xFFD97706) // --primary-dk
val DarkPrimary2    = Color(0xFFFBBF24) // --primary-2
val DarkPrimaryBg   = Color(0xFF1C1800) // --primary-bg
val DarkPrimaryBg2  = Color(0xFF221E00) // --primary-bg2
val DarkPrimaryBd   = Color(0xFF78500A) // --primary-bd
val DarkText        = Color(0xFFF0F0F0) // --text
val DarkText2       = Color(0xFFD4D4D4) // --text-2
val DarkText3       = Color(0xFFA8A8A8) // --text-3
val DarkText4       = Color(0xFF8A8A8A) // --text-4
val DarkMuted       = Color(0xFF666666) // --muted
val DarkMuted2      = Color(0xFF555555) // --muted-2
val DarkMuted3      = Color(0xFF444444) // --muted-3
val DarkDangerBg    = Color(0xFF2A1010) // --danger-bg
val DarkSuccessBg   = Color(0xFF0F2A1A) // --success-bg
val DarkWarningBg   = Color(0xFF2A1F0A) // --warning-bg
val DarkInfoBg      = Color(0xFF1A1A1A) // --info-bg

// ── Compartidos (idénticos en ambos temas de la web) ──
val SuccessGreen = Color(0xFF16A34A) // verde "encendido" / badges
val DangerRed    = Color(0xFFC0392B) // rojo "apagado" / acciones peligrosas

// Acentos de estadísticas / categorías (iguales a los íconos de la web).
val StatCyan   = Color(0xFF1FB6C1) // habitaciones
val StatWarm   = Color(0xFFC9A227) // consumo / energía
val HoraOrange = Color(0xFFE07A3C) // rutinas por hora
val EventoPink = Color(0xFFC0568A) // rutinas por evento
