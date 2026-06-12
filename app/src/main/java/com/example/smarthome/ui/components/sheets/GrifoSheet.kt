package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrifoSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    var isLoading by remember { mutableStateOf(!routineMode) }
    var isOpen by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }
    var unit by remember { mutableStateOf("mililitro") }
    var selectedAction by remember { mutableStateOf("open") }
    var isDispensing by remember { mutableStateOf(false) }
    var dispensedMsg by remember { mutableStateOf<String?>(null) }

    val unidades = listOf(
        "mililitro" to R.string.sheet_unit_ml,
        "centilitro" to R.string.sheet_unit_cl,
        "decilitro" to R.string.sheet_unit_dl,
        "litro" to R.string.sheet_unit_l
    )

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                isOpen = d.state["status"] == "opened"
                selectedAction = if (isOpen) "open" else "close"
            }
            isLoading = false
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SheetHeader(
                title = device.name,
                subtitle = stringResource(R.string.device_type_faucet),
                onRename = if (!routineMode) { newName, cb -> actions.onRename(newName, cb) } else null
            )

            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Status badge
                if (!routineMode) {
                    Surface(
                        color = if (isOpen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isOpen) stringResource(R.string.sheet_open_m) else stringResource(R.string.sheet_closed_m),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Open / Close
                SheetSectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        val openActive = if (routineMode) selectedAction == "open" else isOpen
                        OutlinedButton(
                            onClick = {
                                if (routineMode) {
                                    selectedAction = "open"
                                } else {
                                    actions.onExecuteAction("open", emptyMap(), null)
                                    isOpen = true; selectedAction = "open"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = routineMode || !isOpen,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (openActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) { Text(stringResource(R.string.sheet_open_action)) }

                        val closeActive = if (routineMode) selectedAction == "close" else !isOpen
                        OutlinedButton(
                            onClick = {
                                if (routineMode) {
                                    selectedAction = "close"
                                } else {
                                    actions.onExecuteAction("close", emptyMap(), null)
                                    isOpen = false; selectedAction = "close"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = routineMode || isOpen,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (closeActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            )
                        ) { Text(stringResource(R.string.sheet_close_action)) }
                    }
                }

                // Dispense section
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SheetSectionLabel(stringResource(R.string.sheet_dispense))

                        // Quantity
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.sheet_quantity), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FilledIconButton(
                                    onClick = { if (quantity > 0) quantity-- },
                                    enabled = quantity > 0
                                ) { Text("-", style = MaterialTheme.typography.titleMedium) }

                                Text(
                                    "$quantity",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.widthIn(min = 40.dp)
                                )

                                FilledIconButton(
                                    onClick = { if (quantity < 100) quantity++ },
                                    enabled = quantity < 100
                                ) { Text("+", style = MaterialTheme.typography.titleMedium) }
                            }
                        }

                        // Unit
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(stringResource(R.string.sheet_unit), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                unidades.forEach { (value, labelRes) ->
                                    FilterChip(
                                        selected = unit == value,
                                        onClick = { unit = value },
                                        label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Dispense button
                        val dispensedFmt = stringResource(R.string.sheet_dispensed_fmt, quantity, unit)
                        Button(
                            onClick = {
                                if (routineMode) {
                                    selectedAction = "dispense"
                                } else {
                                    isDispensing = true
                                    dispensedMsg = null
                                    actions.onExecuteAction("dispense", mapOf("quantity" to quantity, "unit" to unit)) { result ->
                                        result.onSuccess { dispensedMsg = dispensedFmt }
                                        isDispensing = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isDispensing && quantity > 0
                        ) {
                            Text(if (isDispensing) stringResource(R.string.sheet_dispensing) else stringResource(R.string.sheet_dispense_fmt, quantity, unit))
                        }

                        dispensedMsg?.let {
                            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (routineMode) {
                    SheetRoutineFooter(
                        onCancel = onDismiss,
                        onAdd = {
                            val routineActions = if (selectedAction == "dispense")
                                listOf(DeviceAction("dispense", mapOf("quantity" to quantity, "unit" to unit)))
                            else
                                listOf(DeviceAction(selectedAction))
                            onAddToRoutine?.invoke(routineActions)
                            onDismiss()
                        }
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SheetRoomLinkButton(device = device, homes = homes, rooms = rooms, modifier = Modifier.weight(1f), onUnlink = actions.onUnlink, onLink = actions.onLink)
                        SheetDeleteButton(onDelete = actions.onDelete, onDismiss = onDismiss, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
