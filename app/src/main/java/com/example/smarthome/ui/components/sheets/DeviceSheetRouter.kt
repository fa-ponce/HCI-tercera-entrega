package com.example.smarthome.ui.components.sheets

import androidx.compose.runtime.Composable
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto
import com.example.smarthome.domain.DeviceTypes

@Composable
fun DeviceSheetRouter(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    onDeviceRenamed: ((DeviceDto) -> Unit)? = null,
    onDeviceDeleted: ((String) -> Unit)? = null,
    onDeviceRoomChanged: ((DeviceDto) -> Unit)? = null,
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    when (device.type.id) {
        DeviceTypes.LAMPARA    -> LamparaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.AC         -> AcSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.PUERTA     -> PuertaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.ALARMA     -> AlarmaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.PERSIANA   -> PersianaSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.HORNO      -> HornoSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.GRIFO      -> GrifoSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.PARLANTE   -> ParlanteSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.ASPIRADORA -> AspiradoraSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        DeviceTypes.HELADERA   -> HeladeraSheet(device, routineMode, onDismiss, onAddToRoutine, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
        else                   -> GenericSheet(device, onDismiss, onDeviceRenamed, onDeviceDeleted, onDeviceRoomChanged, homes, rooms)
    }
}

data class DeviceAction(val actionName: String, val params: Map<String, Any> = emptyMap())
