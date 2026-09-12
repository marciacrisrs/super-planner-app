package com.superplanner.app.ui.tasks

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
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.Task
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksListScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: TasksListViewModel = hiltViewModel(),
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_tarefas)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_task))
            }
        },
    ) { padding ->
        if (tasks.isEmpty()) {
            Text(
                text = stringResource(R.string.tasks_empty),
                modifier = Modifier.padding(padding).padding(24.dp),
            )
        } else {
            Column(modifier = Modifier.padding(padding)) {
                tasks.forEach { task ->
                    TaskRow(
                        task = task,
                        onClick = { onOpen(task.id.value) },
                        onToggleDone = { viewModel.setDone(task.id.value, it) },
                    )
                }
            }
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    onClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val dueText = task.due?.atZone(zone)?.format(
        DateTimeFormatter.ofPattern("dd/MM", Locale("pt", "BR")),
    )
    val duration = stringResource(R.string.task_duration_minutes, task.plannedDuration.toMinutes())
    val priority = stringResource(task.priority.labelRes())
    val supporting = buildString {
        append(duration)
        append(" · ")
        append(priority)
        if (dueText != null) {
            append(" · ")
            append(stringResource(R.string.task_due_prefix, dueText))
        }
    }
    ListItem(
        headlineContent = {
            Text(
                text = task.title,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
            )
        },
        supportingContent = { Text(supporting) },
        leadingContent = {
            Checkbox(checked = task.isDone, onCheckedChange = onToggleDone)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

fun Priority.labelRes(): Int = when (this) {
    Priority.REQUIRED -> R.string.priority_required
    Priority.IMPORTANT -> R.string.priority_important
    Priority.DESIRABLE -> R.string.priority_desirable
    Priority.LEISURE -> R.string.priority_leisure
}
