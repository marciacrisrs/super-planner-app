package com.superplanner.app.ui.meudia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
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
import com.superplanner.app.ui.theme.SuperPlannerBackground
import com.superplanner.app.ui.theme.SuperPlannerCard
import com.superplanner.app.ui.theme.SuperPlannerSectionHeader
import com.superplanner.app.ui.theme.SuperPlannerColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape

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

    SuperPlannerBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_meu_dia), color = SuperPlannerColors.Ink) },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                    actions = {
                        IconButton(onClick = onOpenWeek) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = stringResource(R.string.cd_week), tint = SuperPlannerColors.Ink)
                        }
                        IconButton(onClick = onOpenAvailability) {
                            Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.cd_availability), tint = SuperPlannerColors.Ink)
                        }
                    },
                )
            },
        ) { padding ->
            if (empty) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    SuperPlannerSectionHeader(
                        title = stringResource(R.string.nav_meu_dia),
                        supportingText = stringResource(R.string.meu_dia_empty),
                    )
                    SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.meu_dia_empty),
                                color = SuperPlannerColors.InkSoft,
                            )
                            Button(
                                onClick = onAddEvent,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SuperPlannerColors.Terracotta,
                                    contentColor = SuperPlannerColors.Surface,
                                ),
                            ) {
                                Text(stringResource(R.string.cd_add_event))
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        SuperPlannerSectionHeader(
                            title = stringResource(R.string.nav_meu_dia),
                            actionLabel = stringResource(R.string.cd_add_event),
                            onAction = onAddEvent,
                        )
                    }
                    if (state.events.isNotEmpty()) {
                        item {
                            SuperPlannerSectionHeader(
                                title = stringResource(R.string.nav_eventos),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        items(state.events, key = { it.id.value }) { event ->
                            SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
                                EventRow(event = event, onClick = { onOpenEvent(event.id.value) })
                            }
                        }
                    }
                    if (state.tasks.isNotEmpty()) {
                        item {
                            SuperPlannerSectionHeader(
                                title = stringResource(R.string.nav_tarefas),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        items(state.tasks, key = { it.id.value }) { task ->
                            SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
                                TaskRow(
                                    task = task,
                                    onClick = { onOpenTask(task.id.value) },
                                    onToggleDone = { viewModel.setTaskDone(task.id.value, it) },
                                )
                            }
                        }
                    }
                    if (state.habits.isNotEmpty()) {
                        item {
                            SuperPlannerSectionHeader(
                                title = stringResource(R.string.nav_habitos),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        items(state.habits, key = { it.habit.id.value }) { habitDay ->
                            SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
                                HabitDayRow(
                                    item = habitDay,
                                    onClick = { onOpenHabit(habitDay.habit.id.value) },
                                    onToggleDone = { viewModel.setHabitDone(habitDay.habit.id.value, it) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
