package com.superplanner.app.ui.availability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.AvailabilityKind
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityScreen(
    onBack: (() -> Unit)? = null,
    viewModel: AvailabilityViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.availability_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_availability))
            }
        },
    ) { padding ->
        if (items.isEmpty()) {
            Text(
                stringResource(R.string.availability_empty),
                modifier = Modifier.padding(padding).padding(24.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(items, key = { it.id.value }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dayLabel(item.dayOfWeek))
                            Text(
                                "${item.window.start.format(timeFormatter)}–${item.window.end.format(timeFormatter)} · " +
                                    if (item.kind == AvailabilityKind.FREE) stringResource(R.string.availability_free)
                                    else stringResource(R.string.availability_blocked),
                            )
                        }
                        IconButton(onClick = { viewModel.delete(item.id.value) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete_availability))
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AvailabilityEditorDialog(
            onDismiss = { showDialog = false },
            onSave = { day, start, end, kind ->
                val error = viewModel.save(day, start, end, kind)
                if (error == null) showDialog = false
                error
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailabilityEditorDialog(
    onDismiss: () -> Unit,
    onSave: (DayOfWeek, String, String, AvailabilityKind) -> String?,
) {
    var day by remember { mutableStateOf(DayOfWeek.MONDAY) }
    var start by remember { mutableStateOf("08:00") }
    var end by remember { mutableStateOf("12:00") }
    var kind by remember { mutableStateOf(AvailabilityKind.FREE) }
    var error by remember { mutableStateOf<String?>(null) }
    var dayExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.availability_new)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = !dayExpanded }) {
                    OutlinedTextField(
                        value = dayLabel(day),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.availability_day)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dayExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                        DayOfWeek.entries.forEach { candidate ->
                            DropdownMenuItem(text = { Text(dayLabel(candidate)) }, onClick = {
                                day = candidate
                                dayExpanded = false
                            })
                        }
                    }
                }
                OutlinedTextField(
                    value = start,
                    onValueChange = { start = it },
                    label = { Text(stringResource(R.string.availability_start)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = end,
                    onValueChange = { end = it },
                    label = { Text(stringResource(R.string.availability_end)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (kind == AvailabilityKind.FREE) {
                        Button(onClick = { kind = AvailabilityKind.FREE }) { Text(stringResource(R.string.availability_free)) }
                    } else {
                        OutlinedButton(onClick = { kind = AvailabilityKind.FREE }) { Text(stringResource(R.string.availability_free)) }
                    }
                    if (kind == AvailabilityKind.BLOCKED) {
                        Button(onClick = { kind = AvailabilityKind.BLOCKED }) { Text(stringResource(R.string.availability_blocked)) }
                    } else {
                        OutlinedButton(onClick = { kind = AvailabilityKind.BLOCKED }) { Text(stringResource(R.string.availability_blocked)) }
                    }
                }
                if (error != null) Text(stringResource(errorString(error!!)))
            }
        },
        confirmButton = {
            Button(onClick = { error = onSave(day, start, end, kind) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

private fun errorString(error: String): Int = when (error) {
    "invalid_start" -> R.string.availability_invalid_start
    "invalid_end" -> R.string.availability_invalid_end
    else -> R.string.availability_invalid_range
}

private fun dayLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Segunda-feira"
    DayOfWeek.TUESDAY -> "Terça-feira"
    DayOfWeek.WEDNESDAY -> "Quarta-feira"
    DayOfWeek.THURSDAY -> "Quinta-feira"
    DayOfWeek.FRIDAY -> "Sexta-feira"
    DayOfWeek.SATURDAY -> "Sábado"
    DayOfWeek.SUNDAY -> "Domingo"
}
