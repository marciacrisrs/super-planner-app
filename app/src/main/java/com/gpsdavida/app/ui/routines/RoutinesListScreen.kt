package com.superplanner.app.ui.routines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.Routine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesListScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: RoutinesListViewModel = hiltViewModel(),
) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_rotinas)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_routine))
            }
        },
    ) { padding ->
        if (routines.isEmpty()) {
            Text(
                text = stringResource(R.string.routines_empty),
                modifier = Modifier.padding(padding).padding(24.dp),
            )
        } else {
            Column(modifier = Modifier.padding(padding)) {
                routines.forEach { routine ->
                    RoutineRow(routine = routine, onClick = { onOpen(routine.id.value) })
                }
            }
        }
    }
}

@Composable
private fun RoutineRow(routine: Routine, onClick: () -> Unit) {
    val duration = routine.steps.sumOf { it.plannedDuration.toMinutes() }
    val start = routine.startTime?.toString()?.let { stringResource(R.string.routine_start, it) }
    val summary = listOfNotNull(
        stringResource(R.string.routine_steps_count, routine.steps.size),
        stringResource(R.string.task_duration_minutes, duration),
        start,
    ).joinToString(" · ")
    ListItem(
        headlineContent = { Text(routine.title) },
        supportingContent = { Text(summary) },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    )
}
