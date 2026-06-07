package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smarthome.data.api.models.DeviceDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericSheet(device: DeviceDto, onDismiss: () -> Unit, onDeviceRenamed: ((DeviceDto) -> Unit)? = null) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SheetHeader(title = device.name, subtitle = device.type.name ?: "Dispositivo", deviceId = device.id, onRenamed = { name -> onDeviceRenamed?.invoke(device.copy(name = name)) })
            Text(
                "Tipo de dispositivo no soportado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
            }
        }
    }
}
