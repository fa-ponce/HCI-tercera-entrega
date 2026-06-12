package com.example.smarthome.ui.components.sheets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.domain.deviceTypeName
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onRename: ((newName: String, onResult: (Result<DeviceDto>) -> Unit) -> Unit)? = null
) {
    var displayTitle by remember(title) { mutableStateOf(title) }
    var showDialog by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf("") }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        if (onRename != null) {
            IconButton(onClick = { editText = displayTitle; showDialog = true }) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.common_edit_name),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDialog) {
        var isSaving by remember { mutableStateOf(false) }
        var saveError by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!isSaving) showDialog = false },
            title = { Text(stringResource(R.string.sheet_rename_device), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it; saveError = null },
                        label = { Text(stringResource(R.string.common_name)) },
                        singleLine = true,
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (saveError != null) {
                        Text(saveError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = editText.trim()
                        if (newName.isNotEmpty() && !isSaving) {
                            isSaving = true
                            saveError = null
                            onRename?.invoke(newName) { result ->
                                result.onSuccess {
                                    displayTitle = newName
                                    showDialog = false
                                }.onFailure { saveError = it.message }
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }, enabled = !isSaving) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@Composable
fun SheetSectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SheetSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun SheetDeleteButton(
    onDelete: (onResult: (Result<Unit>) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    Button(
        onClick = { showConfirm = true },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
    ) {
        Text(stringResource(R.string.common_delete), color = Color.White, fontWeight = FontWeight.SemiBold)
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showConfirm = false },
            title = { Text(stringResource(R.string.sheet_delete_device), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.sheet_delete_confirm))
                    if (deleteError != null) {
                        Text(deleteError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isDeleting) {
                            isDeleting = true
                            deleteError = null
                            onDelete { result ->
                                result.onSuccess { onDismiss() }
                                    .onFailure { deleteError = it.message }
                                isDeleting = false
                            }
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.common_delete), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }, enabled = !isDeleting) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetRoomLinkButton(
    device: DeviceDto,
    homes: List<HomeDto>,
    rooms: Map<String, List<RoomDto>>,
    modifier: Modifier = Modifier,
    onUnlink: (onResult: (Result<Unit>) -> Unit) -> Unit = { _ -> },
    onLink: (roomId: String, onResult: (Result<Unit>) -> Unit) -> Unit = { _, _ -> }
) {
    val isLinked = device.room != null
    var showUnlinkConfirm by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var selectedHome by remember { mutableStateOf<HomeDto?>(null) }
    var homeExpanded by remember { mutableStateOf(false) }
    var selectedRoom by remember { mutableStateOf<RoomDto?>(null) }
    var roomExpanded by remember { mutableStateOf(false) }
    var isUnlinking by remember { mutableStateOf(false) }
    var isLinking by remember { mutableStateOf(false) }
    var unlinkError by remember { mutableStateOf<String?>(null) }
    var linkError by remember { mutableStateOf<String?>(null) }

    OutlinedButton(
        onClick = { if (isLinked) showUnlinkConfirm = true else showLinkDialog = true },
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isLinked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(
            1.dp,
            if (isLinked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    ) {
        Text(if (isLinked) stringResource(R.string.sheet_unlink) else stringResource(R.string.sheet_link), fontWeight = FontWeight.SemiBold)
    }

    if (showUnlinkConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isUnlinking) showUnlinkConfirm = false },
            title = { Text(stringResource(R.string.sheet_unlink_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.sheet_unlink_msg))
                    if (unlinkError != null) {
                        Text(unlinkError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isUnlinking) {
                            isUnlinking = true
                            unlinkError = null
                            onUnlink { result ->
                                result.onSuccess { showUnlinkConfirm = false }
                                    .onFailure { unlinkError = it.message }
                                isUnlinking = false
                            }
                        }
                    },
                    enabled = !isUnlinking,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isUnlinking) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.sheet_unlink), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkConfirm = false }, enabled = !isUnlinking) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false; selectedHome = null; selectedRoom = null },
            title = { Text(stringResource(R.string.sheet_link_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (linkError != null) {
                        Text(linkError.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    ExposedDropdownMenuBox(
                        expanded = homeExpanded,
                        onExpandedChange = { homeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedHome?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.common_home)) },
                            placeholder = { Text(stringResource(R.string.sheet_select_home)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(homeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = homeExpanded,
                            onDismissRequest = { homeExpanded = false }
                        ) {
                            homes.forEach { home ->
                                DropdownMenuItem(
                                    text = { Text(home.name) },
                                    onClick = {
                                        selectedHome = home
                                        selectedRoom = null
                                        homeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (selectedHome != null) {
                        val availableRooms = rooms[selectedHome?.id] ?: emptyList()
                        ExposedDropdownMenuBox(
                            expanded = roomExpanded,
                            onExpandedChange = { roomExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedRoom?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.common_room)) },
                                placeholder = { Text(stringResource(R.string.sheet_select_room)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roomExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = roomExpanded,
                                onDismissRequest = { roomExpanded = false }
                            ) {
                                availableRooms.forEach { room ->
                                    DropdownMenuItem(
                                        text = { Text(room.name) },
                                        onClick = { selectedRoom = room; roomExpanded = false }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val room = selectedRoom ?: return@Button
                        if (!isLinking) {
                            isLinking = true
                            linkError = null
                            onLink(room.id) { result ->
                                result.onSuccess {
                                    selectedHome = null
                                    selectedRoom = null
                                    showLinkDialog = false
                                }.onFailure { linkError = it.message }
                                isLinking = false
                            }
                        }
                    },
                    enabled = selectedRoom != null && !isLinking
                ) {
                    if (isLinking) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.sheet_link))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLinkDialog = false; selectedHome = null; selectedRoom = null },
                    enabled = !isLinking
                ) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@Composable
fun SheetRoutineFooter(onCancel: () -> Unit, onAdd: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.common_cancel))
        }
        Button(onClick = onAdd, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.sheet_add_to_routine))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseDeviceSheet(
    device: DeviceDto,
    routineMode: Boolean,
    onDismiss: () -> Unit,
    actions: DeviceSheetActions,
    homes: List<HomeDto>,
    rooms: Map<String, List<RoomDto>>,
    isLoading: Boolean,
    onAddToRoutine: (() -> Unit)? = null,
    showFooter: Boolean = true,
    spacing: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            SheetHeader(
                title = device.name,
                subtitle = stringResource(deviceTypeName(device.type.id)),
                onRename = if (!routineMode) { newName, cb -> actions.onRename(newName, cb) } else null
            )
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                content()
                if (showFooter) {
                    if (routineMode && onAddToRoutine != null) {
                        SheetRoutineFooter(onCancel = onDismiss, onAdd = onAddToRoutine)
                    } else if (!routineMode) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SheetRoomLinkButton(
                                device = device,
                                homes = homes,
                                rooms = rooms,
                                modifier = Modifier.weight(1f),
                                onUnlink = actions.onUnlink,
                                onLink = actions.onLink
                            )
                            SheetDeleteButton(
                                onDelete = actions.onDelete,
                                onDismiss = onDismiss,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> PillGroup(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option) },
                label = { Text(label(option), style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}
