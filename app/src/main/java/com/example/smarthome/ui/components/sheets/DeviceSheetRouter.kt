package com.example.smarthome.ui.components.sheets

import androidx.compose.runtime.Composable
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.domain.DeviceTypes

@Composable
fun DeviceSheetRouter(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null
) {
    when (device.type.id) {
        DeviceTypes.LAMPARA    -> LamparaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.AC         -> AcSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.PUERTA     -> PuertaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.ALARMA     -> AlarmaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.PERSIANA   -> PersianaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.HORNO      -> HornoSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.GRIFO      -> GrifoSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.PARLANTE   -> ParlanteSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.ASPIRADORA -> AspiradoraSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        DeviceTypes.HELADERA   -> HeladeraSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed)
        else                   -> GenericSheet(device, onDismiss, onDeviceRenamed)
    }
}

data class DeviceAction(val actionName: String, val params: Map<String, Any> = emptyMap())
