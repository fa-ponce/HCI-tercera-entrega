package com.example.smarthome.ui.components.sheets

import androidx.compose.runtime.Composable
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.domain.DeviceTypes

@Composable
fun DeviceSheetRouter(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null
) {
    when (device.type.id) {
        DeviceTypes.LAMPARA    -> LamparaSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.AC         -> AcSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.PUERTA     -> PuertaSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.ALARMA     -> AlarmaSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.PERSIANA   -> PersianaSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.HORNO      -> HornoSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.GRIFO      -> GrifoSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.PARLANTE   -> ParlanteSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.ASPIRADORA -> AspiradoraSheet(device, routineMode, onDismiss, onAddToRoutine)
        DeviceTypes.HELADERA   -> HeladeraSheet(device, routineMode, onDismiss, onAddToRoutine)
        else                   -> GenericSheet(device, onDismiss)
    }
}

data class DeviceAction(val actionName: String, val params: Map<String, Any> = emptyMap())
