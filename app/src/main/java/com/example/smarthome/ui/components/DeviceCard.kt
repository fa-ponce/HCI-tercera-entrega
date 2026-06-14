package com.example.smarthome.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.domain.canToggle
import com.example.smarthome.domain.deviceIcon
import com.example.smarthome.domain.deviceTypeName
import com.example.smarthome.domain.isDeviceOn

/**
 * Recorta el nombre a un máximo de caracteres para que las tarjetas se vean
 * uniformes. Si el nombre supera el límite, muestra los primeros [max]
 * caracteres seguidos de "...".
 */
fun truncateName(name: String, max: Int = 18): String =
    if (name.length > max) name.take(max).trimEnd() + "..." else name

private data class DevicePanelColors(
    val accent: Color,
    val accentContainer: Color,
    val subtitle: Color
)

/** Color naranja para señalar dispositivos sin habitación (igual que la web), adapted for dark surfaces. */
@Composable
private fun freeAccent(): Color = if (isDarkTheme()) Color(0xFFF59E0B) else Color(0xFFE67E22)

/** Verde para el indicador de estado "encendido", brightened slightly on dark surfaces. */
@Composable
private fun onAccent(): Color = if (isDarkTheme()) Color(0xFF22C55E) else Color(0xFF16A34A)

@Composable
private fun offAccent(): Color = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun isDarkTheme(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
private fun devicePanelColors(on: Boolean, free: Boolean): DevicePanelColors {
    val accent = when {
        free -> freeAccent()
        on -> onAccent()
        else -> offAccent()
    }
    return DevicePanelColors(
        accent = accent,
        accentContainer = if (on || free) accent.copy(alpha = if (isDarkTheme()) 0.22f else 0.12f)
            else MaterialTheme.colorScheme.surfaceContainerHigh,
        subtitle = if (free) freeAccent() else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun deviceSwitchColors() = SwitchDefaults.colors(
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = if (isDarkTheme()) 0.78f else 0.62f
    ),
    uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = if (isDarkTheme()) 0.72f else 0.46f
    )
)

/** Dibuja un borde punteado (dashed) redondeado, para las tarjetas de dispositivos libres. */
private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp, strokeWidth: Dp = 1.5.dp) =
    this.drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
            )
        )
    }

@Composable
fun DeviceCard(
    device: DeviceDto,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    free: Boolean = false,
    onToggle: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val typeId = device.type.id
    val on = isDeviceOn(typeId, device.state)
    val toggleable = canToggle(typeId)

    val panelColors = devicePanelColors(on = on, free = free)

    // El contenedor del ícono cambia de color al encender, con una transición
    // suave. Encendido: ícono en color primario sobre fondo apenas tintado,
    // igual que la web (#3a5a90 sobre #eef4ff).
    val iconBg by animateColorAsState(
        targetValue = panelColors.accentContainer,
        label = "iconBg"
    )
    val iconTint by animateColorAsState(
        targetValue = panelColors.accent,
        label = "iconTint"
    )
    val elevation by animateDpAsState(
        targetValue = if (on) 3.dp else 1.dp,
        label = "elevation"
    )

    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (free) Modifier.dashedBorder(
                    color = panelColors.accent.copy(alpha = if (isDarkTheme()) 0.75f else 0.55f),
                    cornerRadius = 16.dp
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Franja de acento a la izquierda que marca el estado de un vistazo.
            Box(
                Modifier
                    .size(width = 4.dp, height = 56.dp)
                    .drawBehind { drawRect(panelColors.accent) }
            )

            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = iconBg,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = deviceIcon(typeId),
                            contentDescription = stringResource(deviceTypeName(typeId)),
                            modifier = Modifier.size(26.dp),
                            tint = iconTint
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = truncateName(device.name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (subtitle.isNotEmpty()) subtitle else stringResource(deviceTypeName(typeId)),
                        style = MaterialTheme.typography.bodySmall,
                        color = panelColors.subtitle,
                        fontStyle = if (free) FontStyle.Italic else FontStyle.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.size(3.dp))
                    StatusRow(on = on, toggleable = toggleable)
                }

                if (toggleable) {
                    Switch(
                        checked = on,
                        onCheckedChange = { onToggle() },
                        colors = deviceSwitchColors()
                    )
                }
            }
        }
    }
}

/**
 * Variante compacta y vertical de [DeviceCard], pensada para mostrarse en una
 * grilla de dos (o más) columnas, de modo que entren más dispositivos en pantalla.
 */
@Composable
fun DeviceGridCard(
    device: DeviceDto,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    free: Boolean = false,
    onToggle: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val typeId = device.type.id
    val on = isDeviceOn(typeId, device.state)
    val toggleable = canToggle(typeId)
    val panelColors = devicePanelColors(on = on, free = free)

    val iconBg by animateColorAsState(
        targetValue = panelColors.accentContainer,
        label = "gridIconBg"
    )
    val iconTint by animateColorAsState(
        targetValue = panelColors.accent,
        label = "gridIconTint"
    )
    val elevation by animateDpAsState(
        targetValue = if (on) 3.dp else 1.dp,
        label = "gridElevation"
    )

    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp)
            .then(
                if (free) Modifier.dashedBorder(
                    color = panelColors.accent.copy(alpha = if (isDarkTheme()) 0.75f else 0.55f),
                    cornerRadius = 16.dp
                ) else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = iconBg,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = deviceIcon(typeId),
                            contentDescription = stringResource(deviceTypeName(typeId)),
                            modifier = Modifier.size(24.dp),
                            tint = iconTint
                        )
                    }
                }
                if (toggleable) {
                    Switch(
                        checked = on,
                        onCheckedChange = { onToggle() },
                        colors = deviceSwitchColors()
                    )
                }
            }

            Spacer(Modifier.size(12.dp))

            Text(
                text = truncateName(device.name, max = 16),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = panelColors.subtitle,
                    fontStyle = if (free) FontStyle.Italic else FontStyle.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.weight(1f))
            StatusRow(on = on, toggleable = toggleable)
        }
    }
}

/** Punto de color + texto que comunica el estado del dispositivo. */
@Composable
private fun StatusRow(on: Boolean, toggleable: Boolean) {
    val dotColor by animateColorAsState(
        targetValue = if (on) onAccent() else offAccent(),
        label = "dot"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .drawBehind { drawCircle(dotColor) }
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = when {
                !toggleable && on -> stringResource(R.string.common_active)
                on -> stringResource(R.string.common_on)
                else -> stringResource(R.string.common_off)
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (on) onAccent() else offAccent()
        )
    }
}
