package com.superplanner.app.ui.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.Habit
import com.superplanner.app.domain.model.HabitDay
import com.superplanner.app.ui.tasks.labelRes
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsListScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: HabitsListViewModel = hiltViewModel(),
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_habitos)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_habit))
            }
        },
    ) { padding ->
        if (habits.isEmpty()) {
            Text(
                text = stringResource(R.string.habits_empty),
                modifier = Modifier.padding(padding).padding(24.dp),
            )
        } else {
            Column(modifier = Modifier.padding(padding)) {
                habits.forEach { habit ->
                    HabitTemplateRow(habit = habit, onClick = { onOpen(habit.id.value) })
                }
            }
        }
    }
}

@Composable
fun HabitTemplateRow(habit: Habit, onClick: () -> Unit) {
    val duration = stringResource(R.string.task_duration_minutes, habit.plannedDuration.toMinutes())
    val freq = if (habit.daysOfWeek.isEmpty()) {
        stringResource(R.string.habit_every_day)
    } else {
        stringResource(R.string.habit_days_count, habit.daysOfWeek.size)
    }
    ListItem(
        headlineContent = { Text(habit.title) },
        supportingContent = { Text("$duration · $freq") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Composable
fun HabitDayRow(
    item: HabitDay,
    onClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
) {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    val window = item.habit.window?.let { "${it.start.format(timeFmt)}–${it.end.format(timeFmt)}" }
    val duration = stringResource(R.string.task_duration_minutes, item.habit.plannedDuration.toMinutes())
    val supporting = listOfNotNull(duration, window, stringResource(item.habit.priority.labelRes()))
        .joinToString(" · ")
    ListItem(
        headlineContent = {
            Text(
                text = item.habit.title,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
            )
        },
        supportingContent = { Text(supporting) },
        leadingContent = {
            Checkbox(checked = item.isDone, onCheckedChange = onToggleDone)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}
