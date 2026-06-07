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
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null,
    onDeviceDeleted: ((String) -> Unit)? = null
) {
    when (device.type.id) {
        DeviceTypes.LAMPARA    -> LamparaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.AC         -> AcSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.PUERTA     -> PuertaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.ALARMA     -> AlarmaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.PERSIANA   -> PersianaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.HORNO      -> HornoSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.GRIFO      -> GrifoSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.PARLANTE   -> ParlanteSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.ASPIRADORA -> AspiradoraSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        DeviceTypes.HELADERA   -> HeladeraSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted)
        else                   -> GenericSheet(device, onDismiss, onDeviceRenamed, onDeviceDeleted)
    }
}

data class DeviceAction(val actionName: String, val params: Map<String, Any> = emptyMap())
