package com.superplanner.app.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.ui.shared.FormTimePickerDialog
import com.superplanner.app.ui.shared.weekDayOptions
import com.superplanner.app.ui.tasks.labelRes
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitFormScreen(onDone: () -> Unit, viewModel: HabitFormViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) { if (state.finished) onDone() }
    var confirmDelete by remember { mutableStateOf(false) }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(if (state.isNew) R.string.habit_new else R.string.habit_edit)) },
            navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back)) } },
            actions = { if (!state.isNew) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, stringResource(R.string.habit_delete)) } },
        )
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(state.title, viewModel::onTitleChange, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.habit_title)) }, isError = state.error == HabitFormError.BLANK_TITLE, supportingText = { if (state.error == HabitFormError.BLANK_TITLE) Text(stringResource(R.string.habit_title_required)) })
            OutlinedTextField(state.durationMinutes, viewModel::onDurationChange, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.task_duration_label)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = state.error == HabitFormError.INVALID_DURATION, supportingText = { if (state.error == HabitFormError.INVALID_DURATION) Text(stringResource(R.string.task_duration_required)) })
            Text(stringResource(R.string.habit_frequency))
            Text(stringResource(R.string.habit_frequency_hint))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { weekDayOptions().forEach { (day, label) -> FilterChip(day in state.days, { viewModel.toggleDay(day) }, label = { Text(stringResource(label)) }) } }
            Text(stringResource(R.string.task_priority))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Priority.entries.forEach { priority -> FilterChip(state.priority == priority, { viewModel.onPriority(priority) }, label = { Text(stringResource(priority.labelRes())) }) } }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text(stringResource(R.string.habit_window)); Switch(state.hasWindow, viewModel::setHasWindow) }
            if (state.hasWindow) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ pickStart = true }) { Text(stringResource(R.string.habit_window_start, state.windowStart.format(timeFmt))) }
                    OutlinedButton({ pickEnd = true }) { Text(stringResource(R.string.habit_window_end, state.windowEnd.format(timeFmt))) }
                }
                if (state.error == HabitFormError.INVALID_WINDOW) Text(stringResource(R.string.habit_window_invalid))
            }
            Button(viewModel::save, Modifier.fillMaxWidth()) { Text(stringResource(R.string.habit_save)) }
        }
    }
    if (pickStart) FormTimePickerDialog(state.windowStart, { pickStart = false }) { viewModel.onWindowStart(it); pickStart = false }
    if (pickEnd) FormTimePickerDialog(state.windowEnd, { pickEnd = false }) { viewModel.onWindowEnd(it); pickEnd = false }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text(stringResource(R.string.habit_delete_confirm)) }, confirmButton = { TextButton({ confirmDelete = false; viewModel.delete() }) { Text(stringResource(R.string.habit_delete)) } }, dismissButton = { TextButton({ confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) } })
}
