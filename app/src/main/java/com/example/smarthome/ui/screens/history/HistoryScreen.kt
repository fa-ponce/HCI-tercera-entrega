package com.example.smarthome.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import com.example.smarthome.ui.components.appBarGradient
import com.example.smarthome.ui.components.gradientTopBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.smarthome.R
import com.example.smarthome.domain.logActionLabelRes
import com.example.smarthome.ui.AppViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.layout.WindowInsets

private const val PAGE_SIZE = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    appViewModel: AppViewModel,
    navController: NavHostController
) {
    val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
    val filtered by vm.filteredLogs.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val search by vm.search.collectAsState()
    val page by vm.page.collectAsState()

    val totalPages = maxOf(1, (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE)
    val currentPageItems = filtered.drop(page * PAGE_SIZE).take(PAGE_SIZE)

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.appBarGradient(),
                title = { Text(stringResource(R.string.history_title), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { vm.fetchLogs() }) {
                        Icon(Icons.Rounded.Refresh, stringResource(R.string.history_refresh))
                    }
                },
                colors = gradientTopBarColors()
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                start = 16.dp, end = 16.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Búsqueda
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { vm.search.value = it; vm.page.value = 0 },
                    placeholder = { Text(stringResource(R.string.history_search_placeholder)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = if (search.isNotEmpty()) {
                        {
                            IconButton(onClick = { vm.search.value = ""; vm.page.value = 0 }) {
                                Icon(Icons.Rounded.Clear, stringResource(R.string.common_clear))
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }

            // Info
            item {
                Text(
                    stringResource(R.string.history_records, filtered.size) +
                        if (totalPages > 1) " · " + stringResource(R.string.history_page, page + 1, totalPages) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filtered.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.history_none), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // Cabecera de la tabla
                item {
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(stringResource(R.string.history_col_time), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(52.dp))
                            Text(stringResource(R.string.history_col_device), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(stringResource(R.string.history_col_action), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                        }
                    }
                }

                itemsIndexed(currentPageItems) { idx, log ->
                    val (_, time) = parseTimestamp(log.timestamp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.width(52.dp)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                log.deviceName ?: "—",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                            if (!log.roomName.isNullOrEmpty() || !log.homeName.isNullOrEmpty()) {
                                Text(
                                    listOfNotNull(log.roomName, log.homeName).joinToString(" · "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            logActionLabelRes(log.event)?.let { stringResource(it) } ?: log.event ?: "—",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.8f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (idx < currentPageItems.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }

                // Paginación
                if (totalPages > 1) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { vm.page.value = (page - 1).coerceAtLeast(0) },
                                enabled = page > 0
                            ) {
                                Icon(Icons.Rounded.ArrowBack, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.common_previous))
                            }
                            Text(
                                "${page + 1} / $totalPages",
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedButton(
                                onClick = { vm.page.value = (page + 1).coerceAtMost(totalPages - 1) },
                                enabled = page < totalPages - 1
                            ) {
                                Text(stringResource(R.string.common_next))
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Rounded.ArrowForward, null, Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseTimestamp(ts: String?): Pair<String, String> {
    if (ts == null) return Pair("—", "—")
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(ts) ?: return Pair(ts.take(10), "—")
        val dateFmt = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        Pair(dateFmt.format(date), timeFmt.format(date))
    } catch (e: Exception) {
        Pair(ts.take(10), "—")
    }
}
