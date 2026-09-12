package com.superplanner.app.ui.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.Priority
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFormScreen(
    onDone: () -> Unit,
    viewModel: TaskFormViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) {
        if (state.finished) onDone()
    }
    var confirmDelete by remember { mutableStateOf(false) }
    var pickDue by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()
    val dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt", "BR"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (state.isNew) R.string.task_new else R.string.task_edit),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.task_delete),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.task_title)) },
                isError = state.error == TaskFormError.BLANK_TITLE,
                supportingText = {
                    if (state.error == TaskFormError.BLANK_TITLE) {
                        Text(stringResource(R.string.task_title_required))
                    }
                },
            )
            OutlinedTextField(
                value = state.durationMinutes,
                onValueChange = viewModel::onDurationChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.task_duration_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = state.error == TaskFormError.INVALID_DURATION,
                supportingText = {
                    if (state.error == TaskFormError.INVALID_DURATION) {
                        Text(stringResource(R.string.task_duration_required))
                    }
                },
            )
            Text(stringResource(R.string.task_priority))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = state.priority == priority,
                        onClick = { viewModel.onPriority(priority) },
                        label = { Text(stringResource(priority.labelRes())) },
                    )
                }
            }
            val dueLabel = state.due?.atZone(zone)?.toLocalDate()?.format(dateFmt)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { pickDue = true }) {
                    Text(
                        if (dueLabel == null) stringResource(R.string.task_due_none)
                        else stringResource(R.string.task_due_prefix, dueLabel),
                    )
                }
                if (state.due != null) {
                    TextButton(onClick = { viewModel.onDueDate(null) }) {
                        Text(stringResource(R.string.task_due_clear))
                    }
                }
            }
            if (!state.isNew) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.task_done))
                    Switch(checked = state.done, onCheckedChange = viewModel::setDone)
                }
            }
            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.task_save))
            }
        }
    }

    if (pickDue) {
        val initial = state.due?.atZone(zone)?.toLocalDate() ?: LocalDate.now()
        val picker = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickDue = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = picker.selectedDateMillis ?: return@TextButton
                        viewModel.onDueDate(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                        )
                        pickDue = false
                    },
                ) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { pickDue = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(state = picker)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.task_delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.delete()
                    },
                ) { Text(stringResource(R.string.task_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
