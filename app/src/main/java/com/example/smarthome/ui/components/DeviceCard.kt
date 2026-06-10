package com.example.smarthome.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.domain.canToggle
import com.example.smarthome.domain.deviceIcon
import com.example.smarthome.domain.isDeviceOn

/**
 * Recorta el nombre a un máximo de caracteres para que las tarjetas se vean
 * uniformes. Si el nombre supera el límite, muestra los primeros [max]
 * caracteres seguidos de "...".
 */
fun truncateName(name: String, max: Int = 12): String =
    if (name.length > max) name.take(max).trimEnd() + "..." else name

/** Color naranja para señalar dispositivos sin habitación (igual que la web). */
private val FreeAccent = Color(0xFFE67E22)

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

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (free) Modifier.dashedBorder(
                    color = MaterialTheme.colorScheme.outline,
                    cornerRadius = 12.dp
                ) else Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (on) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = deviceIcon(typeId),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (on) MaterialTheme.colorScheme.onPrimaryContainer
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = truncateName(device.name),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (free) FreeAccent else MaterialTheme.colorScheme.outline,
                        fontStyle = if (free) FontStyle.Italic else FontStyle.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (on) "Encendido" else "Apagado",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (on) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                )
            }

            if (toggleable) {
                Switch(checked = on, onCheckedChange = { onToggle() })
            }
        }
    }
}
