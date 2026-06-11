package com.example.smarthome.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Lenguaje visual compartido de la app: el mismo gradiente de marca
 * (primario → terciario) y las esquinas redondeadas del encabezado del inicio,
 * reutilizados en las barras superiores de todas las pantallas.
 */

/** Pincel de marca: gradiente horizontal primario → terciario. */
@Composable
fun appBarBrush(): Brush = Brush.horizontalGradient(
    listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )
)

/** Aplica el gradiente de marca con esquinas inferiores redondeadas a una barra superior. */
@Composable
fun Modifier.appBarGradient(): Modifier = this
    .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
    .background(appBarBrush())

/** Colores transparentes (deja ver el gradiente) con contenido en onPrimary. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun gradientTopBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = Color.Transparent,
    scrolledContainerColor = Color.Transparent,
    titleContentColor = MaterialTheme.colorScheme.onPrimary,
    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
)
