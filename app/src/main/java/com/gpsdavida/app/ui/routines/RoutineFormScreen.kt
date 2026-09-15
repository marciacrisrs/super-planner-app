package com.superplanner.app.ui.routines

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
fun RoutineFormScreen(onDone: () -> Unit, viewModel: RoutineFormViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var pickStart by remember { mutableStateOf(false) }
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    LaunchedEffect(state.finished) { if (state.finished) onDone() }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(if (state.isNew) R.string.routine_new else R.string.routine_edit)) },
            navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back)) } },
            actions = { if (!state.isNew) IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, stringResource(R.string.routine_delete)) } },
        )
    }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(state.title, viewModel::onTitleChange, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.routine_title)) }, isError = state.error == RoutineFormError.BLANK_TITLE)
            Text(stringResource(R.string.routine_days))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { weekDayOptions().forEach { (day, label) -> FilterChip(day in state.days, { viewModel.toggleDay(day) }, label = { Text(stringResource(label)) }) } }
            Text(stringResource(R.string.task_priority))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Priority.entries.forEach { priority -> FilterChip(state.priority == priority, { viewModel.onPriority(priority) }, label = { Text(stringResource(priority.labelRes())) }) } }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Text(stringResource(R.string.routine_start_time)); Switch(state.hasStartTime, viewModel::setHasStartTime) }
            if (state.hasStartTime) OutlinedButton({ pickStart = true }) { Text(state.startTime.format(timeFmt)) }
            Text(stringResource(R.string.routine_steps))
            state.steps.forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(step.title, { viewModel.updateStepTitle(index, it) }, Modifier.weight(1f), label = { Text("${index + 1}") })
                    OutlinedTextField(step.durationMinutes, { viewModel.updateStepDuration(index, it) }, Modifier.weight(.45f), label = { Text("min") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    TextButton({ viewModel.moveStep(index, -1) }, enabled = index > 0) { Text("↑") }
                    TextButton({ viewModel.moveStep(index, 1) }, enabled = index < state.steps.lastIndex) { Text("↓") }
                    TextButton({ viewModel.removeStep(index) }) { Text("×") }
                }
            }
            if (state.error == RoutineFormError.NO_STEPS) Text(stringResource(R.string.routine_steps_required))
            if (state.error == RoutineFormError.INVALID_STEP) Text(stringResource(R.string.routine_step_invalid))
            OutlinedButton(viewModel::addStep, Modifier.fillMaxWidth()) { Text(stringResource(R.string.routine_add_step)) }
            Button(viewModel::save, Modifier.fillMaxWidth()) { Text(stringResource(R.string.routine_save)) }
        }
    }
    if (pickStart) FormTimePickerDialog(state.startTime, { pickStart = false }) { viewModel.onStartTime(it); pickStart = false }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text(stringResource(R.string.routine_delete_confirm)) }, confirmButton = { TextButton({ confirmDelete = false; viewModel.delete() }) { Text(stringResource(R.string.routine_delete)) } }, dismissButton = { TextButton({ confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) } })
}
