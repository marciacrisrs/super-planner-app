package com.superplanner.app.ui.meudia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.ui.events.EventRow
import com.superplanner.app.ui.habits.HabitDayRow
import com.superplanner.app.ui.tasks.TaskRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeuDiaScreen(
    onAddEvent: () -> Unit,
    onOpenEvent: (String) -> Unit,
    onOpenTask: (String) -> Unit,
    onOpenHabit: (String) -> Unit,
    onOpenAvailability: () -> Unit,
    onOpenWeek: () -> Unit,
    viewModel: MeuDiaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val empty = state.events.isEmpty() && state.tasks.isEmpty() && state.habits.isEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_meu_dia)) },
                actions = {
                    IconButton(onClick = onOpenWeek) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.cd_week))
                    }
                    IconButton(onClick = onOpenAvailability) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.cd_availability))
                    }
                },
            )
        },
    ) { padding ->
        if (empty) {
            Text(
                text = stringResource(R.string.meu_dia_empty),
                modifier = Modifier.padding(padding).padding(24.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.events.isNotEmpty()) {
                    item { Text(stringResource(R.string.nav_eventos), modifier = Modifier.padding(vertical = 8.dp)) }
                    items(state.events, key = { it.id.value }) { event ->
                        EventRow(event = event, onClick = { onOpenEvent(event.id.value) })
                    }
                }
                if (state.tasks.isNotEmpty()) {
                    item { Text(stringResource(R.string.nav_tarefas), modifier = Modifier.padding(vertical = 8.dp)) }
                    items(state.tasks, key = { it.id.value }) { task ->
                        TaskRow(task = task, onClick = { onOpenTask(task.id.value) }, onToggleDone = { viewModel.setTaskDone(task.id.value, it) })
                    }
                }
                if (state.habits.isNotEmpty()) {
                    item { Text(stringResource(R.string.nav_habitos), modifier = Modifier.padding(vertical = 8.dp)) }
                    items(state.habits, key = { it.habit.id.value }) { habitDay ->
                        HabitDayRow(item = habitDay, onClick = { onOpenHabit(habitDay.habit.id.value) }, onToggleDone = { viewModel.setHabitDone(habitDay.habit.id.value, it) })
                    }
                }
            }
        }
    }
}
