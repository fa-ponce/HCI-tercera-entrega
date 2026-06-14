package com.example.smarthome.ui.components.sheets

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smarthome.R
import com.example.smarthome.data.api.models.DeviceDto
import com.example.smarthome.data.api.models.HomeDto
import com.example.smarthome.data.api.models.RoomDto

private const val DEFAULT_CODE = "1234"
private const val ALARM_CODE_PREFS = "alarm_codes"
private val CODE_RE = Regex("^\\d{4}$")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmaSheet(
    device: DeviceDto,
    routineMode: Boolean = false,
    onDismiss: () -> Unit,
    onAddToRoutine: ((List<DeviceAction>) -> Unit)? = null,
    actions: DeviceSheetActions = DeviceSheetActions(),
    homes: List<HomeDto> = emptyList(),
    rooms: Map<String, List<RoomDto>> = emptyMap()
) {
    val context = LocalContext.current
    val codePrefs = remember(context) {
        context.applicationContext.getSharedPreferences(ALARM_CODE_PREFS, Context.MODE_PRIVATE)
    }
    val codeKey = remember(device.id) { "alarm_code_${device.id}" }
    var storedCode by remember(device.id) {
        mutableStateOf(codePrefs.getString(codeKey, DEFAULT_CODE) ?: DEFAULT_CODE)
    }

    var isLoading by remember { mutableStateOf(!routineMode) }
    var status by remember { mutableStateOf(if (routineMode) "armedAway" else "disarmed") }
    var codeInput by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf(false) }
    var codeErrorText by remember { mutableStateOf<String?>(null) }
    var codeVerified by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var showChangeCode by remember { mutableStateOf(false) }
    var currentCode by remember { mutableStateOf("") }
    var newCode by remember { mutableStateOf("") }
    var confirmCode by remember { mutableStateOf("") }
    var changeError by remember { mutableStateOf<String?>(null) }
    var changeSuccess by remember { mutableStateOf(false) }
    var isChangingCode by remember { mutableStateOf(false) }

    LaunchedEffect(device.id) {
        if (!routineMode) {
            actions.onLoad?.invoke(device.id)?.let { d ->
                status = (d.state["status"] as? String) ?: "disarmed"
            }
            isLoading = false
        }
    }

    val actionStatusMap = mapOf("armedStay" to "armStay", "armedAway" to "armAway", "disarmed" to "disarm")
    val invalidCodeText = stringResource(R.string.sheet_code_four_digits)
    val wrongCodeText = stringResource(R.string.sheet_wrong_code)
    val actionFailedText = stringResource(R.string.err_action_execute)
    val currentCodeWrongText = stringResource(R.string.sheet_current_code_wrong)
    val newCodeMismatchText = stringResource(R.string.sheet_new_code_mismatch)

    fun cleanCode(value: String): String = value.filter(Char::isDigit).take(4)

    BaseDeviceSheet(
        device = device,
        routineMode = routineMode,
        onDismiss = onDismiss,
        actions = actions,
        homes = homes,
        rooms = rooms,
        isLoading = isLoading,
        showFooter = false
    ) {
        // Security code input before direct actions or adding alarm actions to routines.
        if (!codeVerified) {
            if (storedCode == DEFAULT_CODE) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        stringResource(R.string.sheet_default_code_notice),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            SheetSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SheetSectionLabel(stringResource(R.string.sheet_security_code))
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = {
                            codeInput = cleanCode(it)
                            codeError = false
                            codeErrorText = null
                            actionError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.sheet_enter_code)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = codeError,
                        supportingText = codeErrorText?.let { message -> { Text(message) } },
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            when {
                                !CODE_RE.matches(codeInput) -> {
                                    codeError = true
                                    codeErrorText = invalidCodeText
                                }
                                codeInput != storedCode -> {
                                    codeError = true
                                    codeErrorText = wrongCodeText
                                }
                                else -> codeVerified = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = codeInput.length == 4
                    ) {
                        Text(stringResource(R.string.sheet_confirm_code))
                    }
                }
            }
        }

        // Actions (visible after code verified or in routine mode)
        if (codeVerified) {
            SheetSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SheetSectionLabel(stringResource(R.string.sheet_actions))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("armStay", "armedStay", stringResource(R.string.sheet_mode_home)),
                            Triple("armAway", "armedAway", stringResource(R.string.sheet_mode_away)),
                            Triple("disarm", "disarmed", stringResource(R.string.sheet_disarm))
                        ).forEach { (action, targetStatus, label) ->
                            val active = status == targetStatus
                            Surface(
                                onClick = {
                                    if (routineMode) {
                                        status = targetStatus
                                    } else if (!active && !isSaving) {
                                        isSaving = true
                                        actionError = null
                                        actions.onExecuteAction(action, mapOf("securityCode" to codeInput)) { result ->
                                            result.onSuccess {
                                                status = targetStatus
                                            }.onFailure { error ->
                                                actionError = error.message ?: actionFailedText
                                            }
                                            isSaving = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium,
                                color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (active) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                enabled = !isSaving
                            ) {
                                Column(
                                    Modifier.padding(10.dp),
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    actionError?.let { message ->
                        Text(
                            message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (routineMode) {
                SheetRoutineFooter(
                    onCancel = onDismiss,
                    onAdd = {
                        val actionName = actionStatusMap[status] ?: "armAway"
                        onAddToRoutine?.invoke(
                            listOf(DeviceAction(actionName, mapOf("securityCode" to codeInput)))
                        )
                        onDismiss()
                    }
                )
            } else {
                SheetSectionCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                showChangeCode = !showChangeCode
                                changeError = null
                                changeSuccess = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.sheet_change_code))
                        }
                        if (showChangeCode) {
                            OutlinedTextField(
                                value = currentCode,
                                onValueChange = {
                                    currentCode = cleanCode(it)
                                    changeError = null
                                    changeSuccess = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.sheet_current_code)) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                isError = changeError != null,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newCode,
                                onValueChange = {
                                    newCode = cleanCode(it)
                                    changeError = null
                                    changeSuccess = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.sheet_new_code)) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                isError = changeError != null,
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = confirmCode,
                                onValueChange = {
                                    confirmCode = cleanCode(it)
                                    changeError = null
                                    changeSuccess = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.sheet_confirm_new_code)) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                isError = changeError != null,
                                supportingText = changeError?.let { message -> { Text(message) } },
                                singleLine = true
                            )
                            if (changeSuccess) {
                                Text(
                                    stringResource(R.string.sheet_code_updated),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Button(
                                onClick = {
                                    when {
                                        !CODE_RE.matches(currentCode) -> changeError = invalidCodeText
                                        currentCode != storedCode -> changeError = currentCodeWrongText
                                        !CODE_RE.matches(newCode) -> changeError = invalidCodeText
                                        newCode != confirmCode -> changeError = newCodeMismatchText
                                        else -> {
                                            isChangingCode = true
                                            changeError = null
                                            changeSuccess = false
                                            actions.onExecuteAction(
                                                "changeSecurityCode",
                                                mapOf("currentCode" to currentCode, "newCode" to newCode)
                                            ) { result ->
                                                result.onSuccess {
                                                    storedCode = newCode
                                                    codeInput = newCode
                                                    codePrefs.edit().putString(codeKey, newCode).apply()
                                                    currentCode = ""
                                                    newCode = ""
                                                    confirmCode = ""
                                                    changeSuccess = true
                                                }.onFailure { error ->
                                                    changeError = error.message ?: actionFailedText
                                                }
                                                isChangingCode = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isChangingCode
                            ) {
                                if (isChangingCode) {
                                    CircularProgressIndicator(
                                        Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(stringResource(R.string.sheet_change_code))
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetRoomLinkButton(device = device, homes = homes, rooms = rooms, modifier = Modifier.weight(1f), onUnlink = actions.onUnlink, onLink = actions.onLink)
                    SheetDeleteButton(onDelete = actions.onDelete, onDismiss = onDismiss, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
