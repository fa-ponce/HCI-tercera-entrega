package com.example.smarthome.ui.components.sheets

import androidx.compose.runtime.Composable
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.domain.DeviceTypes

data class DeviceSheetActions(
    val onExecuteAction: (action: String, params: Map<String, Any>, onResult: ((Result<Map<String, Any?>>) -> Unit)?) -> Unit = { _, _, _ -> },
    val onRename: (newName: String, onResult: (Result<DeviceDto>) -> Unit) -> Unit = { _, _ -> },
    val onDelete: (onResult: (Result<Unit>) -> Unit) -> Unit = { _ -> },
    val onLink: (roomId: String, onResult: (Result<Unit>) -> Unit) -> Unit = { _, _ -> },
    val onUnlink: (onResult: (Result<Unit>) -> Unit) -> Unit = { _ -> },
    val onLoad: (suspend (deviceId: String) -> DeviceDto?)? = null,
    val onGetVacuumPrefs: ((deviceId: String) -> Pair<String?, String?>)? = null,
    val onSaveVacuumPrefs: ((deviceId: String, locationId: String?, dockingId: String?) -> Unit)? = null,
    val onLoadVacuumPrefsAsync: (suspend (deviceId: String) -> Pair<String?, String?>)? = null
)

@Composable
fun DeviceSheetRouter(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    when (device.type.id) {
        DeviceTypes.LAMPARA    -> LamparaSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.AC         -> AcSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.PUERTA     -> PuertaSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.ALARMA     -> AlarmaSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.PERSIANA   -> PersianaSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.HORNO      -> HornoSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.GRIFO      -> GrifoSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.PARLANTE   -> ParlanteSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.ASPIRADORA -> AspiradoraSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        DeviceTypes.HELADERA   -> HeladeraSheet(device, routineMode, onDismiss, onAddToRoutine, actions, homes, rooms)
        else                   -> GenericSheet(device, onDismiss, actions, homes, rooms)
    }
}

data class DeviceAction(val actionName: String, val params: Map<String, Any> = emptyMap())
